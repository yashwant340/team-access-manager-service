package com.example.accessManager.service.impl;

import com.example.accessManager.entity.AuditTrail;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.repository.AuditTrailRepository;
import com.example.accessManager.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository  auditTrailRepository;

    @Override
    public void addAuditEntry(ActionType actionType, String action, String actor, EntityType entityType, Long id){
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setActionType(actionType);
        auditTrail.setActor(actor);
        auditTrail.setAction(action);
        auditTrail.setUpdatedDate(new Date());

        switch (entityType){
            case TEAM:
                auditTrail.setTeamId(id);
                break;
            case USER:
                auditTrail.setUserId(id);
                break;
            case USER_ACCESS:
                auditTrail.setUserAccessControlId(id);
                break;
            case TEAM_ACCESS:
                auditTrail.setTeamAccessControlId(id);
                break;
            default:
                break;
        }

        auditTrailRepository.save(auditTrail);
    }
}
