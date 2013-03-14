/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.launch.woof;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.classpath.ClassPathFactory;
import net.officefloor.building.classpath.ClassPathFactoryImpl;
import net.officefloor.building.classpath.RemoteRepository;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.woof.PropertyModel;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofRepository;
import net.officefloor.model.woof.WoofRepositoryImpl;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofTemplateExtensionModel;
import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.woof.WoofOfficeFloorSource;
import net.officefloor.plugin.woof.gwt.GwtWoofTemplateExtensionService;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;

/**
 * Loads the {@link WoofDevelopmentConfiguration} from the project.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofDevelopmentConfigurationLoader {

	/**
	 * Group ID for the GWT artifacts.
	 */
	public static final String GWT_GROUP_ID = "com.google.gwt";

	/**
	 * Artifact ID for the GWT User artifact.
	 */
	public static final String GWT_USER_ARTIFACT_ID = "gwt-user";

	/**
	 * Artifact ID for the GWT Dev artifact.
	 */
	public static final String GWT_DEV_ARTIFACT_ID = "gwt-dev";

	/**
	 * Default version for GWT Dev artifact to use.
	 */
	public static final String DEFAULT_GWT_DEV_VERSION = "2.5.0";

	/**
	 * Obtains the GWT development class path to match POM configuration. In
	 * particular to ensure same version of <code>gwt-dev</code> as the
	 * <code>gwt-user</code> used.
	 * 
	 * @param pomFile
	 *            Maven POM file.
	 * @return GWT development class path.
	 * @throws Exception
	 *             If fails to obtain the GWT development class path.
	 */
	public static String[] getDevModeClassPath(File pomFile) throws Exception {

		// Create initial class path factory
		PlexusContainer plexusContainer = new DefaultPlexusContainer();
		ClassPathFactoryImpl initial = new ClassPathFactoryImpl(
				plexusContainer, null, new RemoteRepository[0]);

		// Obtain the Maven project and its remote repositories
		MavenProject project = initial.getMavenProject(pomFile);
		List<RemoteRepository> remoteRepositories = new LinkedList<RemoteRepository>();
		for (ArtifactRepository repository : project
				.getRemoteArtifactRepositories()) {
			remoteRepositories.add(new RemoteRepository(repository.getId(),
					repository.getLayout().getId(), repository.getUrl()));
		}

		// Create class path factory from POM remote repositories
		ClassPathFactory factory = new ClassPathFactoryImpl(
				plexusContainer,
				null,
				remoteRepositories
						.toArray(new RemoteRepository[remoteRepositories.size()]));

		// Keep track of class path
		List<String> gwtClassPath = new LinkedList<String>();

		// Obtain the GWT version
		String gwtDevVersion = null;
		for (Dependency dependency : project.getDependencies()) {
			String groupId = dependency.getGroupId();
			String artifactId = dependency.getArtifactId();
			if ((GWT_GROUP_ID.equals(groupId))
					&& (GWT_USER_ARTIFACT_ID.equals(artifactId))) {
				gwtDevVersion = dependency.getVersion();
			}
		}
		if (gwtDevVersion == null) {
			// Use default version of GWT
			gwtDevVersion = DEFAULT_GWT_DEV_VERSION;

			// Must include GWT User for running
			String[] userClassPath = factory.createArtifactClassPath(
					GWT_GROUP_ID, GWT_USER_ARTIFACT_ID, gwtDevVersion, null,
					null);
			gwtClassPath.addAll(Arrays.asList(userClassPath));
		}

		// Include the class path for gwt-dev
		String[] devClassPath = factory.createArtifactClassPath(GWT_GROUP_ID,
				GWT_DEV_ARTIFACT_ID, gwtDevVersion, null, null);
		for (String classPathEntry : devClassPath) {

			// Ignore if already included
			if (gwtClassPath.contains(classPathEntry)) {
				continue;
			}

			// Include class path
			gwtClassPath.add(classPathEntry);
		}

		// Return the GWT class path
		return gwtClassPath.toArray(new String[gwtClassPath.size()]);
	}

	/**
	 * Loads the {@link WoofDevelopmentConfiguration} from the WoOF
	 * configuration file.
	 * 
	 * @param woofModelConfiguration
	 *            {@link InputStream} to WoOF configuration file (typically
	 *            <code>application.woof</code>). {@link InputStream} to enable
	 *            loading from {@link ClassLoader}.
	 * @return {@link WoofDevelopmentConfiguration}.
	 * @throws Exception
	 *             If fails to load the {@link WoofDevelopmentConfiguration}.
	 */
	public static WoofDevelopmentConfiguration loadConfiguration(
			InputStream woofModelConfiguration) throws Exception {

		// Retrieve the WoOF model
		WoofRepository woofRepository = new WoofRepositoryImpl(
				new ModelRepositoryImpl());
		WoofModel woof = woofRepository
				.retrieveWoOF(new InputStreamConfigurationItem(
						woofModelConfiguration));

		// Load the WoOF development configuration
		WoofDevelopmentConfiguration configuration = new WoofDevelopmentConfiguration();

		// Load template configuration
		for (WoofTemplateModel template : woof.getWoofTemplates()) {

			// Include the template URI
			String templateUri = template.getUri();
			if ((templateUri != null) && (templateUri.trim().length() > 0)) {
				// Template URI provided so include
				String startupUrl = transformUriToStartupUrl(templateUri,
						WoofOfficeFloorSource.WOOF_TEMPLATE_URI_SUFFIX);
				configuration.addStartupUrl(startupUrl);
			}

			// Include GWT modules
			for (WoofTemplateExtensionModel extension : template
					.getExtensions()) {

				// Determine if GWT extension
				String extensionAlias = extension.getExtensionClassName();
				if (!(GwtWoofTemplateExtensionService.EXTENSION_ALIAS
						.equals(extensionAlias))) {
					continue; // ignore non-GWT extension
				}

				// Obtain the GWT module from properties
				for (PropertyModel property : extension.getProperties()) {
					if (WoofChanges.PROPERTY_GWT_MODULE_PATH.equals(property
							.getName())) {
						String gwtModuleName = transformGwtModulePathToName(property
								.getValue());
						configuration.addGwtModuleName(gwtModuleName);
					}
				}
			}
		}

		// Load section configuration
		for (WoofSectionModel section : woof.getWoofSections()) {
			for (WoofSectionInputModel sectionInput : section.getInputs()) {

				// Include the section input URI
				String sectionInputUri = sectionInput.getUri();
				if ((sectionInputUri != null)
						&& (sectionInputUri.trim().length() > 0)) {
					// Section Input URI provided so include
					String startupUrl = transformUriToStartupUrl(
							sectionInputUri, null);
					configuration.addStartupUrl(startupUrl);
				}
			}
		}

		// Return the development configuration
		return configuration;
	}

	/**
	 * Transforms the URI to the startup URL.
	 * 
	 * @param uri
	 *            URI to transform.
	 * @param suffix
	 *            Optional suffix for the URI. May be <code>null</code>.
	 * @return Startup URL.
	 */
	private static String transformUriToStartupUrl(String uri, String suffix) {

		// Determine if root URI (no suffix added)
		if ("/".equals(uri)) {
			return uri;
		}

		// Provide the transformed URI
		return (uri.startsWith("/") ? uri : "/" + uri)
				+ (suffix == null ? "" : suffix);
	}

	/**
	 * Transforms the GWT module path to GWT module name.
	 * 
	 * @param gwtModulePath
	 *            GWT module path.
	 * @return GWT module name.
	 */
	private static String transformGwtModulePathToName(String gwtModulePath) {
		String moduleName = gwtModulePath;

		// Strip off extension
		final String EXTENSION = ".gwt.xml";
		if (moduleName.endsWith(EXTENSION)) {
			moduleName = moduleName.substring(0, moduleName.length()
					- EXTENSION.length());
		}

		// Transform path into package naming
		moduleName = moduleName.replace('/', '.');

		// Return the module name
		return moduleName;
	}

	/**
	 * All access via static methods.
	 */
	private WoofDevelopmentConfigurationLoader() {
	}

}