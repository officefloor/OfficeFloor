/*-
 * #%L
 * JAX-RS
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
