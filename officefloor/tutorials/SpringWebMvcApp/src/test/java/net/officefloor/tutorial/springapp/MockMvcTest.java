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

	@Test
	public void getInject() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/complex/inject")).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Inject Dependency")));
	}

	@Test
	public void getStatus() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/complex/status"))
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Status")));
	}

	@Test
	public void getPathParam() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/complex/path/value"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Parameter value")));
	}

	@Test
	public void getQueryParam() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/complex/query?param=value"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Parameter value")));
	}

	@Test
	public void getHeader() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.get("/complex/header").header("header", "value"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Header value")));
	}

	@Test
	public void post() throws Exception {
		this.mvc.perform(MockMvcRequestBuilders.post("/complex").content("value"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string(Matchers.equalTo("Body value")));
	}

}