package net.officefloor.tutorial.springrestrelationship;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

// START SNIPPET: tutorial
@Entity
@Table(name = "authors")
public class Author {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Article> articles = new ArrayList<>();

	public Author() {
	}

	public Author(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Article getArticle(Long articleId) {
		for (Article article : this.articles) {
			if (article.getId().equals(articleId)) {
				return article;
			}
		}
		return null;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Article> getArticles() {
		return this.articles;
	}
}
// END SNIPPET: tutorial
