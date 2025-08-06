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
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화

                // 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")              // 커스텀 로그인 페이지 경로
                        .defaultSuccessUrl("/mypage")     // 로그인 성공 시 이동 경로
                        .permitAll()
                )

                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/mypage/**").authenticated() // mypage는 인증 필요
                        .anyRequest().permitAll() // 나머지는 허용
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 메인 페이지
                        .permitAll()
                );

        return http.build();
    }
}
