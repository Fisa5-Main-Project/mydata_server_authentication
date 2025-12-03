package com.knowwhohow.service;

import com.knowwhohow.dto.FetchCertRequestDTO;
import com.knowwhohow.dto.FetchCertResponseDTO;
import com.knowwhohow.global.config.AesUtil;
import com.knowwhohow.global.config.HashUtil;
import com.knowwhohow.global.exception.CustomException;
import com.knowwhohow.global.exception.ErrorCode;
import com.knowwhohow.repository.CertificationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertAuthService {
    private final CertificationUserRepository certificationUserRepository;
    private final HashUtil hashUtil;

    public FetchCertResponseDTO fetchAndSaveCertUser(FetchCertRequestDTO request) {
        String nameHash = hashUtil.generateHash(request.name());
        String phoneHash = hashUtil.generateHash(request.phone());

        String originCi = certificationUserRepository.findCiByNameAndPhoneNumberAndCarrier(nameHash, phoneHash, request.carrier());

        String ci = AesUtil.encrypt(originCi);

        if(ci == null) {
            throw new CustomException(ErrorCode.NOT_USER);
        }

        return new FetchCertResponseDTO(ci, "본인인증서");
    }
}
