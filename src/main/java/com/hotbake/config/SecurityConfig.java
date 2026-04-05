package com.hotbake.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.hotbake.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/products", "/products/**", "/search",
                                 "/images/**", "/css/**", "/js/**", "/images/**",
                                 "/error/**", "/actuator/health").permitAll()
                // Auth endpoints (public)
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                // Seller apply requires BUYER role only
                .requestMatchers("/auth/seller-apply").hasRole("BUYER")
                // Admin area
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Seller area
                .requestMatchers("/seller/**").hasRole("SELLER")
                // Buyer area
                .requestMatchers("/buyer/**", "/cart/**", "/checkout/**",
                                 "/reviews/**", "/questions/ask").hasRole("BUYER")
                // Seller answering questions
                .requestMatchers("/questions/answer/**").hasRole("SELLER")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    var authorities = authentication.getAuthorities();
                    String redirect = "/";
                    for (var auth2 : authorities) {
                        if (auth2.getAuthority().equals("ROLE_ADMIN")) { redirect = "/admin/dashboard"; break; }
                        if (auth2.getAuthority().equals("ROLE_SELLER")) { redirect = "/seller/dashboard"; break; }
                    }
                    response.sendRedirect(redirect);
                })
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
