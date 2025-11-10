package com.knowwhohow.repository;

import com.knowwhohow.global.entity.CertificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationUserRepository extends JpaRepository<CertificationUser, Long> {

    @Query("SELECT cu.ci FROM CertificationUser cu WHERE cu.name = :name AND cu.phoneNumber = :phone AND cu.carrier = :carrier")
    String findCiByNameAndPhoneNumberAndCarrier(@Param("name") String name, @Param("phone") String phone, @Param("carrier") String carrier);
}
