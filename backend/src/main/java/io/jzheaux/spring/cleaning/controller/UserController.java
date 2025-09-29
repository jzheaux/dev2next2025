package io.jzheaux.spring.cleaning.controller;

import io.jzheaux.spring.cleaning.dto.CreateUserRequest;
import io.jzheaux.spring.cleaning.dto.UserDTO;
import io.jzheaux.spring.cleaning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

/**
 * REST controller for managing users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the controller with the required {@link UserService}.
     *
     * @param userService The service handling user-related operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a list of all users.
     *
     * @return A list of {@link UserDTO} objects.
     */
    @GetMapping
    public Collection<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Creates a new user from the provided request data.
     *
     * @param req The request body containing user information.
     * @return The created {@link UserDTO}, wrapped in a 201 Created response.
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest req) {
        UserDTO created = userService.createUser(req);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The corresponding {@link UserDTO}.
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return HTTP 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
