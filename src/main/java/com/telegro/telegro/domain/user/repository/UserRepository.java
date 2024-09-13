package com.telegro.telegro.domain.user.repository;

import com.telegro.telegro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserId(String userId);
}
