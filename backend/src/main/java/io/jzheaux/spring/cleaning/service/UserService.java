package io.jzheaux.spring.cleaning.service;

import io.jzheaux.spring.cleaning.dto.CreateUserRequest;
import io.jzheaux.spring.cleaning.dto.UserDTO;
import io.jzheaux.spring.cleaning.exceptions.AlreadyExistsException;
import io.jzheaux.spring.cleaning.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing user accounts
 */
@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private Map<UUID, UserDTO> users = new HashMap<>();

    /**
     * Constructs the service with required dependencies.
     *
     * @param passwordEncoder     Encoder for securely storing passwords.
     */
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Returns all users
     */
    public Collection<UserDTO> getAllUsers() {
        return this.users.values();
    }

    /**
     * Finds a user by email.
     *
     * @param email Email address to search for.
     * @return UserDTO
     */
    public UserDTO findByEmail(String email) {
        for (var user : this.users.values()) {
            if (user.email().equals(email)) {
                return user;
            }
        }

        throw new NotFoundException("User not found with email: " + email);
    }

    /**
     * Creates a new user
     */
    public UserDTO createUser(CreateUserRequest request) {
        for (UserDTO user : this.users.values()) {
            if (user.email().equals(request.email())) {
                throw new AlreadyExistsException("user already exists");
            }
        }
        var encoded = this.passwordEncoder.encode(request.password());
        var user = new UserDTO(UUID.randomUUID(), request.name(), request.email(), encoded, request.age());
        this.users.put(user.id(), user);
        return user;
    }

    /**
     * Fetches a user by ID
     */
    public UserDTO getUserById(UUID id) {
        return Optional.ofNullable(this.users.get(id)).orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
    }

    /**
     * Deletes a user
     */
    public void deleteUserById(UUID id) {
        this.users.remove(id);
    }

}
