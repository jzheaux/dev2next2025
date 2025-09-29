package io.jzheaux.spring.cleaning.dto;

import java.util.UUID;

public record UserDTO(UUID id, String name, String password, String email, int age) {
}
