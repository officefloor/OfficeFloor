package net.officefloor.tutorial.springrestservlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestServletTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void requestInfo() throws Exception {
		mvc.perform(get("/request/info?name=OfficeFloor")
				.header("User-Agent", "TestAgent/1.0")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=OfficeFloor")))
				.andExpect(content().string(containsString("agent=TestAgent/1.0")));
	}

	@Test
	public void directWrite() throws Exception {
		mvc.perform(get("/response/direct"))
				.andExpect(status().isOk())
				.andExpect(content().string("Written directly to servlet response"));
	}
}
// END SNIPPET: tutorial
