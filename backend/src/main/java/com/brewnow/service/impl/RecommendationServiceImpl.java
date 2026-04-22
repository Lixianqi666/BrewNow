package com.brewnow.service.impl;

import com.brewnow.dto.recommend.RecommendationEvaluation;
import com.brewnow.dto.recommend.RecommendationItem;
import com.brewnow.dto.recommend.RecommendationMetrics;
import com.brewnow.dto.recommend.RecommendationStats;
import com.brewnow.entity.Product;
import com.brewnow.entity.User;
import com.brewnow.entity.UserBehavior;
import com.brewnow.enums.UserRole;
import com.brewnow.mapper.UserBehaviorMapper;
import com.brewnow.service.FavoriteService;
import com.brewnow.service.ProductService;
import com.brewnow.service.RecommendationService;
import com.brewnow.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final List<String> BEHAVIOR_TYPES = List.of("VIEW", "FAVORITE", "CART", "PURCHASE");

    @Value("${recommend.lambda:0.08}")
    private double lambda;

    @Autowired
    private ProductService productService;

    @Autowired
    private BehaviorServiceImpl behaviorService;

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserService userService;

    @Override
    @Cacheable(cacheNames = "recommend:home", key = "'home:' + (#userId == null ? 'guest' : #userId) + ':' + #limit")
    public List<Product> getHomeRecommendations(Integer userId, int limit) {
        return getHomeRecommendationItems(userId, limit).stream()
                .map(RecommendationItem::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "recommend:related", key = "'related:' + #productId + ':' + (#userId == null ? 'guest' : #userId) + ':' + #limit")
    public List<Product> getRelatedProducts(Integer userId, Integer productId, int limit) {
        return getRelatedRecommendationItems(userId, productId, limit).stream()
                .map(RecommendationItem::getProduct)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationItem> getHomeRecommendationItems(Integer userId, int limit) {
        RecommendationContext context = buildHomeRecommendationContext(userId, limit, lambda, true);
        return context.items;
    }

    @Override
    public List<RecommendationItem> getRelatedRecommendationItems(Integer userId, Integer productId, int limit) {
        RecommendationContext context = buildRelatedRecommendationContext(productId, limit, lambda, true);
        return context.items;
    }

    @Override
    @Cacheable(cacheNames = "recommend:stats", key = "'stats:' + #topK")
    public RecommendationStats getRecommendationStats(int topK) {
        List<UserBehavior> allBehaviors = behaviorService.getAllBehaviors();
        RecommendationStats stats = new RecommendationStats();
        stats.setTotalBehaviors(defaultInt(userBehaviorMapper.countAll()));
        stats.setActiveUsers(defaultInt(userBehaviorMapper.countDistinctUsers()));
        stats.setActiveProducts(defaultInt(productService.getProductCount()));
        stats.setTotalFavorites(defaultInt(favoriteService.getActiveFavoriteCount()));
        stats.setRecommendableUsers(countRecommendableUsers(allBehaviors));

        Map<String, Integer> behaviorTypeCounts = new LinkedHashMap<>();
        for (String behaviorType : BEHAVIOR_TYPES) {
            behaviorTypeCounts.put(behaviorType, defaultInt(userBehaviorMapper.countByType(behaviorType)));
        }
        stats.setBehaviorTypeCounts(behaviorTypeCounts);
        stats.setRecentBehaviors(userBehaviorMapper.selectRecent(8));
        stats.setEvaluation(evaluateRecommendationQuality(topK));
        return stats;
    }

    @Override
    @Cacheable(cacheNames = "recommend:evaluation", key = "'evaluation:' + #topK")
    public RecommendationEvaluation evaluateRecommendationQuality(int topK) {
        List<UserBehavior> behaviors = behaviorService.getAllBehaviors();
        RecommendationEvaluation evaluation = new RecommendationEvaluation();
        evaluation.setTopK(topK);
        evaluation.setLambda(lambda);
        evaluation.setSeason(resolveSeasonLabel(LocalDateTime.now().getMonth()));
        evaluation.setBaseline(calculateEvaluationMetrics(behaviors, 0D, topK));
        evaluation.setTimeDecay(calculateEvaluationMetrics(behaviors, lambda, topK));
        evaluation.setSeasonAware(calculateEvaluationMetrics(behaviors, lambda, topK, true));
        return evaluation;
    }

    private RecommendationContext buildHomeRecommendationContext(Integer userId, int limit, double lambdaValue, boolean applySeasonality) {
        List<Product> activeProducts = productService.getAllProducts();
        RecommendationContext context = new RecommendationContext();
        if (activeProducts.isEmpty()) {
            context.items = List.of();
            return context;
        }

        Integer consumerUserId = normalizeConsumerUser(userId);
        if (consumerUserId == null) {
            context.items = productService.getHotProducts(limit).stream()
                    .map(product -> toRecommendationItem(product, 0D, "POPULAR", "基于全站热销茶品为你推荐"))
                    .collect(Collectors.toList());
            return context;
        }

        List<UserBehavior> behaviors = behaviorService.getAllBehaviors();
        Map<Integer, List<UserBehavior>> userBehaviorMap = behaviors.stream()
                .filter(behavior -> behavior.getUserId() != null && behavior.getProductId() != null)
                .collect(Collectors.groupingBy(UserBehavior::getUserId));
        Map<Integer, Map<Integer, Double>> userItemScores = buildUserItemScores(behaviors, lambdaValue);
        Map<Integer, Double> currentUserScores = userItemScores.getOrDefault(consumerUserId, Map.of());

        if (currentUserScores.isEmpty()) {
            context.items = buildFallbackItems(activeProducts, null, limit, Set.of(), "CONTENT", "基于茶类、口感与产地的冷启动推荐");
            return context;
        }

        Map<Integer, Map<Integer, Double>> itemUserScores = invertUserItemScores(userItemScores);
        Set<Integer> interactedProducts = currentUserScores.keySet();
        Map<Integer, Double> recommendationScores = new HashMap<>();
        Map<Integer, Integer> bestSourceMap = new HashMap<>();
        Map<Integer, Double> bestSourceScore = new HashMap<>();

        for (Integer sourceProductId : interactedProducts) {
            for (Product candidate : activeProducts) {
                Integer candidateId = candidate.getProductId();
                if (candidateId == null || interactedProducts.contains(candidateId)) {
                    continue;
                }
                double similarity = calculateItemSimilarity(sourceProductId, candidateId, itemUserScores);
                if (similarity <= 0) {
                    continue;
                }
                double sourceWeight = currentUserScores.getOrDefault(sourceProductId, 0D);
                double mergedScore = similarity * sourceWeight;
                if (applySeasonality) {
                    mergedScore *= seasonScore(candidate, LocalDateTime.now().getMonth());
                }
                recommendationScores.merge(candidateId, mergedScore, Double::sum);
                if (mergedScore > bestSourceScore.getOrDefault(candidateId, 0D)) {
                    bestSourceScore.put(candidateId, mergedScore);
                    bestSourceMap.put(candidateId, sourceProductId);
                }
            }
        }

        List<RecommendationItem> cfItems = activeProducts.stream()
                .filter(product -> recommendationScores.containsKey(product.getProductId()))
                .sorted(Comparator.comparingDouble((Product product) ->
                        recommendationScores.getOrDefault(product.getProductId(), 0D)).reversed())
                .limit(limit)
                .map(product -> {
                    Integer sourceId = bestSourceMap.get(product.getProductId());
                    Product sourceProduct = findProductById(activeProducts, sourceId);
                    String reason = buildHomeReason(sourceProduct, product, userBehaviorMap.getOrDefault(consumerUserId, List.of()), applySeasonality);
                    return toRecommendationItem(product,
                            recommendationScores.getOrDefault(product.getProductId(), 0D),
                            applySeasonality ? "CF_TIME_DECAY_SEASON" : "CF_TIME_DECAY",
                            reason);
                })
                .collect(Collectors.toList());

        if (cfItems.size() >= limit) {
            context.items = cfItems;
            return context;
        }

        List<RecommendationItem> fallbackItems = buildFallbackItems(
                activeProducts,
                deriveProfileProducts(activeProducts, currentUserScores.keySet()),
                limit,
                cfItems.stream().map(item -> item.getProduct().getProductId()).collect(Collectors.toSet()),
                "CONTENT",
                applySeasonality ? "基于你偏好的茶类、风味、产地与当季饮茶偏好补充推荐" : "基于你偏好的茶类、风味与产地补充推荐"
        );
        context.items = mergeRecommendationItems(limit, cfItems, fallbackItems);
        return context;
    }

    private RecommendationContext buildRelatedRecommendationContext(Integer productId, int limit, double lambdaValue, boolean applySeasonality) {
        RecommendationContext context = new RecommendationContext();
        Product currentProduct = productService.getProductById(productId);
        if (currentProduct == null) {
            context.items = List.of();
            return context;
        }

        List<Product> activeProducts = productService.getAllProducts().stream()
                .filter(product -> !productId.equals(product.getProductId()))
                .collect(Collectors.toList());
        List<UserBehavior> behaviors = behaviorService.getAllBehaviors();
        Map<Integer, Map<Integer, Double>> userItemScores = buildUserItemScores(behaviors, lambdaValue);
        Map<Integer, Map<Integer, Double>> itemUserScores = invertUserItemScores(userItemScores);
        Map<Integer, Double> similarityScores = new HashMap<>();

        for (Product candidate : activeProducts) {
            if (candidate.getProductId() == null) {
                continue;
            }
            double similarity = calculateItemSimilarity(productId, candidate.getProductId(), itemUserScores);
            if (similarity > 0) {
                double score = applySeasonality
                        ? similarity * seasonScore(candidate, LocalDateTime.now().getMonth())
                        : similarity;
                similarityScores.put(candidate.getProductId(), score);
            }
        }

        List<RecommendationItem> cfItems = activeProducts.stream()
                .filter(product -> similarityScores.containsKey(product.getProductId()))
                .sorted(Comparator.comparingDouble((Product candidate) ->
                        similarityScores.getOrDefault(candidate.getProductId(), 0D)).reversed())
                .limit(limit)
                .map(product -> toRecommendationItem(
                        product,
                        similarityScores.getOrDefault(product.getProductId(), 0D),
                        applySeasonality ? "SIMILARITY_SEASON" : "SIMILARITY",
                        buildRelatedReason(currentProduct, product, "与当前茶品存在相似浏览/购买轨迹", applySeasonality)
                ))
                .collect(Collectors.toList());

        if (cfItems.size() >= limit) {
            context.items = cfItems;
            return context;
        }

        List<RecommendationItem> fallbackItems = buildFallbackItems(
                activeProducts,
                List.of(currentProduct),
                limit,
                cfItems.stream().map(item -> item.getProduct().getProductId()).collect(Collectors.toSet()),
                "CONTENT",
                buildRelatedReason(currentProduct, null, "同类茶品内容相似补充推荐", applySeasonality)
        );

        context.items = mergeRecommendationItems(limit, cfItems, fallbackItems);
        return context;
    }

    private Integer normalizeConsumerUser(Integer userId) {
        if (userId == null) {
            return null;
        }
        User user = userService.getUserById(userId);
        if (user == null || user.getRole() != UserRole.CONSUMER) {
            return null;
        }
        return userId;
    }

    private Map<Integer, Map<Integer, Double>> buildUserItemScores(List<UserBehavior> behaviors, double lambdaValue) {
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, Map<Integer, Double>> result = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            if (behavior.getUserId() == null || behavior.getProductId() == null || behavior.getBehaviorWeight() == null) {
                continue;
            }
            LocalDateTime createdAt = behavior.getCreatedAt() != null ? behavior.getCreatedAt() : now;
            long hours = Math.max(0, Duration.between(createdAt, now).toHours());
            double deltaDays = hours / 24.0;
            double decayedWeight = behavior.getBehaviorWeight().doubleValue() * Math.exp(-lambdaValue * deltaDays);

            result.computeIfAbsent(behavior.getUserId(), key -> new HashMap<>())
                    .merge(behavior.getProductId(), decayedWeight, Double::sum);
        }
        return result;
    }

    private Map<Integer, Map<Integer, Double>> invertUserItemScores(Map<Integer, Map<Integer, Double>> userItemScores) {
        Map<Integer, Map<Integer, Double>> itemUserScores = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entry : userItemScores.entrySet()) {
            Integer userId = entry.getKey();
            for (Map.Entry<Integer, Double> itemScore : entry.getValue().entrySet()) {
                itemUserScores.computeIfAbsent(itemScore.getKey(), key -> new HashMap<>())
                        .put(userId, itemScore.getValue());
            }
        }
        return itemUserScores;
    }

    private double calculateItemSimilarity(Integer sourceProductId,
                                           Integer candidateProductId,
                                           Map<Integer, Map<Integer, Double>> itemUserScores) {
        if (sourceProductId == null || candidateProductId == null || sourceProductId.equals(candidateProductId)) {
            return 0;
        }

        Map<Integer, Double> sourceUsers = itemUserScores.get(sourceProductId);
        Map<Integer, Double> candidateUsers = itemUserScores.get(candidateProductId);
        if (sourceUsers == null || candidateUsers == null || sourceUsers.isEmpty() || candidateUsers.isEmpty()) {
            return 0;
        }

        double dot = 0;
        double sourceNorm = 0;
        double candidateNorm = 0;
        for (Double value : sourceUsers.values()) {
            sourceNorm += value * value;
        }
        for (Double value : candidateUsers.values()) {
            candidateNorm += value * value;
        }
        for (Map.Entry<Integer, Double> entry : sourceUsers.entrySet()) {
            dot += entry.getValue() * candidateUsers.getOrDefault(entry.getKey(), 0D);
        }
        if (dot <= 0 || sourceNorm <= 0 || candidateNorm <= 0) {
            return 0;
        }
        return dot / (Math.sqrt(sourceNorm) * Math.sqrt(candidateNorm));
    }

    private List<Product> deriveProfileProducts(List<Product> activeProducts, Set<Integer> productIds) {
        return activeProducts.stream()
                .filter(product -> productIds.contains(product.getProductId()))
                .collect(Collectors.toList());
    }

    private List<RecommendationItem> buildFallbackItems(List<Product> activeProducts,
                                                        List<Product> anchorProducts,
                                                        int limit,
                                                        Set<Integer> excludedIds,
                                                        String strategy,
                                                        String defaultReason) {
        Set<Integer> excludeSet = new HashSet<>(excludedIds);
        Map<Integer, Double> scores = new HashMap<>();

        if (anchorProducts != null && !anchorProducts.isEmpty()) {
            for (Product candidate : activeProducts) {
                if (candidate.getProductId() == null || excludeSet.contains(candidate.getProductId())) {
                    continue;
                }
                double bestScore = 0;
                Product bestAnchor = null;
                for (Product anchor : anchorProducts) {
                    if (anchor.getProductId() != null && anchor.getProductId().equals(candidate.getProductId())) {
                        continue;
                    }
                    double score = calculateContentSimilarity(anchor, candidate);
                    score *= seasonScore(candidate, LocalDateTime.now().getMonth());
                    if (score > bestScore) {
                        bestScore = score;
                        bestAnchor = anchor;
                    }
                }
                if (bestScore > 0) {
                    scores.put(candidate.getProductId(), bestScore);
                }
            }
        }

        List<RecommendationItem> tagItems = activeProducts.stream()
                .filter(product -> product.getProductId() != null && !excludeSet.contains(product.getProductId()))
                .filter(product -> scores.containsKey(product.getProductId()))
                .sorted(Comparator.comparingDouble((Product product) ->
                        scores.getOrDefault(product.getProductId(), 0D)).reversed())
                .limit(limit)
                .map(product -> toRecommendationItem(product, scores.get(product.getProductId()), strategy, defaultReason))
                .collect(Collectors.toList());

        if (tagItems.size() >= limit) {
            return tagItems;
        }

        List<RecommendationItem> hotItems = productService.getHotProducts(limit * 2).stream()
                .filter(product -> product.getProductId() != null && !excludeSet.contains(product.getProductId()))
                .map(product -> toRecommendationItem(product, 0D, "POPULAR", "基于茶品热度补充推荐"))
                .collect(Collectors.toList());
        return mergeRecommendationItems(limit, tagItems, hotItems);
    }

    private double calculateContentSimilarity(Product anchor, Product candidate) {
        double score = 0;
        if (safeEquals(anchor.getCategory(), candidate.getCategory())) {
            score += 4;
        }
        if (safeEquals(anchor.getBrand(), candidate.getBrand())) {
            score += 1.5;
        }
        if (safeEquals(anchor.getOriginPlace(), candidate.getOriginPlace())) {
            score += 1.8;
        }
        score += intersectScore(anchor.getTeaTags(), candidate.getTeaTags(), 1.4);
        score += intersectScore(anchor.getFlavorProfile(), candidate.getFlavorProfile(), 1.3);
        score += priceBandScore(anchor, candidate);
        return score;
    }

    private double intersectScore(String source, String target, double unitScore) {
        Set<String> sourceTokens = tokenize(source);
        Set<String> targetTokens = tokenize(target);
        sourceTokens.retainAll(targetTokens);
        return sourceTokens.size() * unitScore;
    }

    private Set<String> tokenize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return new HashSet<>();
        }
        return List.of(value.split("[,，/、 ]")).stream()
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }

    private boolean safeEquals(String source, String target) {
        if (source == null || target == null) {
            return false;
        }
        return source.trim().equalsIgnoreCase(target.trim());
    }

    private double priceBandScore(Product anchor, Product candidate) {
        if (anchor.getPrice() == null || candidate.getPrice() == null) {
            return 0;
        }
        double diff = anchor.getPrice().subtract(candidate.getPrice()).abs().doubleValue();
        if (diff <= 20) {
            return 1.2;
        }
        if (diff <= 50) {
            return 0.6;
        }
        return 0;
    }

    private RecommendationMetrics calculateEvaluationMetrics(List<UserBehavior> behaviors, double lambdaValue, int topK) {
        return calculateEvaluationMetrics(behaviors, lambdaValue, topK, false);
    }

    private RecommendationMetrics calculateEvaluationMetrics(List<UserBehavior> behaviors, double lambdaValue, int topK, boolean applySeasonality) {
        Map<Integer, List<UserBehavior>> userBehaviorMap = behaviors.stream()
                .filter(behavior -> behavior.getUserId() != null && behavior.getProductId() != null)
                .collect(Collectors.groupingBy(UserBehavior::getUserId));

        int evaluatedUsers = 0;
        int hits = 0;
        double recallHits = 0;

        List<Product> activeProducts = productService.getAllProducts();
        for (Map.Entry<Integer, List<UserBehavior>> entry : userBehaviorMap.entrySet()) {
            List<UserBehavior> userBehaviors = entry.getValue().stream()
                    .sorted(Comparator.comparing(UserBehavior::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            LinkedHashSet<Integer> distinctProducts = userBehaviors.stream()
                    .map(UserBehavior::getProductId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (distinctProducts.size() < 2) {
                continue;
            }

            UserBehavior targetBehavior = userBehaviors.get(userBehaviors.size() - 1);
            Integer targetProductId = targetBehavior.getProductId();
            List<UserBehavior> trainingBehaviors = new ArrayList<>(userBehaviors.subList(0, userBehaviors.size() - 1));
            RecommendationContext context = buildEvaluationContext(activeProducts, entry.getKey(), trainingBehaviors, targetProductId, topK, lambdaValue, applySeasonality, true);
            if (context.items.isEmpty()) {
                continue;
            }

            evaluatedUsers++;
            boolean hit = context.items.stream()
                    .map(item -> item.getProduct().getProductId())
                    .anyMatch(targetProductId::equals);
            if (hit) {
                hits++;
                recallHits += 1;
            }
        }

        if (evaluatedUsers == 0) {
            return new RecommendationMetrics(0, 0, 0, 0);
        }
        double precision = hits / (double) (evaluatedUsers * topK);
        double recall = recallHits / evaluatedUsers;
        double hitRate = hits / (double) evaluatedUsers;
        return new RecommendationMetrics(round4(precision), round4(recall), round4(hitRate), evaluatedUsers);
    }

    private RecommendationContext buildEvaluationContext(List<Product> activeProducts,
                                                         Integer userId,
                                                         List<UserBehavior> trainingBehaviors,
                                                         Integer excludedTargetProductId,
                                                         int limit,
                                                         double lambdaValue,
                                                         boolean applySeasonality,
                                                         boolean evaluationMode) {
        RecommendationContext context = new RecommendationContext();
        List<UserBehavior> allBehaviors = new ArrayList<>(behaviorService.getAllBehaviors().stream()
                .filter(behavior -> !Objects.equals(behavior.getUserId(), userId))
                .collect(Collectors.toList()));
        allBehaviors.addAll(trainingBehaviors);

        Map<Integer, Map<Integer, Double>> userItemScores = buildUserItemScores(allBehaviors, lambdaValue);
        Map<Integer, Double> currentUserScores = userItemScores.getOrDefault(userId, Map.of());
        if (currentUserScores.isEmpty()) {
            context.items = List.of();
            return context;
        }
        Map<Integer, Map<Integer, Double>> itemUserScores = invertUserItemScores(userItemScores);
        Set<Integer> interactedProducts = new HashSet<>(currentUserScores.keySet());
        Map<Integer, Double> recommendationScores = new HashMap<>();

        // 在评估模式下，测试目标产品应该参与推荐但排在后面
        Integer evalTargetProduct = evaluationMode ? excludedTargetProductId : null;

        for (Integer sourceProductId : interactedProducts) {
            for (Product candidate : activeProducts) {
                Integer candidateId = candidate.getProductId();
                if (candidateId == null || interactedProducts.contains(candidateId)) {
                    continue;
                }
                // 在评估模式下允许测试产品参与推荐，在普通模式下排除
                if (!evaluationMode && Objects.equals(candidateId, excludedTargetProductId)) {
                    continue;
                }
                double similarity = calculateItemSimilarity(sourceProductId, candidateId, itemUserScores);
                if (similarity <= 0) {
                    continue;
                }
                double sourceWeight = currentUserScores.getOrDefault(sourceProductId, 0D);
                double score = similarity * sourceWeight;
                if (applySeasonality) {
                    score *= seasonScore(candidate, LocalDateTime.now().getMonth());
                }
                recommendationScores.merge(candidateId, score, Double::sum);
            }
        }

        // 构建推荐列表
        List<RecommendationItem> recommendations = activeProducts.stream()
                .filter(product -> recommendationScores.containsKey(product.getProductId()))
                .sorted(Comparator.comparingDouble((Product product) ->
                        recommendationScores.getOrDefault(product.getProductId(), 0D)).reversed())
                .limit(limit)
                .map(product -> toRecommendationItem(product, recommendationScores.get(product.getProductId()), "COLLAB", ""))
                .collect(Collectors.toList());

        // 如果协同过滤推荐不足，回退到基于内容特征的推荐
        if (recommendations.size() < limit) {
            List<Product> anchorProducts = deriveProfileProducts(activeProducts, interactedProducts);
            // 评估模式下排除历史交互但不排除测试产品
            Set<Integer> excludeForContent = evaluationMode
                    ? new HashSet<>(interactedProducts)  // 评估模式：只排除历史交互
                    : new HashSet<>(interactedProducts);
            excludeForContent.addAll(Collections.singleton(excludedTargetProductId)); // 非评估模式：也排除测试产品

            List<RecommendationItem> contentRecs = buildFallbackItems(
                    activeProducts, anchorProducts, limit,
                    excludeForContent,
                    "CONTENT",
                    "基于茶叶分类与口味偏好推荐"
            );

            // 合并推荐结果，避免重复，协同过滤结果优先
            Set<Integer> addedProductIds = recommendations.stream()
                    .map(r -> r.getProduct().getProductId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (RecommendationItem item : contentRecs) {
                Integer pid = item.getProduct().getProductId();
                // 评估模式下允许添加测试产品（放在末尾以降低优先级）
                if (pid != null && !addedProductIds.contains(pid)) {
                    if (!evaluationMode && Objects.equals(pid, excludedTargetProductId)) {
                        continue; // 非评估模式才排除测试产品
                    }
                    recommendations.add(item);
                    addedProductIds.add(pid);
                    if (recommendations.size() >= limit) break;
                }
            }
        }

        // 评估模式：计算测试产品的内容相似度分数，添加到推荐列表末尾
        if (evaluationMode && excludedTargetProductId != null) {
            Product targetProduct = activeProducts.stream()
                    .filter(p -> Objects.equals(p.getProductId(), excludedTargetProductId))
                    .findFirst()
                    .orElse(null);

            if (targetProduct != null) {
                double contentScore = 0;
                for (Integer sourceProductId : interactedProducts) {
                    Product sourceProduct = activeProducts.stream()
                            .filter(p -> Objects.equals(p.getProductId(), sourceProductId))
                            .findFirst()
                            .orElse(null);
                    if (sourceProduct != null) {
                        double sim = calculateContentSimilarity(sourceProduct, targetProduct);
                        if (sim > contentScore) {
                            contentScore = sim;
                        }
                    }
                }

                boolean alreadyInList = recommendations.stream()
                        .anyMatch(r -> Objects.equals(r.getProduct().getProductId(), excludedTargetProductId));

                if (!alreadyInList && contentScore > 0) {
                    // 测试产品不在列表中但内容相似度>0，按内容相似度排序后插入
                    RecommendationItem targetItem = toRecommendationItem(targetProduct, contentScore, "EVAL_TARGET", "测试目标产品（内容相似度）");
                    recommendations.add(targetItem);

                    // 重新排序：COLLAB > CONTENT > EVAL_TARGET
                    final int evalTargetRank = recommendations.size();
                    recommendations.sort((a, b) -> {
                        String sA = a.getStrategy() != null ? a.getStrategy() : "";
                        String sB = b.getStrategy() != null ? b.getStrategy() : "";
                        if (sA.equals("COLLAB") && !sB.equals("COLLAB")) return -1;
                        if (!sA.equals("COLLAB") && sB.equals("COLLAB")) return 1;
                        if (sA.equals("CONTENT") && sB.equals("EVAL_TARGET")) return -1;
                        if (sA.equals("EVAL_TARGET") && sB.equals("CONTENT")) return 1;
                        return Double.compare(b.getScore(), a.getScore()); // 同策略按分数降序
                    });

                    // 限制为 topK
                    if (recommendations.size() > limit) {
                        recommendations = recommendations.subList(0, limit);
                    }
                }
            }
        }

        context.items = recommendations;
        return context;
    }

    private int countRecommendableUsers(List<UserBehavior> behaviors) {
        return (int) behaviors.stream()
                .filter(behavior -> behavior.getUserId() != null && behavior.getProductId() != null)
                .collect(Collectors.groupingBy(UserBehavior::getUserId,
                        Collectors.mapping(UserBehavior::getProductId, Collectors.toSet())))
                .values().stream()
                .filter(products -> products.size() >= 2)
                .count();
    }

    private RecommendationItem toRecommendationItem(Product product, double score, String strategy, String reason) {
        RecommendationItem item = new RecommendationItem();
        item.setProduct(product);
        item.setScore(round4(score));
        item.setStrategy(strategy);
        item.setReason(reason);
        return item;
    }

    private String buildHomeReason(Product sourceProduct, Product candidate, List<UserBehavior> userBehaviors) {
        return buildHomeReason(sourceProduct, candidate, userBehaviors, false);
    }

    private String buildHomeReason(Product sourceProduct, Product candidate, List<UserBehavior> userBehaviors, boolean applySeasonality) {
        if (sourceProduct == null) {
            return applySeasonality
                    ? "根据你近期偏好的茶类与风味，并结合当季饮茶偏好为你推荐"
                    : "根据你近期偏好的茶类与风味为你推荐";
        }
        String behaviorLabel = resolveBehaviorLabel(sourceProduct.getProductId(), userBehaviors);
        StringBuilder reason = new StringBuilder("因为你近期");
        reason.append(behaviorLabel).append("过“").append(sourceProduct.getProductName()).append("”");
        if (candidate != null && safeEquals(sourceProduct.getCategory(), candidate.getCategory())) {
            reason.append("，且同属").append(candidate.getCategory());
        }
        if (applySeasonality && candidate != null) {
            String seasonHint = buildSeasonHint(candidate, LocalDateTime.now().getMonth());
            if (!seasonHint.isEmpty()) {
                reason.append("，").append(seasonHint);
            }
        }
        return reason.toString();
    }

    private String buildRelatedReason(Product currentProduct, Product candidate, String defaultReason) {
        return buildRelatedReason(currentProduct, candidate, defaultReason, false);
    }

    private String buildRelatedReason(Product currentProduct, Product candidate, String defaultReason, boolean applySeasonality) {
        if (currentProduct == null) {
            return defaultReason;
        }
        if (candidate == null) {
            String reason = "与“" + currentProduct.getProductName() + "”在茶类、风味或产地上相近";
            if (applySeasonality) {
                reason += "，并结合当季饮茶偏好进行补充";
            }
            return reason;
        }
        List<String> reasonParts = new ArrayList<>();
        if (safeEquals(currentProduct.getCategory(), candidate.getCategory())) {
            reasonParts.add("同属" + candidate.getCategory());
        }
        if (safeEquals(currentProduct.getOriginPlace(), candidate.getOriginPlace()) && candidate.getOriginPlace() != null) {
            reasonParts.add("同产地");
        }
        Set<String> flavorOverlap = tokenize(currentProduct.getFlavorProfile());
        flavorOverlap.retainAll(tokenize(candidate.getFlavorProfile()));
        if (!flavorOverlap.isEmpty()) {
            reasonParts.add("风味相近");
        }
        if (reasonParts.isEmpty()) {
            return applySeasonality ? defaultReason + "，并结合当季饮茶偏好排序" : defaultReason;
        }
        String reason = "与当前茶品" + String.join("、", reasonParts) + "，适合作为搭配选择";
        if (applySeasonality) {
            String seasonHint = buildSeasonHint(candidate, LocalDateTime.now().getMonth());
            if (!seasonHint.isEmpty()) {
                reason += "，" + seasonHint;
            }
        }
        return reason;
    }

    private String resolveBehaviorLabel(Integer productId, List<UserBehavior> userBehaviors) {
        return userBehaviors.stream()
                .filter(behavior -> Objects.equals(productId, behavior.getProductId()))
                .sorted(Comparator.comparing(UserBehavior::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(UserBehavior::getBehaviorType)
                .findFirst()
                .map(type -> switch (type) {
                    case "PURCHASE" -> "购买";
                    case "CART" -> "加购";
                    case "FAVORITE" -> "收藏";
                    default -> "浏览";
                })
                .orElse("浏览");
    }

    private double seasonScore(Product product, Month month) {
        if (product == null || product.getCategory() == null) {
            return 1.0;
        }
        String category = product.getCategory().trim();
        return switch (resolveSeason(month)) {
            case "SPRING" -> switch (category) {
                case "绿茶", "白茶" -> 1.18;
                case "乌龙茶" -> 1.08;
                default -> 1.0;
            };
            case "SUMMER" -> switch (category) {
                case "绿茶", "白茶" -> 1.16;
                case "其他" -> 1.05;
                default -> 1.0;
            };
            case "AUTUMN" -> switch (category) {
                case "乌龙茶" -> 1.18;
                case "红茶" -> 1.08;
                default -> 1.0;
            };
            case "WINTER" -> switch (category) {
                case "红茶", "黑茶" -> 1.2;
                case "乌龙茶" -> 1.06;
                default -> 1.0;
            };
            default -> 1.0;
        };
    }

    private String buildSeasonHint(Product product, Month month) {
        if (product == null) {
            return "";
        }
        return seasonScore(product, month) > 1.04 ? "更适合" + resolveSeasonLabel(month) + "饮用" : "";
    }

    private String resolveSeason(Month month) {
        return switch (month) {
            case MARCH, APRIL, MAY -> "SPRING";
            case JUNE, JULY, AUGUST -> "SUMMER";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "AUTUMN";
            default -> "WINTER";
        };
    }

    private String resolveSeasonLabel(Month month) {
        return switch (resolveSeason(month)) {
            case "SPRING" -> "春季";
            case "SUMMER" -> "夏季";
            case "AUTUMN" -> "秋季";
            default -> "冬季";
        };
    }

    private Product findProductById(List<Product> products, Integer productId) {
        if (productId == null) {
            return null;
        }
        return products.stream()
                .filter(product -> Objects.equals(product.getProductId(), productId))
                .findFirst()
                .orElse(null);
    }

    private List<RecommendationItem> mergeRecommendationItems(int limit,
                                                              List<RecommendationItem> primary,
                                                              List<RecommendationItem> secondary) {
        Map<Integer, RecommendationItem> merged = new LinkedHashMap<>();
        addRecommendationItems(merged, primary);
        addRecommendationItems(merged, secondary);
        return new ArrayList<>(merged.values()).stream().limit(limit).collect(Collectors.toList());
    }

    private void addRecommendationItems(Map<Integer, RecommendationItem> bucket, Collection<RecommendationItem> items) {
        for (RecommendationItem item : items) {
            if (item == null || item.getProduct() == null || item.getProduct().getProductId() == null) {
                continue;
            }
            bucket.putIfAbsent(item.getProduct().getProductId(), item);
        }
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round4(double value) {
        return Math.round(value * 10000D) / 10000D;
    }

    private static class RecommendationContext {
        private List<RecommendationItem> items = List.of();
    }
}
