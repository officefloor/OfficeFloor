package net.officefloor.tutorial.scratchopenapi;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

	@NotBlank(message = "title is required")
	private String title;

	private String content;
}
// END SNIPPET: tutorial
