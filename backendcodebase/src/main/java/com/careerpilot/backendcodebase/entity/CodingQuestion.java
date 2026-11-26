package com.careerpilot.backendcodebase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "codingQuestion")
public class CodingQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String number;
    private String  questionName;
    private String url;
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;
    private String description;

}
