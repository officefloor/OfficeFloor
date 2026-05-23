package net.officefloor.tutorial.transactionhttpserver;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Post entity.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@Data
@Entity
@HttpObject
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String content;

}
// END SNIPPET: tutorial