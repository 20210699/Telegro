package com.telegro.telegro.domain.user.repository;

import com.telegro.telegro.domain.user.entity.User;
import com.telegro.telegro.domain.user.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserId(String userId);
    Page<User> findByRoleNot(Role role, Pageable pageable);

    Page<User> findByRole(Role filteredBy, PageRequest pageRequest);
}
