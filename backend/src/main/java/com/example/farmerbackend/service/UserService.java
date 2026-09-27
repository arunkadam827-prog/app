package com.example.farmerbackend.service;

import com.example.farmerbackend.dto.GoogleLoginRequest;
import com.example.farmerbackend.dto.LoginRequest;
import com.example.farmerbackend.dto.RegisterRequest;
import com.example.farmerbackend.entity.User;
import com.example.farmerbackend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        java.lang.String type = request.getUserType();
        if (type == null || type.trim().isEmpty()) {
            type = "BUYER";
        }
        user.setUserType(type.toUpperCase());

        user.setAddress(request.getAddress());
        user.setCity(request.getCity());

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        java.util.Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    public User getUser(java.lang.Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Updates an existing user profile with non-null and non-empty values.
     */
    public User updateUser(java.lang.Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getFullName() != null && !updatedUser.getFullName().isEmpty()) {
            user.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
            user.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getAddress() != null && !updatedUser.getAddress().isEmpty()) {
            user.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getCity() != null && !updatedUser.getCity().isEmpty()) {
            user.setCity(updatedUser.getCity());
        }
        if (updatedUser.getProfileImage() != null && !updatedUser.getProfileImage().isEmpty()) {
            user.setProfileImage(updatedUser.getProfileImage());
        }

        return userRepository.save(user);
    }

    /**
     * Verifies a Google ID token, then logs in the matching account or creates a
     * new one on first use. The account is keyed by the verified Google email.
     */
    public User googleLogin(GoogleLoginRequest request) {
        GoogleTokenVerifier.GoogleUser googleUser =
                googleTokenVerifier.verify(request.getIdToken());

        return userRepository.findByEmail(googleUser.email())
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(googleUser.email());
                    user.setFullName(
                            googleUser.name() != null && !googleUser.name().isBlank()
                                    ? googleUser.name()
                                    : googleUser.email());
                    user.setProfileImage(googleUser.picture());

                    java.lang.String type = request.getUserType();
                    if (type == null || type.trim().isEmpty()) {
                        type = "BUYER";
                    }
                    user.setUserType(type.toUpperCase());

                    // Social accounts have no local password; store a random
                    // unusable hash so password login can never succeed.
                    user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));

                    return userRepository.save(user);
                });
    }
}