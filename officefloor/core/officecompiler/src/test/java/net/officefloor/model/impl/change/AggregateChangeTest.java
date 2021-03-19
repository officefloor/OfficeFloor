/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.change;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.change.Change;

/**
 * Tests the {@link AggregateChange}.
 * 
 * @author Daniel Sagenschneider
 */
public class AggregateChangeTest extends OfficeFrameTestCase {

	/**
	 * Target.
	 */
	private static final String TARGET = "target";

	/**
	 * {@link Change} description.
	 */
	private static final String CHANGE_DESCRIPTION = "description";

	/**
	 * Mock {@link Change}.
	 */
	private final Change<?> one = this.createMock(Change.class);

	/**
	 * Mock {@link Change}.
	 */
	private final Change<?> two = this.createMock(Change.class);

	/**
	 * {@link AggregateChange} to test.
	 */
	private final Change<?> aggregate = new AggregateChange<Object>(TARGET, CHANGE_DESCRIPTION, one, two);

	/**
	 * Ensure details are correct.
	 */
	public void testDetails() {
		assertEquals("Incorrect target", TARGET, this.aggregate.getTarget());
		assertEquals("Incorrect change description", CHANGE_DESCRIPTION, this.aggregate.getChangeDescription());
	}

	/**
	 * Ensure able to not apply.
	 */
	public void testNotApply() {
		this.recordReturn(this.one, this.one.canApply(), true);
		this.recordReturn(this.two, this.two.canApply(), false);
		this.replayMockObjects();
		assertFalse("Should not be able to apply if one change can not be applied", this.aggregate.canApply());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to apply.
	 */
	public void testCanApply() {
		this.recordReturn(this.one, this.one.canApply(), true);
		this.recordReturn(this.two, this.two.canApply(), true);
		this.replayMockObjects();
		assertTrue("Should be able to apply if all changes can apply", this.aggregate.canApply());
		this.verifyMockObjects();
	}

	/**
	 * Undertake the apply.
	 */
	public void testApply() {
		this.one.apply();
		this.two.apply();
		this.replayMockObjects();
		this.aggregate.apply();
		this.verifyMockObjects();
	}

	/**
	 * Revert in reverse order.
	 */
	public void testRevert() {
		this.two.revert();
		this.one.revert();
		this.replayMockObjects();
		this.aggregate.revert();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can use convenience constructor.
	 */
	public void testConvenienceConstructor() {
		this.recordReturn(this.one, this.one.getTarget(), TARGET);
		this.recordReturn(this.one, this.one.getChangeDescription(), "Change description");
		this.replayMockObjects();
		Change<?> change = AggregateChange.aggregate(this.one, this.two);
		assertSame("Incorrect target", TARGET, change.getTarget());
		assertEquals("Incorrect description", "Change description", change.getChangeDescription());
		this.verifyMockObjects();
	}

}
