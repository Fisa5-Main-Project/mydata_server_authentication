package com.knowwhohow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestSmsResponseDTO {
    private String verificationId;
    private String authCode;
}
