package net.officefloor.tutorial.springrestsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestSecurityTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void public_endpoint_no_auth_required() throws Exception {
		mvc.perform(get("/security/public"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Hello, World!\""));
	}

	@Test
	public void protected_endpoint_requires_authentication() throws Exception {
		mvc.perform(get("/security/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "daniel", roles = "USER")
	public void current_user_via_authentication_principal() throws Exception {
		mvc.perform(get("/security/me"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Hello, daniel!\""));
	}

	@Test
	@WithUserDetails("user")
	public void with_user_details_loads_real_user() throws Exception {
		mvc.perform(get("/security/me"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Hello, user!\""));
	}

	@Test
	@WithMockUser(username = "daniel", roles = {"USER", "ADMIN"})
	public void user_roles_from_granted_authorities() throws Exception {
		mvc.perform(get("/security/roles"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"ROLE_ADMIN, ROLE_USER\""));
	}

	@Test
	@WithMockUser(username = "daniel", roles = "USER")
	public void authentication_as_direct_parameter() throws Exception {
		mvc.perform(get("/security/auth"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Authenticated as: daniel\""));
	}

	@Test
	@WithMockUser(username = "daniel", roles = "USER")
	public void spring_security_bean_injected_as_parameter() throws Exception {
		mvc.perform(get("/security/bean"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Loaded: user\""));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void pre_authorize_grants_admin() throws Exception {
		mvc.perform(get("/security/preauthorize"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Admin access via @PreAuthorize\""));
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	public void pre_authorize_denies_non_admin() throws Exception {
		mvc.perform(get("/security/preauthorize"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void secured_grants_admin() throws Exception {
		mvc.perform(get("/security/secured"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Admin access via @Secured\""));
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	public void secured_denies_non_admin() throws Exception {
		mvc.perform(get("/security/secured"))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void roles_allowed_grants_admin() throws Exception {
		mvc.perform(get("/security/rolesallowed"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"Admin access via @RolesAllowed\""));
	}

	@Test
	@WithMockUser(username = "user", roles = "USER")
	public void roles_allowed_denies_non_admin() throws Exception {
		mvc.perform(get("/security/rolesallowed"))
			.andExpect(status().isForbidden());
	}
}
// END SNIPPET: tutorial
