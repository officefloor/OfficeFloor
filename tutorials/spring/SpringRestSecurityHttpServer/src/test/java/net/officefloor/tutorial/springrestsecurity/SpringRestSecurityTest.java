package net.officefloor.tutorial.springrestsecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
	@WithMockUser(username = "daniel", roles = {"USER", "ADMIN"})
	public void user_roles_from_granted_authorities() throws Exception {
		mvc.perform(get("/security/roles"))
			.andExpect(status().isOk())
			.andExpect(content().json("\"ROLE_ADMIN, ROLE_USER\""));
	}
}
// END SNIPPET: tutorial
