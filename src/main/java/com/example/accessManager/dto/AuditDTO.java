package com.example.accessManager.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditDTO {
    private String auditDescription;
    private String actor;
    private String date;
}
