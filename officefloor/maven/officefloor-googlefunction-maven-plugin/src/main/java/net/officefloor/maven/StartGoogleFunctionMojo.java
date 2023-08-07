package net.officefloor.maven;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import net.officefloor.server.google.function.maven.MavenGoogleFunctionOfficeFloorExtensionService;
import net.officefloor.server.google.function.maven.OfficeFloorHttpFunctionMain;

/**
 * {@link Mojo} to start Google Function.
 */
@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.COMPILE)
public class StartGoogleFunctionMojo extends OpenOfficeFloorMojo {

	/**
	 * {@link PluginDescriptor}.
	 */
	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor plugin;

	/**
	 * <p>
	 * HTTP port for servicing Google Function.
	 * <p>
	 * Must be specified so that project is clear on integration test ports. This
	 * avoids possible changing default port number.
	 */
	@Parameter(required = true, property = "http.port")
	protected int httpPort;

	/**
	 * <p>
	 * HTTPS port for servicing Google Function.
	 * <p>
	 * Must be specified so that project is clear on integration test ports. This
	 * avoids possible changing default port number.
	 */
	@Parameter(property = "https.port")
	protected int httpsPort = -1;

	/*
	 * ==================== OpenOfficeFloorMojo ====================
	 */

	@Override
	protected void loadAdditionalClasspathElements(List<String> classPathEntries)
			throws MojoExecutionException, MojoFailureException {

		// Include google function available code
		for (Artifact artifact : plugin.getArtifacts()) {
			if (("net.officefloor.server".equals(artifact.getGroupId()))
					&& (artifact.getArtifactId().startsWith("officeserver_googlefunction"))) {
				File jarFile = artifact.getFile();

				// Include on class path
				String jarFilePath = jarFile.getAbsolutePath();
				classPathEntries.add(jarFilePath);
			}
		}
	}

	@Override
	protected void loadAdditionalSystemProperties(Properties systemProperties)
			throws MojoExecutionException, MojoFailureException {

		// Default the HTTPS port
		int https = this.httpsPort;
		if (https < 0) {
			https = this.httpPort + 1;
		}

		// Provide ports
		systemProperties.setProperty(MavenGoogleFunctionOfficeFloorExtensionService.HTTP_PORT_NAME,
				String.valueOf(this.httpPort));
		systemProperties.setProperty(MavenGoogleFunctionOfficeFloorExtensionService.HTTPS_PORT_NAME,
				String.valueOf(https));
	}

	@Override
	protected Class<?> getMainClass() throws MojoExecutionException, MojoFailureException {
		return OfficeFloorHttpFunctionMain.class;
	}

}
