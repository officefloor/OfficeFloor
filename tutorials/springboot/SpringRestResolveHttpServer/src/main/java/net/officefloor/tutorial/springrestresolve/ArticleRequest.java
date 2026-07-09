package net.officefloor.tutorial.springrestresolve;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

	@NotBlank(message = "title is required")
	private String title;

	/** Tags are given by name. The managed Tag entities are resolved from these. */
	private List<String> tags = new ArrayList<>();
}
// END SNIPPET: tutorial
