package com.smartcompany.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.smartcompany.model.User;

public interface UserRepository extends JpaRepository<User, String>,
                                        JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    void deleteByUsername(String username);
}