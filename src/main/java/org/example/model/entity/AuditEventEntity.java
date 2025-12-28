package org.example.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.model.dto.AuditEvent;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "audit_events",
        indexes = {
                @Index(name="idx_audit_ts", columnList="ts"),
                @Index(name="idx_audit_type_ts", columnList="eventType, ts"),
                @Index(name="idx_audit_actor_ts", columnList="actor, ts"),
                @Index(name="idx_audit_resource", columnList="resourceType, resourceId")
        })
public class AuditEventEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Instant ts;

    @Column(nullable=false, length=64)
    private String eventType;

    @Column(nullable=false, length=16)
    private String severity; // INFO/WARN/ERROR

    @Column(length=128)
    private String actor;

    @Column(length=64)
    private String ip;

    @Column(length=512)
    private String userAgent;

    @Column(length=64)
    private String requestId;

    @Column(length=64)
    private String resourceType;

    @Column(length=128)
    private String resourceId;

    @Column(length=16)
    private String outcome; // SUCCESS/FAILURE

    protected AuditEventEntity() {}

    public static AuditEventEntity from(AuditEvent e) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.ts = e.ts();
        entity.eventType = e.eventType();
        entity.severity = e.severity();
        entity.actor = e.actor();
        entity.ip = e.ip();
        entity.userAgent = e.userAgent();
        entity.requestId = e.requestId();
        entity.resourceType = e.resourceType();
        return entity;
    }
}
