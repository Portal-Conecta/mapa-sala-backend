package com.portal.conecta.mapa_de_sala;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.portal.conecta.mapa_de_sala.shared.integration.hub")
public class MapaDeSalaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MapaDeSalaApplication.class, args);
    }
}