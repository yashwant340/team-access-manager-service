package com.example.accessManager.service.impl;

import com.example.accessManager.dto.AuditDTO;
import com.example.accessManager.dto.UserAccessControlDTO;
import com.example.accessManager.entity.AuditTrail;
import com.example.accessManager.entity.TeamAccessControl;
import com.example.accessManager.entity.UserAccessControl;
import com.example.accessManager.enums.ActionType;
import com.example.accessManager.enums.EntityType;
import com.example.accessManager.exceptions.NotFoundException;
import com.example.accessManager.repository.AuditTrailRepository;
import com.example.accessManager.repository.TeamAccessControlRepository;
import com.example.accessManager.repository.UserAccessControlRepository;
import com.example.accessManager.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository  auditTrailRepository;
    private final TeamAccessControlRepository teamAccessControlRepository;
    private final UserAccessControlRepository userAccessControlRepository;

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

    @Override
    public List<AuditDTO> getAuditLogs(Long id, String type) throws NotFoundException {
        List<AuditTrail> auditTrailList = type.equals("Team") ? auditTrailRepository.findAllByTeam(id) : auditTrailRepository.findAllByUser(id);
        List<AuditDTO> auditDTOList = new ArrayList<>();
        for(AuditTrail auditTrail: auditTrailList){
            AuditDTO auditDTO = new AuditDTO();
            auditDTO.setActor(auditTrail.getActor());
            auditDTO.setDate(String.valueOf(auditTrail.getUpdatedDate()));
            switch (auditTrail.getActionType()){
                case ActionType.ADD_TEAM, ActionType.UPDATE_TEAM, ActionType.ACCESS_MODE_CHANGE, ActionType.ADD_USER:
                    auditDTO.setAuditDescription(auditTrail.getAction());
                    break;
                case ActionType.TEAM_ACCESS_CHANGE:
                    TeamAccessControl teamAccessControl = teamAccessControlRepository.findById(auditTrail.getTeamAccessControlId()).orElseThrow(() -> new NotFoundException("Access control not found with id : " + auditTrail.getTeamAccessControlId()));
                    auditDTO.setAuditDescription(auditTrail.getAction() + " to " + teamAccessControl.getFeature().getName());
                    break;
                case ActionType.USER_ACCESS_CHANGE:
                    UserAccessControl userAccessControl =userAccessControlRepository.findById(auditTrail.getUserAccessControlId()).orElseThrow(() -> new NotFoundException("User control not found with id : " + auditTrail.getUserAccessControlId()));
                    auditDTO.setAuditDescription(auditTrail.getAction() + " to " + userAccessControl.getFeature().getName());
                default:
                    break;
            }
            auditDTOList.add(auditDTO);
        }
        return auditDTOList;
    }
}
