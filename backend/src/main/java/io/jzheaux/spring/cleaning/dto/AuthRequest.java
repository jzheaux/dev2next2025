package io.jzheaux.spring.cleaning.dto;

public record AuthRequest(String email, String password,boolean rememberMe) {
}
