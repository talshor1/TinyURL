package org.example.services;

import org.example.model.RequestContext;
import org.example.model.dto.AuditEvent;
import org.example.model.entity.AuditEventEntity;
import org.example.repository.AuditEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditEventService {
    private final AuditEventRepository repo;

    public AuditEventService(AuditEventRepository repo) {
        this.repo = repo;
    }

    @Async
    public void emit(AuditEvent e) {
        AuditEventEntity row = AuditEventEntity.from(e); // map dto->entity
        repo.save(row);
    }

    public static AuditEvent now(String eventType, String severity, String actor,
                                 RequestContext ctx, String resourceType, String resourceId,
                                 String outcome) {
        return new AuditEvent(
                Instant.now(), eventType, severity, actor,
                ctx.ip(), ctx.userAgent(), ctx.requestId(),
                resourceType, resourceId, outcome
        );
    }
}
