/*-
 * #%L
 * PayPal
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

package net.officefloor.pay.paypal.mock;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.paypal.core.PayPalHttpClient;

/**
 * {@link TestRule} to mock {@link PayPalHttpClient} interaction.
 * 
 * @author Daniel Sagenschneider
 */
public class PayPalRule extends AbstractPayPalJUnit implements TestRule {

	/*
	 * ====================== TestRule =============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					PayPalRule.this.setupMockPayPaylHttpClient();

					// Evaluation the rule
					base.evaluate();

				} finally {
					PayPalRule.this.tearDownMockPayPaylHttpClient();
				}
			}
		};
	}

}
