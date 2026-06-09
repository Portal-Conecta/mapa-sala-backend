package com.portal.conecta.mapa_de_sala.shared.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, UserProfile profile) {
}
