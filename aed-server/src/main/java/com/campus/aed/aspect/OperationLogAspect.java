package com.campus.aed.aspect;

import com.campus.aed.entity.OperationLog;
import com.campus.aed.mapper.OperationLogMapper;
import com.campus.aed.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志切面: 自动记录所有写操作(非 GET 请求)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper;

    @Pointcut("execution(public * com.campus.aed.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        // 只记录写操作
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            try {
                saveLog(joinPoint, request);
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
        return joinPoint.proceed();
    }

    private void saveLog(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        OperationLog operationLog = new OperationLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            operationLog.setUserId(loginUser.getId());
            operationLog.setUsername(loginUser.getUsername());
        }
        operationLog.setOperation(joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName());
        operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
        operationLog.setParams(buildParams(joinPoint.getArgs()));
        String forwardedIp = request.getHeader("X-Forwarded-For");
        operationLog.setIp(StringUtils.hasText(forwardedIp) ? forwardedIp : request.getRemoteAddr());
        operationLogMapper.insert(operationLog);
    }

    /** 序列化请求参数(排除请求响应对象与文件, 截断至1000字符) */
    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof ServletRequest || arg instanceof ServletResponse
                    || arg instanceof MultipartFile || arg instanceof BindingResult) {
                continue;
            }
            filtered.add(arg);
        }
        try {
            String json = objectMapper.writeValueAsString(filtered);
            return json.length() > 1000 ? json.substring(0, 1000) : json;
        } catch (Exception e) {
            return "";
        }
    }
}
