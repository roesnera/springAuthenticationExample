package com.example.authenticationpractice2.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // we need this method to lookup users in our db,
    // so we write the signature here to be implemented by Spring
    Optional<User> findByEmail(String email);
}
