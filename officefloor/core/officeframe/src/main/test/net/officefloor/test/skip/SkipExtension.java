/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
