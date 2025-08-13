package com.example.accessManager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "login_request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String empId;

    private String team;

    private String role;


    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "updated_date")
    private Date updatedDate;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
