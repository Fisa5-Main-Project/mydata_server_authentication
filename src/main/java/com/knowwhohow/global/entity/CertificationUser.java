package com.knowwhohow.global.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "certification_user")
public class CertificationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 이름

    @Column(nullable = false)
    private String gender; // 성별 (M/F)

    @Column(nullable = false)
    private LocalDate birthDate; // 생년월일

    @Column(nullable = false)
    private String carrier; // 통신사

    @Column(nullable = false)
    private String phoneNumber; // 전화번호

    @Column(nullable = false, unique = true)
    private String ci;

    public CertificationUser(String name, String gender, LocalDate birthDate, String carrier, String phoneNumber, String ci) {
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.carrier = carrier;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
    }
}
