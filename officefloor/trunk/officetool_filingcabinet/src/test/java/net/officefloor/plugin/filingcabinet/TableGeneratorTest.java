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
package net.officefloor.plugin.filingcabinet;

import java.io.File;

import net.officefloor.model.impl.repository.filesystem.FileSystemConfigurationContext;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * Tests the {@link TableGenerator}.
 * 
 * @author Daniel
 */
public class TableGeneratorTest extends AbstractGeneratorTest {

	/**
	 * Ensures the appropriate classes get generated.
	 */
	public void testGenerateTable() throws Exception {

		// Create the configuration context
		String tmpPath = System.getProperty("java.io.tmpdir");
		File targetDir = new File(tmpPath, this.getClass().getSimpleName());
		targetDir.mkdir();
		clearDirectory(targetDir);
		ConfigurationContext configurationContext = new FileSystemConfigurationContext(
				targetDir);

		// Output the listing of tables
		for (TableMetaData table : this.generator.getTableMetaData()) {

			// Ignore information schema
			if ("INFORMATION_SCHEMA".equals(table.getSchemaName())) {
				continue;
			}

			// Generate the table
			TableGenerator generator = new TableGenerator(table);
			generator.generate(configurationContext);
		}

		// Obtain the package path
		File packagePath = new File(new File(targetDir, PACKAGE_PREFIX.replace(
				'.', '/')), "public_/productprice");

		// Validate the generated content
		assertContents(this.findFile(this.getClass(), "ProductPrice.java"),
				new File(packagePath, "ProductPrice.java"));
		assertContents(this.findFile(this.getClass(),
				"ProductPriceIndexProductIdQuantity.java"), new File(
				packagePath, "ProductPriceIndexProductIdQuantity.java"));
		assertContents(this.findFile(this.getClass(),
				"ProductPriceRepository.java"), new File(packagePath,
				"ProductPriceRepository.java"));
	}

}
