package com.example.accessManager.entity;

import com.example.accessManager.enums.ActionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "audit_trail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "action_description")
    private String action;

    @Column(name = "team_access_control_id")
    private Long teamAccessControlId;

    @Column(name = "user_access_control_id")
    private Long userAccessControlId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "actor")
    private String actor;

    @Column(name = "updated_date")
    private Date updatedDate;

}
