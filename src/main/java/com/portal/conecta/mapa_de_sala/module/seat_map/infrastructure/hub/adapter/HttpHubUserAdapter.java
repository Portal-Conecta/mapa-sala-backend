package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubUser;
import com.portal.conecta.mapa_de_sala.module.seat_map.domain.port.HubUserPort;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.dto.HubUserResponse;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.exception.HubIntegrationException;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubApiProperties;

@Component
@ConditionalOnProperty(prefix = "hub.api", name = "mock-enabled", havingValue = "false")
public class HttpHubUserAdapter implements HubUserPort {

    private final RestClient restClient;

    public HttpHubUserAdapter(HubApiProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.url()).build();
    }

    @Override
    public boolean existsById(UUID userId) {
        try {
            restClient.get()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Serviço de usuários do Hub indisponível.", exception);
        }
    }

    @Override
    public Optional<HubUser> findById(UUID userId) {
        try {
            HubUserResponse response = restClient.get()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .body(HubUserResponse.class);

            if (response == null) {
                return Optional.empty();
            }

            return Optional.of(new HubUser(response.id(), response.name()));
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Serviço de usuários do Hub indisponível.", exception);
        }
    }
}
