package com.shopnest.aop;

import com.shopnest.entity.AuditLog;
import com.shopnest.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * AUDIT TRAIL ASPECT
 * Automatically records sensitive business actions to the audit_log table.
 * No audit code in business logic — completely separated.
 *
 * Interview talking point:
 * "I used @AfterReturning advice to capture successful order placements,
 *  product deletions, and status changes — storing them in an audit_log
 *  table without touching the service methods themselves. This is a great
 *  example of separation of concerns with AOP."
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning("execution(* com.shopnest.service.OrderService.placeOrder(..))")
    public void auditOrderPlaced(JoinPoint jp) {
        saveAuditLog("ORDER_PLACED", "Order placed by user", getCurrentUser());
    }

    @AfterReturning("execution(* com.shopnest.service.OrderService.updateOrderStatus(..))")
    public void auditOrderStatusUpdate(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String details = "Order ID: " + (args.length > 0 ? args[0] : "?")
                       + " | New Status: " + (args.length > 1 ? args[1] : "?");
        saveAuditLog("ORDER_STATUS_UPDATED", details, getCurrentUser());
    }

    @AfterReturning("execution(* com.shopnest.service.ProductService.deleteProduct(..))")
    public void auditProductDeleted(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String details = "Product ID: " + (args.length > 0 ? args[0] : "?");
        saveAuditLog("PRODUCT_DELETED", details, getCurrentUser());
    }

    @AfterReturning("execution(* com.shopnest.service.AuthService.register(..))")
    public void auditUserRegistered(JoinPoint jp) {
        saveAuditLog("USER_REGISTERED", "New user registered", "SYSTEM");
    }

    private void saveAuditLog(String action, String details, String performedBy) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(action)
                .details(details)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();
            auditLogRepository.save(auditLog);
            log.info("AUDIT: {} by {}", action, performedBy);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "ANONYMOUS";
    }
}
