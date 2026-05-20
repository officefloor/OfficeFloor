package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;

import lombok.Data;
import net.officefloor.web.HttpParameters;

/**
 * Request to encode the letter.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@Data
@HttpParameters
public class EncryptLetter implements Serializable {
	private static final long serialVersionUID = 1L;

	private char letter;

	public void setLetter(String letter) {
		this.letter = (letter.length() == 0 ? ' ' : letter.charAt(0));
	}
}
// END SNIPPET: example