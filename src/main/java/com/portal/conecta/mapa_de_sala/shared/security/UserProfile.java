package com.portal.conecta.mapa_de_sala.shared.security;

public enum UserProfile {
    APRENDIZ,
    REPRESENTANTE,
    DOCENTE,
    PERFIL_SENAI,
    PERFIL_WEG,
    ADMINISTRADOR;

    public boolean hasGlobalRoomAccess() {
        return this == PERFIL_SENAI || this == PERFIL_WEG || this == ADMINISTRADOR;
    }
}
