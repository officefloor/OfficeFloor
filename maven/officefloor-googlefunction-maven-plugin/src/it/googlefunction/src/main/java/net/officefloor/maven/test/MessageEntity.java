package net.officefloor.maven.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message entity.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

	private String id;

	private String message;

}
