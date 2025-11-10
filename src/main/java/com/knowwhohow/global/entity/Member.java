package com.knowwhohow.global.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ci;

    @Column(nullable = false)
    private String roles;

    public Member(String ci, String roles) {
        this.ci = ci;
        this.roles = roles;
    }
}
