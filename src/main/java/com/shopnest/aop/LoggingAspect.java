package com.shopnest.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * LOGGING ASPECT — Cross-cutting concern
 * Automatically logs all method calls in the service layer.
 * No need to add log statements in every service method.
 *
 * Interview talking point:
 * "I used Spring AOP to implement centralized logging across all service classes
 *  using @Around advice and ProceedingJoinPoint — without modifying business logic."
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Pointcut for all service layer methods
     */
    @Pointcut("execution(* com.shopnest.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * Around advice: logs method entry, arguments, and exit
     */
    @Around("serviceLayer()")
    public Object logMethodCall(ProceedingJoinPoint pjp) throws Throwable {
        String className = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();

        log.debug("[{}] >> {}() called with args: {}",
            className, methodName, Arrays.toString(pjp.getArgs()));

        Object result = pjp.proceed();

        log.debug("[{}] << {}() completed", className, methodName);
        return result;
    }

    /**
     * After throwing advice: logs exceptions with full details
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logException(JoinPoint jp, Exception ex) {
        String className = jp.getTarget().getClass().getSimpleName();
        String methodName = jp.getSignature().getName();
        log.error("[{}] Exception in {}(): {} - {}",
            className, methodName, ex.getClass().getSimpleName(), ex.getMessage());
    }
}
