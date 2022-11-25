package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link OneToOne}.
 * 
 * @author Daniel Sagenschneider
 */
public class OneToOneTest {

	/**
	 * {@link OneToOne} being tested.
	 */
	private final OneToOne<OneToOneTest> oneToOne = new OneToOne<>();

	/**
	 * Ensure initially null.
	 */
	@Test
	public void initiallyNull() {
		assertNull(this.oneToOne.get(), "Should be initially null");
	}

	/**
	 * Ensure can set and get related {@link Document}.
	 */
	@Test
	public void setAndGet() {
		this.oneToOne.set(this);
		assertSame(this, this.oneToOne.get(), "Ensure get specified document");
	}
}
