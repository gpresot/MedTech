package com.example.MedTech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/home", "/register", "/register/**",
                 "/css/**", "/images/**", "/error").permitAll().requestMatchers("/perfil/paciente").hasRole("PACIENTE")
  .requestMatchers("/perfil/doutor").hasRole("DOUTOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {
                boolean doutor = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_DOUTOR"));
                response.sendRedirect(doutor ? "/perfil/doutor" : "/perfil/paciente");
            })
            .failureUrl("/login?error")
            .permitAll()
)
            
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        return new InMemoryUserDetailsManager();
    }
}