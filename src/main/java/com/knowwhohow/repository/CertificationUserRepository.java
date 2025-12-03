package com.knowwhohow.repository;

import com.knowwhohow.global.entity.CertificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificationUserRepository extends JpaRepository<CertificationUser, Long> {

    @Query("SELECT cu.ci FROM CertificationUser cu WHERE cu.nameHash = :nameHash AND cu.phoneNumberHash = :phoneNumberHash AND cu.carrier = :carrier")
    String findCiByNameAndPhoneNumberAndCarrier(@Param("nameHash") String nameHash,
                  @Param("phoneNumberHash") String phoneNumberHash,
                  @Param("carrier") String carrier);

    Optional<CertificationUser> findByNameAndPhoneNumber(String name, String phoneNumber);
}
