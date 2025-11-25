package com.example.ecommerce.service;

import com.example.ecommerce.model.Role;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // Updated to accept Address
    public boolean registerNewUser(String name, String email, String password, String address) {
        if (userRepo.findByEmail(email).isEmpty()) {
            // Save with Address
            User newUser = new User(null, name, email, passwordEncoder.encode(password), address, Role.USER);
            userRepo.save(newUser);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    // Update profile method (optional but good for Profile page)
    public void updateUser(User user) {
        userRepo.save(user);
    }
}