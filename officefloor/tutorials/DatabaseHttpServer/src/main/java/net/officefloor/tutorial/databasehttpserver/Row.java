package net.officefloor.tutorial.databasehttpserver;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpParameters;

/**
 * Represents a row from the table in the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@Data
@AllArgsConstructor
@NoArgsConstructor
@HttpParameters
public class Row implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private String description;
}
// END SNIPPET: example