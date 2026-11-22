package com.supportdesk.support_desk.auth.repository;

import com.supportdesk.support_desk.auth.entity.Role;
import com.supportdesk.support_desk.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login — find user by their email address
    Optional<User> findByEmail(String email);

    // Used during registration — check if email is already taken
    boolean existsByEmail(String email);

    // Used by admin — get all agents for assignment dropdown
    List<User> findByRole(Role role);

    // Used by admin — get active agents only
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);
}