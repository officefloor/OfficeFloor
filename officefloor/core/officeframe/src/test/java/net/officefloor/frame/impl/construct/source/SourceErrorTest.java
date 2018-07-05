/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.IssueTarget;
import net.officefloor.frame.api.source.LoadServiceError;
import net.officefloor.frame.api.source.UnknownClassError;
import net.officefloor.frame.api.source.UnknownPropertyError;
import net.officefloor.frame.api.source.UnknownResourceError;
import net.officefloor.frame.api.source.UnknownServiceError;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests each of the errors to ensure emits the appropriate issue.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceErrorTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link UnknownPropertyError} emits the correct issue.
	 */
	public void testUnknonwnPropertyError() {
		this.doTest(new UnknownPropertyError("missing"), "Must specify property 'missing'", null);
		this.doTest(new UnknownPropertyError(new UnknownPropertyError("missing"), new DefaultServiceFactory()),
				"Must specify property 'missing' for service factory " + DefaultServiceFactory.class.getName(), null);
	}

	/**
	 * Ensure {@link UnknownClassError} emits the correct issue.
	 */
	public void testUnknownClassError() {
		this.doTest(new UnknownClassError("missing"), "Can not load class 'missing'", null);
		this.doTest(new UnknownClassError(new UnknownClassError("missing"), new DefaultServiceFactory()),
				"Can not load class 'missing' for service factory " + DefaultServiceFactory.class.getName(), null);
	}

	/**
	 * Ensure {@link UnknownResourceError} emits the correct issue.
	 */
	public void testUnknownResourceError() {
		this.doTest(new UnknownResourceError("missing"), "Can not obtain resource at location 'missing'", null);
		this.doTest(new UnknownResourceError(new UnknownResourceError("missing"), new DefaultServiceFactory()),
				"Can not obtain resource at location 'missing' for service factory "
						+ DefaultServiceFactory.class.getName(),
				null);
	}

	/**
	 * Ensure {@link UnknownServiceError} emits the correct issue.
	 */
	public void testUnknownServiceError() {
		this.doTest(new UnknownServiceError(NotConfiguredServiceFactory.class),
				"No services configured for " + NotConfiguredServiceFactory.class.getName(), null);
	}

	/**
	 * Ensure {@link LoadServiceError} emits the correct issue.
	 */
	public void testLoadServiceError() {
		Throwable cause = new Throwable("TEST");
		this.doTest(new LoadServiceError(DefaultServiceFactory.class.getName(), cause),
				"Failed to create service from " + DefaultServiceFactory.class.getName(), cause);
	}

	/**
	 * Undertakes testing the {@link AbstractSourceError}.
	 * 
	 * @param error
	 *            {@link AbstractSourceError}.
	 * @param expectedDescription
	 *            Expected description of the issue.
	 * @param expectedCause
	 *            Expected cause. May be <code>null</code>.
	 */
	private void doTest(AbstractSourceError error, String expectedDescription, Throwable expectedCause) {
		Closure<String> description = new Closure<>();
		Closure<Throwable> cause = new Closure<>();
		error.addIssue(new IssueTarget() {
			@Override
			public void addIssue(String issueDescription, Throwable issueCause) {
				assertNotNull("Invoked without a cause", cause);
				description.value = issueDescription;
				cause.value = issueCause;
			}

			@Override
			public void addIssue(String issueDescription) {
				description.value = issueDescription;
			}
		});
		assertEquals("Incorrect description", expectedDescription, description.value);
		assertEquals("Incorrect cause", expectedCause, cause.value);
	}

}