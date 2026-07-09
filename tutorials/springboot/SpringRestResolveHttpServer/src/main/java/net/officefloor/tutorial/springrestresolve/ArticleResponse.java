package net.officefloor.tutorial.springrestresolve;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// START SNIPPET: tutorial
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

	private Long id;
	private String title;
	private List<String> tags;
}
// END SNIPPET: tutorial
