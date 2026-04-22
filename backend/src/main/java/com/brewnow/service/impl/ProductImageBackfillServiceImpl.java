package com.brewnow.service.impl;

import com.brewnow.entity.Product;
import com.brewnow.mapper.ProductMapper;
import com.brewnow.service.MinioStorageService;
import com.brewnow.service.ProductImageBackfillService;
import com.brewnow.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageBackfillServiceImpl implements ProductImageBackfillService {

    private final ProductService productService;
    private final MinioStorageService minioStorageService;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public int backfillProductImages(boolean overwriteAll) {
        List<Product> products = productService.getProductsByPageIncludeDeleted(1, Integer.MAX_VALUE);
        int updatedCount = 0;

        for (Product product : products) {
            if (product == null || product.getProductId() == null) {
                continue;
            }

            if (!overwriteAll && shouldKeepCurrentImage(product.getImageUrl())) {
                continue;
            }

            String fileName = "catalog/product-" + product.getProductId() + ".svg";
            String svg = buildSvg(product);
            String imageUrl = minioStorageService.uploadBytes(
                    svg.getBytes(StandardCharsets.UTF_8),
                    "image/svg+xml",
                    "products",
                    fileName
            );

            updatedCount += productMapper.updateImageUrlById(product.getProductId(), imageUrl);
        }

        log.info("商品图片回填完成，更新数量: {}", updatedCount);
        return updatedCount;
    }

    private boolean shouldKeepCurrentImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return false;
        }
        String normalized = imageUrl.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("/brew-now/products/")
                || normalized.contains("127.0.0.1:9000/brew-now/products/")
                || normalized.contains("localhost:9000/brew-now/products/");
    }

    private String buildSvg(Product product) {
        Palette palette = paletteFor(product.getCategory());
        String title = escapeSvg(product.getProductName());
        String category = escapeSvg(defaultText(product.getCategory(), "茶叶"));
        String brand = escapeSvg(defaultText(product.getBrand(), "沏刻"));
        String spec = escapeSvg(defaultText(product.getCompatibleDevices(), "精选茶品"));

        return """
                <svg width="1200" height="1200" viewBox="0 0 1200 1200" xmlns="http://www.w3.org/2000/svg">
                  <defs>
                    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%" stop-color="__START__"/>
                      <stop offset="100%" stop-color="__END__"/>
                    </linearGradient>
                    <radialGradient id="glow" cx="0.2" cy="0.2" r="0.9">
                      <stop offset="0%" stop-color="__GLOW__" stop-opacity="0.85"/>
                      <stop offset="100%" stop-color="#ffffff" stop-opacity="0"/>
                    </radialGradient>
                  </defs>
                  <rect width="1200" height="1200" rx="48" fill="url(#bg)"/>
                  <rect width="1200" height="1200" rx="48" fill="url(#glow)"/>
                  <circle cx="930" cy="250" r="170" fill="__ACCENT__" fill-opacity="0.20"/>
                  <circle cx="220" cy="940" r="210" fill="__LEAF__" fill-opacity="0.18"/>
                  <rect x="92" y="92" width="1016" height="1016" rx="42" fill="rgba(255,255,255,0.18)" stroke="rgba(255,255,255,0.35)"/>
                  <text x="120" y="188" font-size="52" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="__LABEL__" font-weight="700">__BRAND__</text>
                  <text x="120" y="292" font-size="112" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="__TITLECOLOR__" font-weight="800">__TITLE__</text>
                  <text x="120" y="382" font-size="44" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="__SUBTLE__">__META__</text>
                  <text x="120" y="910" font-size="62" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="__LABEL__" font-weight="700">BrewNow 沏刻茶叶电商平台</text>
                  <text x="120" y="980" font-size="34" font-family="'PingFang SC','Microsoft YaHei',sans-serif" fill="__SUBTLE__">精选茶品 · 商品示意图 · 已迁移至 MinIO</text>
                  <g transform="translate(760, 620)">
                    <path d="M0 0 C70 -160, 200 -240, 300 -360 C260 -180, 150 -40, 20 60 Z" fill="__ACCENT__" fill-opacity="0.88"/>
                    <path d="M-120 120 C-10 -30, 110 -90, 250 -120 C150 0, 80 120, -40 220 Z" fill="__LEAF__" fill-opacity="0.72"/>
                  </g>
                </svg>
                """
                .replace("__START__", palette.start)
                .replace("__END__", palette.end)
                .replace("__GLOW__", palette.glow)
                .replace("__ACCENT__", palette.accent)
                .replace("__LEAF__", palette.leaf)
                .replace("__LABEL__", palette.label)
                .replace("__TITLECOLOR__", palette.title)
                .replace("__SUBTLE__", palette.subtle)
                .replace("__BRAND__", brand)
                .replace("__TITLE__", title)
                .replace("__META__", category + " · " + spec);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String escapeSvg(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private Palette paletteFor(String category) {
        if (category == null) {
            return new Palette("#f2e7cf", "#d6c19d", "#fff7e2", "#8b5e34", "#426a4b", "#5c4831", "#1f3a2d", "#6d6556");
        }
        return switch (category.trim()) {
            case "绿茶" -> new Palette("#dcebd2", "#adc89a", "#f8fff2", "#4f7c45", "#5f8f63", "#4d6a38", "#25422d", "#5c7057");
            case "红茶" -> new Palette("#f0d5c9", "#c48c72", "#fff3ed", "#9b4f32", "#7f3b2b", "#7f3b2b", "#4a231c", "#7e6158");
            case "乌龙茶" -> new Palette("#d9e4d6", "#9eb39f", "#f7fbf4", "#58725a", "#4d6654", "#5c6f5f", "#20372a", "#687767");
            case "白茶" -> new Palette("#f3efe4", "#d8cfb9", "#fffef8", "#8f8366", "#c5baa0", "#7a6f56", "#463f31", "#7d776c");
            case "花茶" -> new Palette("#f4dedb", "#ddb3ab", "#fff4f3", "#a45d62", "#c98d7b", "#8d5960", "#4f2f35", "#8a7074");
            case "普洱茶" -> new Palette("#ead8c6", "#ba9670", "#fff5e9", "#7f5633", "#6a4a2f", "#7f5633", "#3f2c1c", "#776658");
            default -> new Palette("#f2e7cf", "#d6c19d", "#fff7e2", "#8b5e34", "#426a4b", "#5c4831", "#1f3a2d", "#6d6556");
        };
    }

    private record Palette(
            String start,
            String end,
            String glow,
            String accent,
            String leaf,
            String subtle,
            String title,
            String label
    ) {
    }
}
