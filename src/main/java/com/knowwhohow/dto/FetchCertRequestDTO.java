package com.knowwhohow.dto;

public record FetchCertRequestDTO(
        String name,
        String rrn1,
        String rrn2,
        String carrier,
        String phone,
        String smsCode
) { }
