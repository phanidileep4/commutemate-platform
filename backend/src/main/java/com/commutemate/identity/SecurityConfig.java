package com.commutemate.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean SecurityFilterChain security(HttpSecurity http, DevIdentityFilter devIdentityFilter) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterBefore(devIdentityFilter, AnonymousAuthenticationFilter.class)
        .build();
  }
}
