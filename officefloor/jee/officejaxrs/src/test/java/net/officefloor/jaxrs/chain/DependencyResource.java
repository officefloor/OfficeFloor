package net.officefloor.jaxrs.chain;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.clazz.Dependency;

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

}