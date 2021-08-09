/*-
 * #%L
 * OfficeFloor AppEngine Maven Plugin
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

package net.officefloor.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.cloud.tools.maven.run.RunMojo;

/**
 * Runs the AppEngine with Datastore for integration testing.
 * 
 * @author Daniel Sagenschneider
 */
@Mojo(name = "run")
public class RunAppEngineMojo extends RunMojo {

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
