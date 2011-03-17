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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
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

	/**
	 * Base URL for links.
	 * 
	 * @parameter
	 * @required
	 */
	private String baseUrl;

	/**
	 * Ignore file names.
	 * 
	 * @parameter
	 */
	private String[] ignores;

	/**
	 * Order of the sections.
	 * 
	 * @parameter
	 */
	private String[] order;

	/**
	 * Directory to copy aggregated resources content for potential PDF
	 * generation.
	 * 
	 * @parameter default-value="target/generated-site/pdf/book"
	 */
	private String pdfResourcesDirectory;

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
		copyModule("", this.basedir, aggregateDir, this.baseUrl, "", chapters,
				this.ignores, this);

		// Copy resources for PDF generation
		File resourcesDir = new File(aggregateDir, "resources");
		File pdfDir = new File(this.basedir, this.pdfResourcesDirectory);
		copyDirectory(resourcesDir, pdfDir, null, null, null, null, true, "",
				new Chapter("resources", "resources"), new String[0]);

		// Order the sections
		for (Chapter chapter : chapters) {
			Collections.sort(chapter.sections, new Comparator<Section>() {
				@Override
				public int compare(Section a, Section b) {
					int indexA = this.getIndex(a);
					int indexB = this.getIndex(b);
					return indexA - indexB;
				}

				private int getIndex(Section section) {
					String[] ordering = AggregateGoal.this.order;
					if (ordering != null) {
						for (int i = 0; i < ordering.length; i++) {
							if (section.id.endsWith(ordering[i])) {
								return i; // provide order
							}
						}
					}
					return Integer.MAX_VALUE; // default to last
				}
			});
		}

		// Write the book content for doxia
		try {
			PrintWriter book = new PrintWriter(new FileWriter(new File(
					aggregateDir, "book.xml"), false));
			book.println("<book xmlns=\"http://maven.apache.org/BOOK/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/BOOK/1.0.0 http://maven.apache.org/xsd/book-1.0.0.xsd\">");
			book.println("\t<id>book</id>");
			book.println("\t<title>" + this.title + "</title>");
			book.println("\t<chapters>");
			for (Chapter chapter : chapters) {
				if (chapter.sections.size() > 0) {
					// Include chapter as has content
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
	 * @param baseUrl
	 *            Base URL for content.
	 * @param moduleRelativePath
	 *            Relative path from base URL for the module.
	 * @param chapters
	 *            {@link Chapter} instances.
	 * @param ignore
	 *            Files to ignore.
	 * @param mojo
	 *            {@link AggregateGoal}.
	 * @throws MojoFailureException
	 */
	private static void copyModule(String modulePrefix, File projectBaseDir,
			File targetDirectory, String baseUrl, String moduleRelativePath,
			List<Chapter> chapters, String[] ignore, AggregateGoal mojo)
			throws MojoFailureException {

		try {
			// Load the pom.xml
			File pomFile = new File(projectBaseDir, "pom.xml");
			MavenXpp3Reader mavenReader = new MavenXpp3Reader();
			Model pom = mavenReader.read(new FileReader(pomFile));

			// Create and add the chapter
			String chapterId = ("".equals(modulePrefix) ? "introduction"
					: modulePrefix);
			String chapterName = pom.getName();
			if ((chapterName == null) || (chapterName.trim().length() == 0)) {
				chapterName = chapterId; // default to chapter Id
			}
			Chapter chapter = new Chapter(chapterId, chapterName);
			chapters.add(chapter);

			// Log progress
			mojo.getLog().info("\t" + modulePrefix);

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
							new File(targetDirectory, child.getName()),
							baseUrl, moduleRelativePath, pom, projectBaseDir,
							isResourcesDir, modulePrefix, chapter, ignore);
				}
			}

			// Copy the sub modules
			for (String subModule : pom.getModules()) {

				// Obtain sub module details
				String subModuleRelativePath = ("".equals(moduleRelativePath) ? ""
						: moduleRelativePath + "/")
						+ subModule;
				String subModulePrefix = subModule.replace('/', '_');
				subModulePrefix = subModulePrefix.replace('.', '_');
				File subModuleBaseDir = new File(projectBaseDir, subModule);

				// Determine if ignore
				if (isIgnore(subModuleBaseDir, ignore)) {
					continue;
				}

				// Copy the sub module
				copyModule(subModulePrefix, subModuleBaseDir, targetDirectory,
						baseUrl, subModuleRelativePath, chapters, ignore, mojo);
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
	 * @param baseUrl
	 *            Base URL for content.
	 * @param moduleRelativePath
	 *            Relative path from base URL for the module.
	 * @param pom
	 *            {@link Model} POM.
	 * @param moduleBaseDir
	 *            Module base directory.
	 * @param isRawCopy
	 *            Indicates if raw copy (no manipulation of content).
	 * @param modulePrefix
	 *            Prefix on files for the module if not raw copy.
	 * @param chapter
	 *            {@link Chapter}.
	 * @param ignore
	 *            Files to ignore.
	 * @throws MojoFailureException
	 *             If fails to copy the directory.
	 */
	private static void copyDirectory(File sourceDirectory,
			File targetDirectory, String baseUrl, String moduleRelativePath,
			Model pom, File moduleBaseDir, boolean isRawCopy,
			String modulePrefix, Chapter chapter, String[] ignore)
			throws MojoFailureException {

		// Determine if ignore directory
		if (isIgnore(sourceDirectory, ignore)) {
			return;
		}

		// Ignore non-existent directory
		if (!(sourceDirectory.isDirectory())) {
			return;
		}

		// Copy the files within the directory
		for (File child : sourceDirectory.listFiles()) {
			if (child.isFile()) {
				// Copy the file
				copyFile(child, targetDirectory, baseUrl, moduleRelativePath,
						pom, moduleBaseDir, isRawCopy, modulePrefix, chapter,
						ignore);
			} else {
				// Directory, so recursively copy
				copyDirectory(child,
						new File(targetDirectory, child.getName()), baseUrl,
						moduleRelativePath, pom, moduleBaseDir, isRawCopy,
						modulePrefix, chapter, ignore);
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
	 * @param baseUrl
	 *            Base URL for content.
	 * @param moduleRelativePath
	 *            Relative path from base URL for the module.
	 * @param pom
	 *            POM for the project containing the site file.
	 * @param moduleBaseDir
	 *            Module base directory.
	 * @param isRawCopy
	 *            Indicates if raw copy (no manipulation of content).
	 * @param modulePrefix
	 *            Prefix on files for the module if not raw copy.
	 * @param chapter
	 *            {@link Chapter}.
	 * @param ignore
	 *            Files to ignore.
	 * @throws MojoFailureException
	 *             If fails to copy the file.
	 */
	private static void copyFile(File sourceFile, File targetDirectory,
			String baseUrl, String moduleRelativePath, Model pom,
			File moduleBaseDir, boolean isRawCopy, String modulePrefix,
			Chapter chapter, String[] ignore) throws MojoFailureException {

		// Determine if ignore
		if (isIgnore(sourceFile, ignore)) {
			return;
		}

		// Ensure the directory exists
		ensureDirectoryExists(targetDirectory,
				"Failed creating directory in site aggregation");

		// Read in the contents of the file
		byte[] contents;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			FileInputStream reader = new FileInputStream(sourceFile);
			for (int datum = reader.read(); datum != -1; datum = reader.read()) {
				buffer.write((byte) datum);
			}
			reader.close();
			contents = buffer.toByteArray();
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

						/*
						 * TODO consider resolving POM, however for now just the
						 * properties known to be used for OfficeFloor.
						 */
						if ("${basedir}".equals(value)) {
							value = moduleBaseDir.getAbsolutePath();
						} else if ("${project.version}".equals(value)) {
							value = pom.getVersion();
						}

						// Register the property
						context.put(name, value);
					}
				}

				// Replace tags
				StringWriter buffer = new StringWriter();
				Velocity.evaluate(context, buffer, fileName, new String(
						contents));
				contents = buffer.toString().getBytes();

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

			// Transform APT links
			if (fileName.endsWith(".apt")) {
				String text = new String(contents);
				text = text.replace("{{{/", "{{{" + baseUrl + "/");
				text = text.replace("{{{.", "{{{" + baseUrl + "/"
						+ moduleRelativePath);
				contents = text.getBytes();
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
			FileOutputStream writer = new FileOutputStream(targetFile, false);
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

	/**
	 * Determines if ignore content.
	 * 
	 * @param file
	 *            File to determine if ignore.
	 * @param ignore
	 *            Names to ignore.
	 * @return <code>true</code> ignore file.
	 */
	private static boolean isIgnore(File file, String[] ignore) {

		// Obtain the file name
		String fileName = file.getName();

		// Ignore known files
		if (fileName.endsWith("~")) {
			return true;
		}

		// Determine if ignore
		if (ignore != null) {
			for (String name : ignore) {
				if (name.equals(file.getName())) {
					return true; // ignore
				}
			}
		}

		// As here, do not ignore
		return false;
	}

}