package com.example.accessManager.entity;

import com.example.accessManager.enums.AccessMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    private String email;

    private String role;

    private Long empId;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    private Team team;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "access_mode")
    @Enumerated(EnumType.STRING)
    private AccessMode accessMode;

    @Column(name = "created_date")
    private Date createdDate;
}
