package com.example.shared.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Draft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
}