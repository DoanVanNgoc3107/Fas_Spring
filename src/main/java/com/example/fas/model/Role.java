package com.example.fas.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name = "`roles`")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;



}