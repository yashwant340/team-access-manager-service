package com.example.accessManager.entity;

import com.example.accessManager.enums.PendingRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "access_request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", referencedColumnName = "id")
    private Feature feature;

    @Column(name = "requested_on")
    private Date requestedOn;

    @Column(name = "requested_type")
    private String requestType;


    @Column(name = "requested_status")
    @Enumerated(EnumType.STRING)
    private PendingRequestStatus requestStatus;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
