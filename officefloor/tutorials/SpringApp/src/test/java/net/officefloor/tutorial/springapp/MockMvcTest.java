package net.officefloor.tutorial.springapp;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Tests with {@link MockMvc}.
 * 
 * @author Daniel Sagenschneider
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MockMvcTest {

	@Autowired
	private MockMvc mvc;

	@Test
	public void getSimple() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/simple")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Simple Spring")));
	}
}