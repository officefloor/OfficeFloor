package net.officefloor.tutorial.springrestrelationship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

	private Long id;
	private String title;
	private String content;
	private Long authorId;
}
// END SNIPPET: tutorial
