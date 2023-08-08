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

		// Artifact filters
		ArtifactFilter[] filters = new ArtifactFilter[] { new ArtifactFilter("org.eclipse", "*"),
				new ArtifactFilter("org.apache.maven", "*") };

		// Include google function available code
		NEXT_ARTIFACT: for (Artifact artifact : plugin.getArtifacts()) {

			// Determine if filter the artifact
			for (ArtifactFilter filter : filters) {
				if (filter.isFilter(artifact)) {
					continue NEXT_ARTIFACT;
				}
			}

			// As here, add the artifact
			File jarFile = artifact.getFile();

			// Include on class path
			String jarFilePath = jarFile.getAbsolutePath();
			classPathEntries.add(jarFilePath);
		}
	}

	/**
	 * {@link Artifact} filter.
	 */
	private static class ArtifactFilter {

		/**
		 * Group ID.
		 */
		private final String groupId;

		/**
		 * Artifact ID prefix.
		 */
		private final String artifactId;

		/**
		 * Instantiate.
		 * 
		 * @param groupId    Group ID.
		 * @param artifactId Artifact ID.
		 */
		private ArtifactFilter(String groupId, String artifactId) {
			this.groupId = groupId;
			this.artifactId = artifactId;
		}

		/**
		 * Determines if filters the {@link Artifact}.
		 * 
		 * @param artifact {@link Artifact}.
		 * @return <code>true</code> if filter the {@link Artifact}.
		 */
		private boolean isFilter(Artifact artifact) {
			if ("*".equals(this.artifactId)) {
				// Filter one group ID prefix
				return artifact.getGroupId().startsWith(this.groupId);
			} else {
				// Filter on artifact ID prefix
				return this.groupId.equals(artifact.getGroupId())
						&& artifact.getArtifactId().startsWith(this.artifactId);
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
