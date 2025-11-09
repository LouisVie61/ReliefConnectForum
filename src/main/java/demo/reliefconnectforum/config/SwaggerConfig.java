package demo.reliefconnectforum.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI reliefConnectOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("JWT token obtained from /api/auth/login or OAuth2 flow. Format: Bearer {token}")))
                .info(new Info()
                        .title("Relief Connect API")
                        .description("""
                                API cho hệ thống cầu cứu & gây quỹ vùng lũ - version 3
                                
                                ## OAuth2 Login Instructions:
                                1. Call GET /test/login-with-google to get the OAuth2 URL
                                2. Copy the `oauth2_url` from the response
                                3. Paste it in your browser's address bar
                                4. Complete Google login
                                5. Copy the JWT token from the success response
                                6. Click "Authorize" button above and paste the token
                                """)
                        .version("v2.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Tài liệu chi tiết dự án")
                        .url("https://github.com/LouisVie61/ReliefConnectForum"));
    }
}