package com.hotbake.service;

import com.hotbake.dto.UserRegistrationDto;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.model.Role;
import com.hotbake.model.User;
import com.hotbake.repository.RoleRepository;
import com.hotbake.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private UserRegistrationDto dto;
    private Role buyerRole;

    @BeforeEach
    void setUp() {
        dto = new UserRegistrationDto();
        dto.setFirstName("Rahim");
        dto.setLastName("Uddin");
        dto.setEmail("rahim@test.com");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setPhone("01700000001");

        buyerRole = new Role("ROLE_BUYER");
    }

    @Test
    void registerBuyer_validDto_savesUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_BUYER")).thenReturn(Optional.of(buyerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.registerBuyer(dto);

        assertThat(result.getEmail()).isEqualTo("rahim@test.com");
        assertThat(result.getFirstName()).isEqualTo("Rahim");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerBuyer_duplicateEmail_throwsIllegalArgument() {
        when(userRepository.existsByEmail("rahim@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerBuyer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void registerBuyer_passwordMismatch_throwsIllegalArgument() {
        dto.setConfirmPassword("differentPassword");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.registerBuyer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        User user = new User();
        user.setEmail("rahim@test.com");
        when(userRepository.findByEmail("rahim@test.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("rahim@test.com");

        assertThat(result.getEmail()).isEqualTo("rahim@test.com");
    }

    @Test
    void banUser_setsUserBanned() {
        User user = new User();
        user.setId(1L);
        user.setBanned(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.banUser(1L);

        assertThat(user.isBanned()).isTrue();
        verify(userRepository).save(user);
    }
}
