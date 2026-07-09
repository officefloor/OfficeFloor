package net.officefloor.tutorial.springrestfilter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// START SNIPPET: tutorial
@SpringBootTest
@AutoConfigureMockMvc
public class SpringRestFilterTest {

	@Autowired
	private MockMvc mvc;

	// Five articles are seeded (data.sql): 2 spring, 2 officefloor, 1 testing.

	// No filter: all articles.
	@Test
	public void listAll() throws Exception {
		mvc.perform(get("/article").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(5)));
	}

	// Optional filter present: only the matching category.
	@Test
	public void listFilteredByCategory() throws Exception {
		mvc.perform(get("/article?category=spring").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].category").value("spring"));
	}

	// Pagination without a filter: first page of two.
	@Test
	public void pageUnfiltered() throws Exception {
		mvc.perform(get("/article/page?page=0&size=2").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(2)))
				.andExpect(jsonPath("$.totalElements").value(5))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.page").value(0));
	}

	// Filter and pagination together.
	@Test
	public void pageFilteredByCategory() throws Exception {
		mvc.perform(get("/article/page?category=officefloor&page=0&size=1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(2))
				.andExpect(jsonPath("$.content[0].category").value("officefloor"));
	}

	// Defaults apply when page and size are omitted.
	@Test
	public void pageUsesDefaults() throws Exception {
		mvc.perform(get("/article/page").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(5)))
				.andExpect(jsonPath("$.size").value(10));
	}
}
// END SNIPPET: tutorial
