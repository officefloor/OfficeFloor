package net.officefloor.spring.starter.rest.jwt;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractJwtVerification extends AbstractMockMvcVerification {

    // ── Item 1: No token → 401 Unauthorized ──────────────────────────────────

    @Test
    public void jwtUnauthenticated() throws Exception {
        this.mvc.perform(get(getPath("/protected"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── Item 2: Valid JWT → 200 with subject as response body ────────────────

    @Test
    public void jwtAuthenticated() throws Exception {
        this.mvc.perform(get(getPath("/protected"))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(j -> j.subject("jwt-user"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("jwt-user")));
    }

    // ── Item 3: JWT with required role → 200 ─────────────────────────────────

    @Test
    public void jwtAdminRole_access() throws Exception {
        this.mvc.perform(get(getPath("/admin"))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("admin-accessed")));
    }

    // ── Item 4: JWT without required role → 403 Forbidden ────────────────────

    @Test
    public void jwtAdminRole_denied() throws Exception {
        this.mvc.perform(get(getPath("/admin"))
                        .accept(MediaType.APPLICATION_JSON)
                        .with(jwt()))
                .andExpect(status().isForbidden());
    }
}
