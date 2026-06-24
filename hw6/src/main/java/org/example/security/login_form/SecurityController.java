package org.example.security.login_form;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SecurityController {
    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public method";
    }

    @GetMapping("/me")
    public CurrentUser currentUser(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return new CurrentUser(authentication.getName(), roles);
    }

    @GetMapping(value = "/user", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @PreAuthorize("hasRole('USER')")
    public String userEndpoint() {
        return "\u0412\u044b \u0432\u043e\u0448\u043b\u0438 \u043a\u0430\u043a USER.";
    }

    @GetMapping(value = "/admin", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "\u0412\u044b \u0432\u043e\u0448\u043b\u0438 \u043a\u0430\u043a ADMIN.";
    }

    @GetMapping(value = "/user-or-admin", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String userOrAdminEndpoint() {
        return "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0434\u043b\u044f USER \u0438\u043b\u0438 ADMIN.";
    }

    public record CurrentUser(String username, List<String> roles) {
    }
}
