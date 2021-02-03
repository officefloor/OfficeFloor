package net.officefloor.maven.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpObject;

/**
 * Message.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@HttpObject
public class Message {

	private String message;

//	public Message(String message) {
//		this.message = message;
//	}
//	
//	public String getMessage() {
//		return this.message;
//	}
}