package net.officefloor.tutorial.springrestfilter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// START SNIPPET: tutorial
/** A page of articles, with the pagination metadata clients need. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticlePage {

	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private List<ArticleResponse> content;
}
// END SNIPPET: tutorial
