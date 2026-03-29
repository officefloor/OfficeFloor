package net.officefloor.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * {@link Mojo} to stop Google Function.
 */
@Mojo(name = "stop", requiresDependencyResolution = ResolutionScope.COMPILE)
public class StopGoogleFunctionMojo extends CloseOfficeFloorMojo {

}
