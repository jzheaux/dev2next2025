// ==== AuthController.java ====
package io.jzheaux.spring.cleaning.controller;

import io.jzheaux.spring.cleaning.dto.*;
import io.jzheaux.spring.cleaning.service.JwtUtil;
import io.jzheaux.spring.cleaning.service.RefreshTokenService;
import io.jzheaux.spring.cleaning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtUtil jwt;
    private final UserService userService;
    private final RefreshTokenService rtService;

    public AuthController(AuthenticationManager authManager, JwtUtil jwt, UserService userService, RefreshTokenService rtService) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.userService = userService;
        this.rtService = rtService;
    }

    /**
     * Authenticates a user using their email and password, and returns JWT access and refresh tokens.
     *
     * @param req The login request containing email, password, and remember-me flag.
     * @return A response containing the access token and refresh token.
     * @throws BadCredentialsException if the credentials are invalid.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthTokenDTO> login(@RequestBody AuthRequest req) {

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
        } catch (Exception ex) {
            throw new BadCredentialsException("Incorrect email or password");
        }

        UserDTO user = userService.findByEmail(req.email());

        String access  = jwt.generateToken(user, Duration.ofMinutes(15));
        String refresh = rtService.create(user.id(), req.rememberMe()); // add flag to AuthRequest

        return ResponseEntity.ok(new AuthTokenDTO(access, refresh));
    }

    /**
     * Registers a new user with the provided information and returns an access and refresh token.
     *
     * @param req The registration request containing user data.
     * @return A response containing the access token and refresh token for the new user.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthTokenDTO> register(@RequestBody CreateUserRequest req) {

        UserDTO user = userService.createUser(req);

        String access  = jwt.generateToken(user, Duration.ofMinutes(15));
        String refresh = rtService.create(user.id(), false);

        return ResponseEntity.ok(new AuthTokenDTO(access, refresh));
    }

    /**
     * Refreshes the JWT access token using a valid refresh token.
     *
     * @param body The request containing a valid refresh token.
     * @return A response with a new access token. No new refresh token is issued.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenDTO> refresh(@RequestBody RefreshTokenDTO body) {
        UUID userId = rtService.verifyAndGetUserId(body.refreshToken());

        UserDTO user = userService.getUserById(userId);          // already exists in your service
        String access = jwt.generateToken(user, Duration.ofMinutes(15));

        return ResponseEntity.ok(new AuthTokenDTO(access, null)); // no new refresh token
    }

    /**
     * Logs the user out by invalidating the given refresh token.
     *
     * @param body The request containing the refresh token to invalidate.
     * @return HTTP 200 OK response if successful.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenDTO body) {
        rtService.invalidate(body.refreshToken());
        return ResponseEntity.ok().build();
    }
}