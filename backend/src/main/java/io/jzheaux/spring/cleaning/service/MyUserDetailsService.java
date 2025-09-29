package io.jzheaux.spring.cleaning.service;

import io.jzheaux.spring.cleaning.exceptions.NotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserService users;

    /**
     * Constructs the service
     *
     * @param users The {@link UserService} used to query the database.
     */
    public MyUserDetailsService(UserService users) {
        this.users = users;
    }

    /**
     * Loads a user by email
     *
     * @param email The user's email address.
     * @return A Spring Security {@link UserDetails} object with roles and hashed password.
     * @throws UsernameNotFoundException If no user is found with the given email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            var user = this.users.findByEmail(email);
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.email())
                    .password(user.password())
                    .authorities("USER")
                    .build();
        } catch (NotFoundException ex) {
            throw new UsernameNotFoundException("user not found");
        }
    }
}
