package com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.adapter;

import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.exception.HubIntegrationException;
import com.portal.conecta.mapa_de_sala.module.seat_map.infrastructure.hub.properties.HubApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class HttpHubClassAdapterTest {

    private static final String HUB_URL = "http://hub-mock";

    private MockRestServiceServer mockServer;
    private HttpHubClassAdapter adapter;

    private final UUID userId = UUID.randomUUID();
    private final UUID classId = UUID.randomUUID();
    private final UUID otherClassId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        adapter = new HttpHubClassAdapter(new HubApiProperties(HUB_URL, false), builder);
    }

    @Test
    void belongsToClass_shouldReturnFalseWhenUserHasNoMemberships() {
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        boolean result = adapter.belongsToClass(userId, classId);

        assertThat(result).isFalse();
    }

    @Test
    void belongsToClass_shouldReturnTrueWhenUserHasSingleMatchingMembership() {
        String body = "[{\"id\":\"" + classId + "\"}]";
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        boolean result = adapter.belongsToClass(userId, classId);

        assertThat(result).isTrue();
    }

    @Test
    void belongsToClass_shouldReturnTrueWhenClassIsAmongMultipleMemberships() {
        String body = "[{\"id\":\"" + otherClassId + "\"},{\"id\":\"" + classId + "\"}]";
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        boolean result = adapter.belongsToClass(userId, classId);

        assertThat(result).isTrue();
    }

    @Test
    void belongsToClass_shouldReturnFalseWhenClassIsNotAmongMultipleMemberships() {
        UUID unrelatedClassId = UUID.randomUUID();
        String body = "[{\"id\":\"" + otherClassId + "\"},{\"id\":\"" + unrelatedClassId + "\"}]";
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        boolean result = adapter.belongsToClass(userId, classId);

        assertThat(result).isFalse();
    }

    @Test
    void getClassIdsForUser_shouldReturnEmptyListWhenUserHasNoMemberships() {
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var result = adapter.getClassIdsForUser(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void getClassIdsForUser_shouldReturnSingleClassId() {
        String body = "[{\"id\":\"" + classId + "\"}]";
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        var result = adapter.getClassIdsForUser(userId);

        assertThat(result).containsExactly(classId);
    }

    @Test
    void getClassIdsForUser_shouldReturnAllClassIdsWhenUserHasMultipleMemberships() {
        String body = "[{\"id\":\"" + classId + "\"},{\"id\":\"" + otherClassId + "\"}]";
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        var result = adapter.getClassIdsForUser(userId);

        assertThat(result).containsExactlyInAnyOrder(classId, otherClassId);
    }

    @Test
    void fetchMemberships_shouldReturnEmptyListWhenHubReturns404() {
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withStatus(NOT_FOUND));

        var result = adapter.getClassIdsForUser(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchMemberships_shouldThrowHubIntegrationExceptionWhenHubIsUnavailable() {
        mockServer.expect(requestTo(HUB_URL + "/users/" + userId + "/class"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.getClassIdsForUser(userId))
                .isInstanceOf(HubIntegrationException.class);
    }
}