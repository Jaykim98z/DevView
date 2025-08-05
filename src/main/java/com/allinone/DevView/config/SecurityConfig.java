package com.allinone.DevView.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 비밀번호 암호화 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // stateless한 rest api를 개발할 것이므로 csrf 방어는 일단 비활성화
                .csrf(csrf -> csrf.disable())

                // form-login, http-basic 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 요청 경로별 인가 설정
                .authorizeHttpRequests(auth -> auth
                        // 개발 단계에서 모든 요청 허용
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
