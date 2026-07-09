package net.officefloor.tutorial.springrestresolve;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// START SNIPPET: tutorial
/**
 * Reference entity. Tags are shared across articles and are looked up by name.
 * They are pre-seeded (see data.sql), not created per request.
 */
@Entity
@Table(name = "tags")
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	public Tag() {
	}

	public Tag(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
}
// END SNIPPET: tutorial
