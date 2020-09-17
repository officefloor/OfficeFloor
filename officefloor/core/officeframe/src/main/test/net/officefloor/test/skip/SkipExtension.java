/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.test.skip;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link Extension} to skip test under particular conditions.
 * 
 * @author Daniel Sagenschneider
 */
public class SkipExtension implements BeforeAllCallback, BeforeEachCallback {

	/**
	 * Condition for skipping.
	 */
	public static interface SkipCondition {

		/**
		 * Indicates whether to skip.
		 * 
		 * @return <code>true</code> to skip test.
		 */
		boolean isSkip();

		/**
		 * Obtains message indicating reason for skipping.
		 * 
		 * @return Message indicating reason for skipping.
		 */
		String getSkipMessage();
	}

	/**
	 * {@link SkipCondition}.
	 */
	private final SkipCondition condition;

	/**
	 * Instantiate with {@link SkipCondition}.
	 * 
	 * @param condition {@link SkipCondition}.
	 */
	public SkipExtension(SkipCondition condition) {
		this.condition = condition;
	}

	/**
	 * Instantiate.
	 * 
	 * @param isSkip Indicates whether to skip.
	 */
	public SkipExtension(boolean isSkip) {
		this(isSkip, null);
	}

	/**
	 * Instantiate.
	 * 
	 * @param isSkip  Indicates whether to skip.
	 * @param message Skip message.
	 */
	public SkipExtension(boolean isSkip, String message) {
		this(new SkipCondition() {

			@Override
			public boolean isSkip() {
				return isSkip;
			}

			@Override
			public String getSkipMessage() {
				return message;
			}
		});
	}

	/*
	 * ================= Extension =======================
	 */

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.beforeEach(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		assumeFalse(this.condition.isSkip(), this.condition.getSkipMessage());
	}

}
