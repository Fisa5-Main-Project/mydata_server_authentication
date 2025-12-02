package com.knowwhohow.global.entity;

//import com.knowwhohow.global.config.EncryptConverter;
import com.knowwhohow.global.config.EncryptConverter;
import com.knowwhohow.global.config.LocalDateEncryptConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "certification_user", indexes = {
        @Index(name = "idx_cert_user_name_hash", columnList = "nameHash"),
        @Index(name = "idx_cert_user_phone_hash", columnList = "phoneNumberHash")
})
public class CertificationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = EncryptConverter.class)
    private String name; // 이름

    @Column(nullable = false)
    private String nameHash;

    @Column(nullable = false)
    private String gender; // 성별 (M/F)

    @Column(nullable = false)
//    @Convert(converter = LocalDateEncryptConverter.class)
    private LocalDate birthDate; // 생년월일

    @Column(nullable = false)
    private String carrier; // 통신사

    @Column(nullable = false)
    @Convert(converter = EncryptConverter.class)
    private String phoneNumber; // 전화번호

    @Column(nullable = false)
    private String phoneNumberHash;

    @Column(nullable = false, unique = true)
    @Convert(converter = EncryptConverter.class)
    private String ci;

    public CertificationUser(String name, String nameHash, String gender, LocalDate birthDate, String carrier, String phoneNumber, String ci) {
        this.name = name;
        this.nameHash = nameHash;
        this.gender = gender;
        this.birthDate = birthDate;
        this.carrier = carrier;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
    }
}
