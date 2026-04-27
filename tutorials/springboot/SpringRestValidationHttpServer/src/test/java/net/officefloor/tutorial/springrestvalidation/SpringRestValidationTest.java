package net.officefloor.tutorial.springrestvalidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestValidationTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	public void valid_request_is_accepted() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setProduct("Widget");
		request.setQuantity(3);

		mvc.perform(post("/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Ordered 3 x Widget"));
	}

	@Test
	public void invalid_request_is_rejected_with_400() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setProduct("");   // blank — violates @NotBlank
		request.setQuantity(0);   // zero — violates @Min(1)

		mvc.perform(post("/order")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void binding_result_valid_request_is_accepted() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setProduct("Gadget");
		request.setQuantity(5);

		mvc.perform(post("/order/binding")
				.contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Ordered 5 x Gadget"));
	}

	@Test
	public void binding_result_invalid_request_returns_error_details() throws Exception {
		OrderRequest request = new OrderRequest();
		request.setProduct("");   // blank — violates @NotBlank
		request.setQuantity(0);   // zero — violates @Min(1)

		mvc.perform(post("/order/binding")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").isNotEmpty());
	}
}
// END SNIPPET: tutorial
