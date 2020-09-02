package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;

import lombok.Value;

/**
 * Encription of the letter.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@Value
public class LetterEncryption implements Serializable {
	private static final long serialVersionUID = 1L;

	private final char letter;

	private final char code;
}
// END SNIPPET: example