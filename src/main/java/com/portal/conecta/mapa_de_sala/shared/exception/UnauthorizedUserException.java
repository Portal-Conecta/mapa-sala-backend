package com.portal.conecta.mapa_de_sala.shared.exception;

public class UnauthorizedUserException extends RuntimeException {

    public UnauthorizedUserException(String message) {
      super(message);
    }
  
    public UnauthorizedUserException() {
      super("User is not authorized.");
    }
  }