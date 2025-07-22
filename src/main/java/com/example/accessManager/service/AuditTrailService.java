package com.example.accessManager.service;

import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import org.springframework.stereotype.Service;

@Service
public interface AuditTrailService {
    void addAuditEntry(ActionType actionType, String action, String actor, EntityType entityType, Long id);
}
