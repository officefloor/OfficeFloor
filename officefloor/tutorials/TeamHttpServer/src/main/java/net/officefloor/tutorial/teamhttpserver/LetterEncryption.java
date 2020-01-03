package net.officefloor.tutorial.teamhttpserver;

import java.io.Serializable;

/**
 * Encription of the letter.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class LetterEncryption implements Serializable {
	private static final long serialVersionUID = 1L;

	private char letter;

	private char code;

	public LetterEncryption(char letter, char code) {
		this.letter = letter;
		this.code = code;
	}

	public char getLetter() {
		return this.letter;
	}

	public char getCode() {
		return this.code;
	}
}
// END SNIPPET: example