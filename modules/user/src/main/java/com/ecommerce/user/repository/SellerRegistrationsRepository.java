package com.ecommerce.user.repository;

import com.ecommerce.user.domain.SellerRegistrations;
import com.ecommerce.user.enums.RegistrationStatus;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SellerRegistrationsRepository extends JpaRepository<SellerRegistrations, Long> {
    Optional<SellerRegistrations> findByUser_Id(Long id);

    boolean existsByUser_IdAndStatus(Long id, RegistrationStatus registrationStatus);

    Optional<SellerRegistrations> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    List<SellerRegistrations> findByStatus(RegistrationStatus status);
}
