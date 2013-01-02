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
package net.officefloor.plugin.war.maven;

import java.io.File;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.war.WarOfficeFloorDecorator;
import net.officefloor.plugin.war.WarOfficeFloorDecoratorTest;

/**
 * Ensure can generate the JAR minus <code>META-INF</code> and
 * <code>WEB-INF</code> directories.
 * 
 * @author Daniel Sagenschneider
 */
public class GenerateJarMinusMetaAndWebInfTest extends OfficeFrameTestCase {

	/**
	 * Expected JAR.
	 */
	private File expectedJar;

	@Override
	protected void setUp() throws Exception {
		this.expectedJar = this.findFile(this.getClass(),
				"ExpectedJar/info.woof.txt").getParentFile();
	}

	/**
	 * Ensure can generate for directory.
	 */
	public void testDirectory() throws Exception {

		// Obtain the directory
		File directory = this.findFile(this.getClass(),
				"ExtractedDirectory/info.woof.txt").getParentFile();

		// Generate the JAR
		File generatedJar = WarOfficeFloorDecorator
				.generateJarMinusMetaAndWebInf(directory);
		assertNotNull("JAR should be generated", generatedJar);

		// Validate as expected
		WarOfficeFloorDecoratorTest.assertJar(this.expectedJar, generatedJar);
	}

	/**
	 * Ensure can generate for web archive.
	 */
	public void testWebArchive() throws Exception {

		// Obtain the WAR
		File war = this.findFile(this.getClass(), "WebArchive.war");

		// Generate the JAR
		File generatedJar = WarOfficeFloorDecorator
				.generateJarMinusMetaAndWebInf(war);
		assertNotNull("JAR should be generated", generatedJar);

		// Validate as expected
		WarOfficeFloorDecoratorTest.assertJar(this.expectedJar, generatedJar);
	}

}