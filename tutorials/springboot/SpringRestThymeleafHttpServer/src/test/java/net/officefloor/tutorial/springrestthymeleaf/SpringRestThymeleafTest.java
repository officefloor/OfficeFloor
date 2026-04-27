package net.officefloor.tutorial.springrestthymeleaf;

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
public class SpringRestThymeleafTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void greetingWithName() throws Exception {
		mvc.perform(get("/greeting?name=Spring").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, <span>Spring</span>")));
	}

	@Test
	public void greetingDefaultName() throws Exception {
		mvc.perform(get("/greeting").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello, <span>World</span>")));
	}
}
// END SNIPPET: tutorial
