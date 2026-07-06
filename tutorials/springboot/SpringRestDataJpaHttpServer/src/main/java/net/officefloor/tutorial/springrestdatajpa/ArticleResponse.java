package net.officefloor.tutorial.springrestdatajpa;

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
}
// END SNIPPET: tutorial
