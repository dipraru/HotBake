package com.hotbake.config;

import com.hotbake.model.Role;
import com.hotbake.model.User;
import com.hotbake.repository.RoleRepository;
import com.hotbake.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Create roles if they don't exist
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_SELLER");
        createRoleIfNotFound("ROLE_BUYER");

        // Create default admin if not present
        if (!userRepository.existsByEmail("admin@hotbake.com")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            User admin = new User();
            admin.setFirstName("HotBake");
            admin.setLastName("Admin");
            admin.setEmail("admin@hotbake.com");
            admin.setPassword(passwordEncoder.encode("Admin@1234"));
            admin.setPhone("01700000000");
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            userRepository.save(admin);

            System.out.println("========================================");
            System.out.println("  Default Admin Created:");
            System.out.println("  Email   : admin@hotbake.com");
            System.out.println("  Password: Admin@1234");
            System.out.println("========================================");
        }
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(new Role(name));
        }
    }
}
