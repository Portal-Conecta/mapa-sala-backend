package com.portal.conecta.mapa_de_sala;

import com.portal.conecta.mapa_de_sala.shared.integration.hub.HubRoomFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Smoke test: verifica que o contexto Spring/JPA sobe sem erros de mapeamento.
 * Usa o perfil "test" (H2 em memória, Flyway desabilitado, DDL create-drop).
 */
@SpringBootTest
@ActiveProfiles("test")
class MapaDeSalaApplicationTests {

    @MockitoBean
    private HubRoomFeignClient hubRoomFeignClient;

    @Test
    void contextLoads() {
        // Se o contexto subir sem exceção, o mapeamento JPA está correto.
    }
}
