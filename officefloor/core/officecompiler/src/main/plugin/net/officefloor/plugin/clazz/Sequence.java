package net.officefloor.plugin.clazz;

/**
 * Sequence to obtain the next index.
 * 
 * @author Daniel Sagenschneider
 */
public class Sequence {

	/**
	 * Value for next index in the sequence.
	 */
	private int nextIndex = 0;

	/**
	 * Obtains the next index in the sequence.
	 * 
	 * @return Next index in the sequence.
	 */
	public int nextIndex() {
		return this.nextIndex++;
	}

}