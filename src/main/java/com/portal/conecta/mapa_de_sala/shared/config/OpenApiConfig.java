package com.portal.conecta.mapa_de_sala.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Portal Documental - Assessoria Imobiliária")
				.description("""
					Sistema de gestão documental para assessorias imobiliárias.
					Fornece funcionalidades completas de autenticação, gerenciamento de usuários,
					clientes, processos e propriedades imobiliárias com integração de controle de auditoria.
					""")
				.version("1.0.0")
				.contact(new Contact()
					.name("Suporte - Documental Assessoria")
					.email("jonathanuberdev@gmail.com")
					.url("https://documentalbrasil.com.br"))
				.license(new License()
					.name("Licença Proprietária")
					.url("https://documentalbrasil.com.br")))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("Bearer Authentication",
					new SecurityScheme()
						.type(Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Token JWT para autenticação de requisições. Obtenha o token através do endpoint /auth/login.")));
	}
}