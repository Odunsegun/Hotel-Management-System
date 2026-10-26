package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, org.springframework.core.env.Environment environment) throws Exception {
        boolean demo = environment.acceptsProfiles(org.springframework.core.env.Profiles.of("demo"));
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**").permitAll()
                        .requestMatchers("/h2-console/**").access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(demo))
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/rooms/**", "/services/**", "/customers/delete/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder,
            @org.springframework.beans.factory.annotation.Value("${hotel.admin-password}") String adminPassword,
            @org.springframework.beans.factory.annotation.Value("${hotel.staff-password}") String staffPassword) {
        if (adminPassword.isBlank() || staffPassword.isBlank()) {
            throw new IllegalArgumentException("Configure non-empty hotel account passwords.");
        }
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        UserDetails staff = User.builder()
                .username("staff")
                .password(passwordEncoder.encode(staffPassword))
                .roles("STAFF")
                .build();

        return new InMemoryUserDetailsManager(admin, staff);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
