package vn.coursebooking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/error",
                        "/",
                        "/courses",
                        "/login",
                        "/register",
                        "/css/**"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/auth/csrf"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/register"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/courses",
                        "/api/courses/*",
                        "/api/consultants",
                        "/api/slots"
                ).permitAll()

                .requestMatchers(
                        "/api/courses/**",
                        "/api/consultants/**",
                        "/api/slots/**"
                ).hasRole("ADMIN")

                .requestMatchers("/api/admin/**", "/admin/**")
                .hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .successHandler((request, response, authentication) ->
                        {
                            if (request.getHeader("Accept") != null
                                    && request.getHeader("Accept").contains("text/html")) {
                                response.sendRedirect("/courses");
                            } else {
                                response.setStatus(204);
                            }
                        }
                )
                .failureHandler((request, response, exception) -> {
                    if (request.getHeader("Accept") != null
                            && request.getHeader("Accept").contains("text/html")) {
                        response.sendRedirect("/login?error");
                    } else {
                        response.setStatus(401);
                    }
                })
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                        {
                            if (request.getHeader("Accept") != null
                                    && request.getHeader("Accept").contains("text/html")) {
                                response.sendRedirect("/courses");
                            } else {
                                response.setStatus(204);
                            }
                        }
                )
        );

        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) ->
                        {
                            if (request.getHeader("Accept") != null
                                    && request.getHeader("Accept").contains("text/html")
                                    && !request.getRequestURI().startsWith("/api/")) {
                                response.sendRedirect("/login");
                            } else {
                                response.setStatus(401);
                            }
                        }
                )
                .accessDeniedHandler((request, response, exception) ->
                        response.setStatus(403)
                )
        );

        return http.build();
    }
}