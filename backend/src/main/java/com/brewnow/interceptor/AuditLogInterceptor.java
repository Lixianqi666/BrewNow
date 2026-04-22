package com.brewnow.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT_LOG");
    private static final String START_TIME = "auditStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        long duration = startTime == null ? -1L : System.currentTimeMillis() - startTime;

        Object userId = request.getAttribute("userId");
        Object userType = request.getAttribute("userType");
        Object role = request.getAttribute("role");
        Object merchantId = request.getAttribute("merchantId");

        auditLog.info("method={}, uri={}, status={}, durationMs={}, userId={}, userType={}, role={}, merchantId={}, remoteAddr={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                userId,
                userType,
                role,
                merchantId,
                request.getRemoteAddr());
    }
}
