package io.jzheaux.spring.cleaning.dto;

import java.math.BigDecimal;

//This is sent from frontend
public record CreateUserRequest(String name, String email, String password, int age) {
}
