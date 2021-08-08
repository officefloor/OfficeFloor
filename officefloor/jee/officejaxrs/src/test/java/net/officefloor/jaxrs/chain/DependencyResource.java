/*-
 * #%L
 * JAX-RS
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

package net.officefloor.jaxrs.chain;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.Qualified;

/**
 * JAX-RS resource using {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
@Path("/dependency")
public class DependencyResource {

	private @Dependency ResourceDependency dependency;

	@GET
	public String get() {
		return "Dependency " + this.dependency.getMessage();
	}

	private @Dependency ResourceDependency duplicateDependency;

	@GET
	@Path("/duplicate")
	public String getDuplicate() {
		return "Duplicate " + this.duplicateDependency.getMessage();
	}

	private @Qualified("QUALIFIED") @Dependency ResourceDependency qualifiedDependency;

	@GET
	@Path("/qualified")
	public String getQualified() {
		return "Dependency " + this.qualifiedDependency.getMessage();
	}

	private @Inject JustInTimeDependency justInTimeDependency;

	@GET
	@Path("/justintime")
	public String justInTime() {
		return "Dependency " + this.justInTimeDependency.getMessage();
	}

	private @Inject JustInTimeDependency justInTimeDuplicate;

	@GET
	@Path("/justintime/duplicate")
	public String justInTimeDuplicate() {
		return "Duplicate " + this.justInTimeDuplicate.getMessage();
	}

	private @QualifiedInject @Inject JustInTimeDependency qualifiedJustInTime;

	@GET
	@Path("/justintime/qualified")
	public String justInTimeQualified() {
		return "Dependency " + this.qualifiedJustInTime.getMessage();
	}

	private @Inject ExecutorService executor;

	@GET
	@Path("/async/dependency")
	public void ayncDependency(@Suspended AsyncResponse async) {
		assertTrue("Should be suspended", async.isSuspended());
		this.executor.execute(() -> async.resume("Async " + this.dependency.getMessage()));
	}

	private @Inject ManagedExecutorService managedExecutor;

	@GET
	@Path("/async/inject")
	public void asyncInject(@Suspended AsyncResponse async) {
		assertTrue("Should be suspended", async.isSuspended());
		this.managedExecutor.execute(() -> async.resume("Async " + this.justInTimeDependency.getMessage()));
	}

}
