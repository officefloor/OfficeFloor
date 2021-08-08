/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.explore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityBuilder;
import net.officefloor.web.security.build.HttpSecurityExplorer;
import net.officefloor.web.security.build.HttpSecurityExplorerContext;
import net.officefloor.web.security.scheme.AbstractMockHttpSecuritySource;
import net.officefloor.web.security.scheme.MockAccessControl;
import net.officefloor.web.security.scheme.MockAuthentication;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Tests the {@link HttpSecurityExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityExplorerTest extends OfficeFrameTestCase {

	/**
	 * Ensure handle no {@link HttpSecurity} instances.
	 */
	public void testNoSecurities() throws Exception {
		this.doExplore(V("anonymous", null, null, null));
	}

	/**
	 * Ensure can explore simple {@link HttpSecurity}.
	 */
	public void testSimpleSecurity() throws Exception {
		SimpleHttpSecuritySource source = new SimpleHttpSecuritySource();
		this.doExplore(V("SIMPLE", source, null, (explore) -> {
			assertEquals("Should be no dependencies", 0, explore.getHttpSecurityType().getDependencyTypes().length);
			assertEquals("Should be no flows", 0, explore.getHttpSecurityType().getFlowTypes().length);
		}));
	}

	public static class SimpleHttpSecuritySource extends AbstractMockHttpSecuritySource<String, None, None> {
	}

	/**
	 * Ensure explore multiple {@link HttpSecurity} instances.
	 */
	public void testMultipleSecurities() throws Exception {
		this.doExplore(V("ONE", new SimpleHttpSecuritySource(), null, null),
				V("TWO", new SimpleHttpSecuritySource(), null, null));
	}

	/**
	 * Ensure explore flow of {@link HttpSecurity}.
	 */
	public void testFlow() throws Exception {
		this.doExplore(V("FLOW", new FlowHttpSecuritySource(), (security, context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.link(security.getOutput("0"),
					context.addSection("SECTION", FlowSection.class).getOfficeSectionInput("service"));
		}, (explore) -> {
			HttpSecurityFlowType<?>[] flowTypes = explore.getHttpSecurityType().getFlowTypes();
			assertEquals("Incorrect number of flows", 1, flowTypes.length);
			ExecutionManagedFunction function = explore.getManagedFunction(flowTypes[0]);
			assertNotNull("Should explore flow", function);
			assertEquals("Incorrect flow", "SECTION.service", function.getManagedFunctionName());
		}));
	}

	public static class FlowHttpSecuritySource extends AbstractMockHttpSecuritySource<String, None, Indexed> {

		@Override
		protected void loadMetaData(
				MetaDataContext<MockAuthentication, MockAccessControl, String, None, Indexed> context)
				throws Exception {
			super.loadMetaData(context);
			context.addFlow(String.class);
		}
	}

	public static class FlowSection {
		public void service(@Parameter String parameter) {
			// no operation
		}
	}

	/**
	 * Convenience short hand constructor for {@link SecurityVerify}.
	 */
	private static SecurityVerify V(String httpSecurityName, HttpSecuritySource<?, ?, ?, ?, ?> httpSecuritySource,
			BiConsumer<HttpSecurityBuilder, CompileWebContext> enrich,
			Consumer<HttpSecurityExplorerContext> validator) {
		return new SecurityVerify(httpSecurityName, httpSecuritySource, enrich, validator);
	}

	/**
	 * Undertakes testing exploring.
	 * 
	 * @param verifications {@link SecurityVerify} instances.
	 */
	private void doExplore(SecurityVerify... verifications) throws Exception {
		WebCompileOfficeFloor compiler = new WebCompileOfficeFloor();
		compiler.mockHttpServer(null);
		Map<String, SecurityVerify> verifiers = new HashMap<>();
		Closure<Boolean> isExplored = new Closure<>(false);
		compiler.web((context) -> {
			HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());

			// Load the securities
			for (SecurityVerify verifier : verifications) {
				String httpSecurityName = verifier.httpSecurityName;
				if (verifier.httpSecuritySource != null) {
					HttpSecurityBuilder builder = security.addHttpSecurity(httpSecurityName,
							verifier.httpSecuritySource);
					if (verifier.enrich != null) {
						verifier.enrich.accept(builder, context);
					}
				}
				verifiers.put(httpSecurityName, verifier);
			}

			// Add explorer
			security.addHttpSecurityExplorer((explore) -> {
				String httpSecurityName = explore.getHttpSecurityName();
				SecurityVerify verifier = verifiers.get(httpSecurityName);
				assertNotNull("No verifier for security " + httpSecurityName, verifier);
				if (verifier.httpSecuritySource != null) {
					assertSame("Incorrect security source", verifier.httpSecuritySource,
							explore.getHttpSecuritySource());
				}
				assertNotNull("Should have security type", explore.getHttpSecurityType());
				if (verifier.validator != null) {
					verifier.validator.accept(explore);
				}
				verifiers.remove(httpSecurityName);
				isExplored.value = true;
			});

			security.informWebArchitect();
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			assertTrue("Should explore", isExplored.value);
			assertEquals("Should verify all securities", 0, verifiers.size());
		}
	}

	private static class SecurityVerify {

		private final String httpSecurityName;

		private final HttpSecuritySource<?, ?, ?, ?, ?> httpSecuritySource;

		private final BiConsumer<HttpSecurityBuilder, CompileWebContext> enrich;

		private final Consumer<HttpSecurityExplorerContext> validator;

		private SecurityVerify(String httpSecurityName, HttpSecuritySource<?, ?, ?, ?, ?> httpSecuritySource,
				BiConsumer<HttpSecurityBuilder, CompileWebContext> enrich,
				Consumer<HttpSecurityExplorerContext> validator) {
			this.httpSecurityName = httpSecurityName;
			this.httpSecuritySource = httpSecuritySource;
			this.enrich = enrich;
			this.validator = validator;
		}
	}

}
