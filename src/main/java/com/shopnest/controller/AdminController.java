package com.shopnest.controller;

import com.shopnest.entity.AuditLog;
import com.shopnest.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<AuditLog> logs = auditLogRepository.findAllByOrderByPerformedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", logs.getContent(),
            "totalElements", logs.getTotalElements()
        ));
    }
}
