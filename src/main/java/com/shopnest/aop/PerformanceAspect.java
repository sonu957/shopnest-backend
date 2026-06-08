package com.shopnest.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * PERFORMANCE MONITORING ASPECT
 * Tracks execution time of all service methods.
 * Warns when any method exceeds 2 seconds.
 *
 * Interview talking point:
 * "I added a separate PerformanceAspect that measures execution time
 *  using System.currentTimeMillis() and logs a warning for slow methods —
 *  a classic AOP cross-cutting concern that keeps performance monitoring
 *  completely separate from business logic."
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    private static final long SLOW_THRESHOLD_MS = 2000;

    @Pointcut("execution(* com.shopnest.service.*.*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object trackPerformance(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = pjp.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        String method = pjp.getTarget().getClass().getSimpleName()
                + "." + pjp.getSignature().getName();

        if (executionTime > SLOW_THRESHOLD_MS) {
            log.warn("SLOW METHOD DETECTED: {} took {}ms (threshold: {}ms)",
                method, executionTime, SLOW_THRESHOLD_MS);
        } else {
            log.debug("PERFORMANCE: {} executed in {}ms", method, executionTime);
        }

        return result;
    }
}
