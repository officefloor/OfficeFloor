package net.officefloor.tutorial.springrestcors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestCorsTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void compositionCors_allowedOrigin() throws Exception {
		mvc.perform(get("/cors/composition")
				.header("Origin", "https://example.com"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
				.andExpect(content().string(equalTo("Hello from composition CORS endpoint")));
	}

	@Test
	public void compositionCors_rejectedOrigin() throws Exception {
		mvc.perform(get("/cors/composition")
				.header("Origin", "https://evil.com"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@Test
	public void annotationCors_allowedOrigin() throws Exception {
		mvc.perform(get("/cors/annotation")
				.header("Origin", "https://example.com"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
				.andExpect(content().string(equalTo("Hello from annotation CORS endpoint")));
	}

	@Test
	public void annotationCors_rejectedOrigin() throws Exception {
		mvc.perform(get("/cors/annotation")
				.header("Origin", "https://evil.com"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}
}
// END SNIPPET: tutorial
