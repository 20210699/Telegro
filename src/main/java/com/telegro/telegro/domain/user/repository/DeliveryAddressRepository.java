package com.telegro.telegro.domain.user.repository;

import com.telegro.telegro.domain.user.entity.DeliveryAddress;
import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    Optional<DeliveryAddress> findByUserAndRecipientNameAndAddressAndAddressDetailAndZipcode(User user, String recipientName,String address, String addressDetail, String zipcode);
}
