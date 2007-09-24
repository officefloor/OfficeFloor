/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.generate.model;

import java.io.InputStream;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.repository.ConfigurationContext;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.repository.ModelRepository;

/**
 * Ensures able to unmarshall
 * {@link net.officefloor.model.generate.model.ModelMetaData}.
 * 
 * @author Daniel
 */
public class ModelMetaDataUnmarshallTest extends OfficeFrameTestCase {

	/**
	 * Unmarshall {@link ModelMetaData}.
	 */
	public void testUnmarshallModelMetaData() throws Exception {

		// Obtain the configuration
		final InputStream input = this.findInputStream(this.getClass(),
				"Test.model.xml");
		ConfigurationItem configuration = new ConfigurationItem() {

			public String getId() {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("TODO implement");
			}

			public InputStream getConfiguration() throws Exception {
				return input;
			}

			public void setConfiguration(InputStream configuration)
					throws Exception {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("TODO implement");
			}

			public ConfigurationContext getContext() {
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("TODO implement");
			}

		};

		// Unmarshall the model
		ModelRepository repository = new ModelRepository();
		ModelMetaData model = repository.retrieve(new ModelMetaData(),
				configuration);

		// Validate the model
		assertEquals("Incorrect name", "test", model.getName());
		assertEquals("Incorrect package", "net.officefloor.test", model
				.getPackageName());
		assertEquals("Incorrect import one", "net.officefloor.test.ImportOne",
				model.getImportClasses().get(0));
		assertEquals("Incorrect import two", "net.officefloor.test.ImportTwo",
				model.getImportClasses().get(1));
		FieldMetaData field = model.getFields().get(0);
		assertEquals("Incorrect field name", "Field One", field.getName());
		assertEquals("Incorrect field type", "String", field.getType());
		assertEquals("Incorrect field description", "Test field", field
				.getDescription());
		assertEquals("Incorrect field end-field", "Link One", field
				.getEndField());
		assertEquals("Incorrect field end-list", "Link Two", field.getEndList());
		ListMetaData list = model.getLists().get(0);
		assertEquals("Incorrect list name", "List One", list.getName());
		assertEquals("Incorrect list type", "ImportOne", list.getType());
		assertEquals("Incorrect list description", "Test list", list
				.getDescription());

	}
}
