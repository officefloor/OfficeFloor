package net.officefloor.tutorial.cloudhttpserver;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.web.HttpObject;

/**
 * Post document.
 * 
 * @author Daniel Sagenschneider
 */
@Document
@HttpObject
@Data
@NoArgsConstructor
public class Post {

	private @Key String key;

	private String message;

	public Post(String message) {
		this.message = message;
	}
}
