package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(columnDefinition = "TEXT")
    private String details;

    @PrePersist
    public void prePersist() {
        if (this.performedAt == null) this.performedAt = LocalDateTime.now();
    }
}
