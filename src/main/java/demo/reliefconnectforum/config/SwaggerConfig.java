package demo.reliefconnectforum.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI reliefConnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Relief Connect API")
                        .description("API cho hệ thống cầu cứu & gây quỹ vùng lũ - version 2")
                        .version("v2.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Tài liệu chi tiết dự án")
                        .url("https://github.com/LouisVie61/ReliefConnectForum"));
    }
}
