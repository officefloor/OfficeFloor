package net.officefloor.tutorial.springrestfunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestFunctionHttpServerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void validOrderFlowsThroughPipeline() throws Exception {
		mvc.perform(post("/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new OrderRequest("PROD-1", 3)))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").value("ORD-1"))
				.andExpect(jsonPath("$.productId").value("PROD-1"))
				.andExpect(jsonPath("$.quantity").value(3))
				.andExpect(jsonPath("$.total").value(3 * 9.99));
	}

	@Test
	public void invalidOrderStopsAtValidation() throws Exception {
		mvc.perform(post("/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(new OrderRequest("", 0)))
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderId").isEmpty())
				.andExpect(jsonPath("$.total").value(0.0));
	}
}
// END SNIPPET: tutorial
