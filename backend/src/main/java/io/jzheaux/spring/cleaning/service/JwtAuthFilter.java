package io.jzheaux.spring.cleaning.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication filter that intercepts every request once to extract and validate a JWT token.

 * If a valid token is found in the "Authorization" header, it sets up the {@link SecurityContextHolder}
 * with a {@link UsernamePasswordAuthenticationToken} derived from the user details.
*/
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    /**
     * Constructs the filter with dependencies for token validation and user loading.
     *
     * @param jwtUtil Utility for extracting and validating JWTs.
     * @param userDetailsService Service to load user details from the database.
     */
    public JwtAuthFilter(JwtUtil jwtUtil, MyUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepts the HTTP request and attempts to extract and validate a JWT token from the Authorization header.
     * If the token is valid and the user is not already authenticated, this method populates the
     * {@link SecurityContextHolder} with an authenticated principal.
     *
     * @param request  The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param filterChain The remaining filters in the chain.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No JWT found — continue with the filter chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // remove "Bearer "

        try {
            final String email = jwtUtil.extractEmail(token); // "sub" claim

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            logger.warn("Invalid JWT: {}", e);
        }

        filterChain.doFilter(request, response);
    }
}
