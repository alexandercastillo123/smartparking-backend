package com.smartparking.Smartparking.config;

import com.smartparking.Smartparking.entity.iam.User;
import com.smartparking.Smartparking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        String userId = "ad9760e9-b0ac-432d-ac1a-9e29219f7d4a"; // ID del token que falla
        
        if (userRepository.findById(userId).isEmpty()) {
            User user = new User();
            user.setUserId(userId);
            user.setEmail("demo@smartparking.com");
            user.setPasswordHash("hashed_password_demo"); // No se usará para login
            user.setRole(User.Role.university_member);
            user.setStatus(User.Status.active);
            user.setCreatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            System.out.println("✅ Usuario demo restaurado para evitar 404: " + userId);
        }
    }
}
