package com.example.accessManager.dto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingRequestDTO {
    private Long id;
    private String requestedOn;
    private String pendingWith;
    private String requestType;
    private String requestStatus;
}
