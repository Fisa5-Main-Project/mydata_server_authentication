package com.knowwhohow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SmsCertificationRequestDTO {
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "주민등록번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{6}-\\d{1}$", message = "주민등록번호는 123456-1 형식이어야 합니다.")
    private String rrn;

    @NotBlank(message = "통신사는 필수 입력 항목입니다.")
    private String telecom;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "유효하지 않은 전화번호 형식입니다.")
    private String phoneNumber;
}

