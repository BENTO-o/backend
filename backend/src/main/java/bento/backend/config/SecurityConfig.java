package bento.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 접근 권한 설정
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/oauth2/authorization/**").permitAll()  // OAuth2 인증 요청 허용
                        .requestMatchers("/oauth/**").authenticated()  // OAuth2 인증 성공 후 토큰 발급 엔드포인트는 인증 필요
                        .anyRequest().permitAll()  // 그 외 모든 요청은 허용
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/oauth/login/success")  // 로그인 성공 후 토큰 반환 엔드포인트
                        .failureUrl("/oauth/login/failure")  // 로그인 실패 시 엔드포인트
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
