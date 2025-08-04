package com.example.accessManager.service;

import com.example.accessManager.dto.AuditDTO;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuditTrailService {
    void addAuditEntry(ActionType actionType, String action, String actor, EntityType entityType, Long id);


    List<AuditDTO> getAuditLogs(Long id, String type) throws NotFoundException;
}
