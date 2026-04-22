package com.brewnow.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.brewnow.common.Result;
import com.brewnow.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, "未提供有效的认证令牌");
            return false;
        }

        try {
            String token = authHeader.substring(7);

            // 验证token
            jwtUtil.verifyToken(token);

            // 提取用户信息并放入request属性中
            Integer userId = jwtUtil.getUserIdFromToken(token);
            String userType = jwtUtil.getUserTypeFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            String merchantId = jwtUtil.getMerchantIdFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("userType", userType);
            request.setAttribute("role", role);
            request.setAttribute("merchantId", merchantId);
            request.setAttribute("token", token);

            return true;
        } catch (Exception e) {
            writeErrorResponse(response, "认证令牌无效或已过期");
            return false;
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Result<Void> result = Result.unauthorized(message);
        String jsonResponse = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResponse);
    }
}