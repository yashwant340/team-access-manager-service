package com.example.accessManager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Feature")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;


}
