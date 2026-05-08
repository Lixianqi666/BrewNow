package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.dto.recommend.RecommendationEvaluation;
import com.brewnow.dto.recommend.RecommendationItem;
import com.brewnow.dto.recommend.RecommendationStats;
import com.brewnow.entity.Product;
import com.brewnow.entity.UserBehavior;
import com.brewnow.service.RecommendationService;
import com.brewnow.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommend")
@Tag(name = "推荐模块", description = "协同过滤、时间衰减、季节增强与评估相关接口")
public class RecommendController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/home")
    @Operation(summary = "首页推荐商品", description = "返回首页推荐商品列表，已融合协同过滤、时间衰减与季节增强策略")
    public Result<List<Product>> getHomeRecommendations(
            @Parameter(description = "Bearer Token，可选；登录后可返回个性化推荐")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(recommendationService.getHomeRecommendations(resolveUserId(authHeader), limit));
    }

    @GetMapping("/home/explain")
    @Operation(summary = "首页推荐解释", description = "返回带推荐理由的首页推荐结果")
    public Result<List<RecommendationItem>> getHomeRecommendationItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(recommendationService.getHomeRecommendationItems(resolveUserId(authHeader), limit));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "相关推荐商品", description = "基于当前商品生成相关推荐列表")
    public Result<List<Product>> getRelatedProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendationService.getRelatedProducts(resolveUserId(authHeader), productId, limit));
    }

    @GetMapping("/product/{productId}/explain")
    @Operation(summary = "相关推荐解释", description = "返回带推荐理由的相关推荐结果")
    public Result<List<RecommendationItem>> getRelatedRecommendationItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendationService.getRelatedRecommendationItems(resolveUserId(authHeader), productId, limit));
    }

    @GetMapping("/stats")
    @Operation(summary = "推荐统计", description = "返回行为规模、推荐覆盖率和评估摘要，适合产品演示与运营分析")
    public Result<RecommendationStats> getRecommendationStats(@RequestParam(defaultValue = "10") Integer topK) {
        return Result.success(recommendationService.getRecommendationStats(topK));
    }

    @GetMapping("/evaluation")
    @Operation(summary = "推荐效果评估", description = "对比基线协同过滤、时间衰减协同过滤和季节增强协同过滤效果")
    public Result<RecommendationEvaluation> getRecommendationEvaluation(@RequestParam(defaultValue = "10") Integer topK) {
        return Result.success(recommendationService.evaluateRecommendationQuality(topK));
    }

    @GetMapping("/stats/export")
    @Operation(summary = "导出推荐统计", description = "导出推荐统计与策略评估数据，文件格式为 xlsx")
    public ResponseEntity<byte[]> exportRecommendationStats(@RequestParam(defaultValue = "10") Integer topK) {
        RecommendationStats stats = recommendationService.getRecommendationStats(topK);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet overviewSheet = workbook.createSheet("概览");
            int rowIndex = 0;

            rowIndex = writePairRow(overviewSheet, rowIndex, "指标", "值");
            rowIndex = writePairRow(overviewSheet, rowIndex, "行为总数", String.valueOf(stats.getTotalBehaviors()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "活跃用户", String.valueOf(stats.getActiveUsers()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "可推荐用户", String.valueOf(stats.getRecommendableUsers()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "在售茶品", String.valueOf(stats.getActiveProducts()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "收藏总量", String.valueOf(stats.getTotalFavorites()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "TopK", String.valueOf(stats.getEvaluation().getTopK()));
            rowIndex = writePairRow(overviewSheet, rowIndex, "时间衰减 lambda", String.valueOf(stats.getEvaluation().getLambda()));
            writePairRow(overviewSheet, rowIndex, "当前季节", stats.getEvaluation().getSeason());

            Sheet behaviorSheet = workbook.createSheet("行为分布");
            int behaviorRow = 0;
            Row behaviorHeader = behaviorSheet.createRow(behaviorRow++);
            behaviorHeader.createCell(0).setCellValue("行为类型");
            behaviorHeader.createCell(1).setCellValue("次数");
            for (Map.Entry<String, Integer> entry : stats.getBehaviorTypeCounts().entrySet()) {
                Row row = behaviorSheet.createRow(behaviorRow++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue());
            }

            Sheet evaluationSheet = workbook.createSheet("策略评估");
            int evalRow = 0;
            Row evalHeader = evaluationSheet.createRow(evalRow++);
            evalHeader.createCell(0).setCellValue("方案");
            evalHeader.createCell(1).setCellValue("Precision@K");
            evalHeader.createCell(2).setCellValue("Recall@K");
            evalHeader.createCell(3).setCellValue("HitRate@K");
            evalHeader.createCell(4).setCellValue("评估用户数");

            evalRow = writeEvaluationRow(
                    evaluationSheet,
                    evalRow,
                    "基线协同过滤",
                    stats.getEvaluation().getBaseline().getPrecisionAtK(),
                    stats.getEvaluation().getBaseline().getRecallAtK(),
                    stats.getEvaluation().getBaseline().getHitRateAtK(),
                    stats.getEvaluation().getBaseline().getEvaluatedUsers());

            evalRow = writeEvaluationRow(
                    evaluationSheet,
                    evalRow,
                    "时间衰减协同过滤",
                    stats.getEvaluation().getTimeDecay().getPrecisionAtK(),
                    stats.getEvaluation().getTimeDecay().getRecallAtK(),
                    stats.getEvaluation().getTimeDecay().getHitRateAtK(),
                    stats.getEvaluation().getTimeDecay().getEvaluatedUsers());

            writeEvaluationRow(
                    evaluationSheet,
                    evalRow,
                    "季节增强协同过滤",
                    stats.getEvaluation().getSeasonAware().getPrecisionAtK(),
                    stats.getEvaluation().getSeasonAware().getRecallAtK(),
                    stats.getEvaluation().getSeasonAware().getHitRateAtK(),
                    stats.getEvaluation().getSeasonAware().getEvaluatedUsers());

            Sheet recentSheet = workbook.createSheet("近期行为样本");
            int recentRow = 0;
            Row recentHeader = recentSheet.createRow(recentRow++);
            recentHeader.createCell(0).setCellValue("用户ID");
            recentHeader.createCell(1).setCellValue("商品ID");
            recentHeader.createCell(2).setCellValue("行为类型");
            recentHeader.createCell(3).setCellValue("行为权重");
            recentHeader.createCell(4).setCellValue("时间");
            for (UserBehavior behavior : stats.getRecentBehaviors()) {
                Row row = recentSheet.createRow(recentRow++);
                row.createCell(0).setCellValue(behavior.getUserId() == null ? 0 : behavior.getUserId());
                row.createCell(1).setCellValue(behavior.getProductId() == null ? 0 : behavior.getProductId());
                row.createCell(2).setCellValue(behavior.getBehaviorType() == null ? "" : behavior.getBehaviorType());
                row.createCell(3).setCellValue(behavior.getBehaviorWeight() == null ? "0" : behavior.getBehaviorWeight().toString());
                row.createCell(4).setCellValue(behavior.getCreatedAt() == null ? "" : behavior.getCreatedAt().toString());
            }

            for (Sheet sheet : new Sheet[] { overviewSheet, behaviorSheet, evaluationSheet, recentSheet }) {
                if (sheet.getRow(0) == null) {
                    continue;
                }
                for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(outputStream);

            String filename = "推荐统计导出.xlsx";
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private int writePairRow(Sheet sheet, int rowIndex, String key, String value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value == null ? "" : value);
        return rowIndex + 1;
    }

    private int writeEvaluationRow(
            Sheet sheet,
            int rowIndex,
            String strategy,
            double precision,
            double recall,
            double hitRate,
            int users) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(strategy);
        row.createCell(1).setCellValue(precision);
        row.createCell(2).setCellValue(recall);
        row.createCell(3).setCellValue(hitRate);
        row.createCell(4).setCellValue(users);
        return rowIndex + 1;
    }

    private Integer resolveUserId(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            return jwtUtil.getUserIdFromToken(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
