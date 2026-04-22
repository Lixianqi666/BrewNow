package com.brewnow.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire}")
    private Long expire;

    /**
     * 生成Token（基础版本，保持兼容性）
     * 
     * @param userId   用户ID
     * @param userType 用户类型（user/admin）
     * @return token字符串
     */
    public String generateToken(Integer userId, String userType) {
        return generateToken(userId, userType, null, null);
    }

    /**
     * 生成Token（完整版本）
     * 
     * @param userId     用户ID
     * @param userType   用户类型（user/admin）
     * @param role       角色信息
     * @param merchantId 商家ID（可选）
     * @return token字符串
     */
    public String generateToken(Integer userId, String userType, String role, String merchantId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date expireDate = new Date(System.currentTimeMillis() + expire * 1000);

            var builder = JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("userType", userType)
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate);

            if (role != null) {
                builder.withClaim("role", role);
            }
            if (merchantId != null) {
                builder.withClaim("merchantId", merchantId);
            }

            return builder.sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Token生成失败", e);
        }
    }

    /**
     * 验证Token
     * 
     * @param token token字符串
     * @return 解码后的JWT
     */
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Token验证失败", e);
        }
    }

    /**
     * 从Token中获取用户ID
     * 
     * @param token token字符串
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userId").asInt();
    }

    /**
     * 从Token中获取用户类型
     * 
     * @param token token字符串
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("userType").asString();
    }

    /**
     * 从Token中获取角色
     * 
     * @param token token字符串
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("role").asString();
    }

    /**
     * 从Token中获取商家ID
     * 
     * @param token token字符串
     * @return 商家ID
     */
    public String getMerchantIdFromToken(String token) {
        DecodedJWT decodedJWT = verifyToken(token);
        return decodedJWT.getClaim("merchantId").asString();
    }

    /**
     * 检查Token是否过期
     * 
     * @param token token字符串
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = verifyToken(token);
            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
