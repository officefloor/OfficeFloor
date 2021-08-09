/*-
 * #%L
 * OfficeXml
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

package net.officefloor.plugin.xml.unmarshall.tree;

import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link TreeXmlUnmarshallerManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public class TreeXmlUnmarshallerManagedObjectSourceTest extends
		AbstractTreeXmlUnmarshallerTestCase {

	/*
	 * ================= AbstractTreeXmlUnmarshallerTestCase =================
	 */

	@Override
	protected TreeXmlUnmarshaller createNonRecursiveTreeXmlUnmarshaller()
			throws Throwable {
		return this.createUnmarshaller("NonRecursiveMetaData.xml");
	}

	@Override
	protected TreeXmlUnmarshaller createRecursiveTreeXmlUnmarshaller()
			throws Throwable {
		return this.createUnmarshaller("RecursiveMetaData.xml");
	}

	/**
	 * Creates the {@link TreeXmlUnmarshaller} from the input configuration.
	 *
	 * @param configurationFileName
	 *            Name of file containing the mapping configuration.
	 * @return {@link TreeXmlUnmarshaller}.
	 * @throws Throwable
	 *             If fails to create the {@link TreeXmlUnmarshaller}.
	 */
	private TreeXmlUnmarshaller createUnmarshaller(String configurationFileName)
			throws Throwable {

		// Ensure the file is available
		this.findFile(this.getClass(), configurationFileName);
		String configurationFilePath = this.getPackageRelativePath(this
				.getClass())
				+ "/" + configurationFileName;

		// Play
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader
				.addProperty(
						TreeXmlUnmarshallerManagedObjectSource.CONFIGURATION_PROPERTY_NAME,
						configurationFilePath);
		TreeXmlUnmarshallerManagedObjectSource source = loader
				.loadManagedObjectSource(TreeXmlUnmarshallerManagedObjectSource.class);

		// Return the TreeXmlUnmarshaller
		return (TreeXmlUnmarshaller) new ManagedObjectUserStandAlone()
				.sourceManagedObject(source).getObject();
	}

}
