/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.maven;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * Maven goal to aggregate site content.
 * 
 * @goal aggregate
 * 
 * @author Daniel Sagenschneider
 */
public class AggregateGoal extends AbstractMojo {

	/**
	 * Base directory configured by Maven.
	 * 
	 * @parameter default-value="${project.basedir}"
	 */
	private File basedir;

	/**
	 * Directory within ${basedir} to aggregate site content within.
	 * 
	 * @parameter default-value="target/site-aggregate"
	 */
	private String aggregateDir;

	/**
	 * Title for the book.
	 * 
	 * @parameter
	 * @required
	 */
	private String title;

	/*
	 * ======================= Mojo ===========================
	 */

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Ensure have base directory
		if (this.basedir == null) {
			throw new MojoFailureException("Must have basedir");
		}

		// Ensure have aggregate directory
		if ((this.aggregateDir == null)
				|| (this.aggregateDir.trim().length() == 0)) {
			throw new MojoFailureException(
					"Must not have blank aggregate directory");
		}

		// Ensure the aggregate directory is available and cleared
		File aggregateDir = new File(this.basedir, this.aggregateDir);
		deleteDirectory(aggregateDir);
		ensureDirectoryExists(aggregateDir,
				"Failed to create site aggregate directory");

		// Indicate rendering content to directory
		this.getLog()
				.info("Aggregating site content to "
						+ aggregateDir.getAbsolutePath());

		// Create the listing of chapters
		List<Chapter> chapters = new LinkedList<Chapter>();

		// Copy the top level module
		copyModule("", this.basedir, aggregateDir, chapters);

		// Write the book content for doxia
		try {
			PrintWriter book = new PrintWriter(new FileWriter(new File(
					aggregateDir, "book.xml"), false));
			book.println("<book>");
			book.println("\t<id>book</id>");
			book.println("\t<title>" + this.title + "</title>");
			book.println("\t<chapters>");
			for (Chapter chapter : chapters) {
				book.println("\t\t<chapter>");
				book.println("\t\t\t<id>" + chapter.id + "</id>");
				book.println("\t\t\t<title>" + chapter.title + "</title>");
				book.println("\t\t\t<sections>");
				for (Section section : chapter.sections) {
					book.println("\t\t\t\t<section>");
					book.println("\t\t\t\t\t<id>" + section.id + "</id>");
					book.println("\t\t\t\t</section>");
				}
				book.println("\t\t\t</sections>");
				book.println("\t\t</chapter>");
			}
			book.println("\t</chapters>");
			book.print("</book>");
			book.close();
		} catch (IOException ex) {
			throw new MojoFailureException("Failed writing doxia book file", ex);
		}
	}

	/**
	 * Deletes the directory.
	 * 
	 * @param directory
	 *            Directory to clear.
	 * @throws MojoFailureException
	 *             If fails to clear the directory.
	 */
	private static final void deleteDirectory(File directory)
			throws MojoFailureException {

		// Ignore if not exist
		if (!(directory.exists())) {
			return;
		}

		// Remove all files from directory
		for (File file : directory.listFiles()) {
			if (!(file.isDirectory())) {
				// Remove the file
				if (!(file.delete())) {
					throw new MojoFailureException("Failed to clear file "
							+ file.getAbsolutePath());
				}
			}
		}

		// Remove the directories (as all that should be left)
		for (File subDirectory : directory.listFiles()) {
			deleteDirectory(subDirectory);
		}

		// Remove the directory
		if (!(directory.delete())) {
			throw new MojoFailureException("Failed to clear directory "
					+ directory.getAbsolutePath());
		}
	}

	/**
	 * Copies the module.
	 * 
	 * @param modulePrefix
	 *            Prefix for the module.
	 * @param projectBaseDir
	 *            Project base directory.
	 * @param targetDirectory
	 *            Target directory.
	 * @param chapters
	 *            {@link Chapter} instances.
	 * @throws MojoFailureException
	 */
	private static void copyModule(String modulePrefix, File projectBaseDir,
			File targetDirectory, List<Chapter> chapters)
			throws MojoFailureException {

		try {
			// Load the pom.xml
			File pomFile = new File(projectBaseDir, "pom.xml");
			MavenXpp3Reader mavenReader = new MavenXpp3Reader();
			Model pom = mavenReader.read(new FileReader(pomFile));

			// Create and add the chapter
			String chapterId = ("".equals(modulePrefix) ? "introduction"
					: modulePrefix);
			Chapter chapter = new Chapter(chapterId, chapterId);
			chapters.add(chapter);

			// Determine if site directory available
			File siteDir = new File(projectBaseDir, "src/site");
			if (siteDir.isDirectory()) {

				// Aggregate the site directory
				for (File child : siteDir.listFiles()) {

					// Ignore child files
					if (child.isFile()) {
						continue;
					}

					// Determine if the resources directory
					boolean isResourcesDir = ("resources".equals(child
							.getName()));

					// Copy the directory
					copyDirectory(child,
							new File(targetDirectory, child.getName()), pom,
							isResourcesDir, modulePrefix, chapter);
				}
			}

			// Copy the sub modules
			for (String subModule : pom.getModules()) {

				// Obtain sub module details
				String subModulePrefix = subModule.replace('/', '_');
				File subModuleBaseDir = new File(projectBaseDir, subModule);

				// Copy the sub module
				copyModule(subModulePrefix, subModuleBaseDir, targetDirectory,
						chapters);
			}

		} catch (Exception ex) {
			throw new MojoFailureException("Failed to aggregate site content",
					ex);
		}
	}

	/**
	 * Copies the directory contents.
	 * 
	 * @param sourceDirectory
	 *            Directory to be copied.
	 * @param targetDirectory
	 *            Target location to copy directory.
	 * @param isRawCopy
	 *            Indicates if raw copy (no manipulation of content).
	 * @param modulePrefix
	 *            Prefix on files for the module if not raw copy.
	 * @param chapter
	 *            {@link Chapter}.
	 * @throws MojoFailureException
	 *             If fails to copy the directory.
	 */
	private static void copyDirectory(File sourceDirectory,
			File targetDirectory, Model pom, boolean isRawCopy,
			String modulePrefix, Chapter chapter) throws MojoFailureException {

		// Ignore non-existent directory
		if (!(sourceDirectory.isDirectory())) {
			return;
		}

		// Copy the files within the directory
		for (File child : sourceDirectory.listFiles()) {
			if (child.isFile()) {
				// Copy the file
				copyFile(child, targetDirectory, pom, isRawCopy, modulePrefix,
						chapter);
			} else {
				// Directory, so recursively copy
				copyDirectory(child,
						new File(targetDirectory, child.getName()), pom,
						isRawCopy, modulePrefix, chapter);
			}
		}
	}

	/**
	 * Copies the file.
	 * 
	 * @param sourceFile
	 *            File to be copied.
	 * @param targetDirectory
	 *            Target location to copy the file.
	 * @param pom
	 *            POM for the project containing the site file.
	 * @param isRawCopy
	 *            Indicates if raw copy (no manipulation of content).
	 * @param modulePrefix
	 *            Prefix on files for the module if not raw copy.
	 * @param chapter
	 *            {@link Chapter}.
	 * @throws MojoFailureException
	 *             If fails to copy the file.
	 */
	private static void copyFile(File sourceFile, File targetDirectory,
			Model pom, boolean isRawCopy, String modulePrefix, Chapter chapter)
			throws MojoFailureException {

		// Ensure the directory exists
		ensureDirectoryExists(targetDirectory,
				"Failed creating directory in site aggregation");

		// Read in the contents of the file
		String contents;
		try {
			StringWriter buffer = new StringWriter();
			FileReader reader = new FileReader(sourceFile);
			for (int character = reader.read(); character != -1; character = reader
					.read()) {
				buffer.write(character);
			}
			reader.close();
			contents = buffer.toString();
		} catch (IOException ex) {
			throw new MojoFailureException(
					"Failed copy file in site aggregation: "
							+ sourceFile.getPath(), ex);
		}

		// Determine name of target file
		String fileName = sourceFile.getName();

		if (!isRawCopy) {
			// Transform if velocity template
			final String VELOCITY_EXTENSION = ".vm";
			if (fileName.endsWith(VELOCITY_EXTENSION)) {

				// Create the context
				VelocityContext context = new VelocityContext();
				Properties properties = pom.getProperties();
				if (properties != null) {
					for (String name : properties.stringPropertyNames()) {
						String value = properties.getProperty(name);
						context.put(name, value);
					}
				}

				// Replace tags
				StringWriter buffer = new StringWriter();
				Velocity.evaluate(context, buffer, fileName, contents);
				contents = buffer.toString();

				// Remove velocity extension
				fileName = fileName.substring(0,
						(fileName.length() - VELOCITY_EXTENSION.length()));
			}

			// Prefix module
			fileName = ("".equals(modulePrefix) ? "" : modulePrefix + "-")
					+ fileName;

			// Include section for the file
			int extensionIndex = fileName.indexOf('.');
			if (extensionIndex > 0) {
				String fileId = fileName.substring(0, extensionIndex);
				chapter.sections.add(new Section(fileId));
			}
		}

		// Ensure the target file not already exists
		File targetFile = new File(targetDirectory, fileName);
		if (targetFile.exists()) {
			throw new MojoFailureException("Duplicate target file "
					+ targetFile.getPath());
		}

		try {
			// Write the file content
			FileWriter writer = new FileWriter(targetFile, false);
			writer.write(contents);
			writer.close();
		} catch (IOException ex) {
			throw new MojoFailureException(
					"Failed copy file in site aggregation "
							+ sourceFile.getPath() + " to " + fileName, ex);
		}
	}

	/**
	 * Ensures the directory exists.
	 * 
	 * @param directory
	 *            Ensures the directory exists.
	 * @param errorMessage
	 *            Error message.
	 * @throws MojoFailureException
	 *             If fails to ensure directory exists.
	 */
	private static void ensureDirectoryExists(File directory,
			String errorMessage) throws MojoFailureException {
		if (!(directory.isDirectory())) {
			// Create the directory
			if (!(directory.mkdirs())) {
				throw new MojoFailureException(errorMessage + ": "
						+ directory.getPath());
			}
		}
	}

}