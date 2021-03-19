/*-
 * #%L
 * OfficeFloor AppEngine Maven Plugin
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

package net.officefloor.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.cloud.tools.maven.run.RunAsyncMojo;

/**
 * Starts the AppEngine with Datastore for integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class StartAppEngineMojo extends RunAsyncMojo {

	/**
	 * Port to run the Datastore on.
	 */
	@Parameter(property = "datastorePort")
	private Integer datastorePort = null;

	/**
	 * Target directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File targetDir;

	/**
	 * Final name.
	 */
	@Parameter(defaultValue = "${project.build.finalName}", readonly = true)
	private String finalName;

	/**
	 * {@link PluginDescriptor}.
	 */
	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor plugin;

	/*
	 * ========================== Mojo ===============================
	 */

	@Override
	public void execute() throws MojoExecutionException {

		// Setup App Engine
		AppEngineUtil.setupAppEngine(this.targetDir, this.finalName, this.datastorePort, this.plugin, this);

		// Continue on to start the app engine
		super.execute();
	}

}
