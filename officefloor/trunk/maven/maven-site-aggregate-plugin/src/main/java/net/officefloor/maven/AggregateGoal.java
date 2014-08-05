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
package net.officefloor.maven;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	 * Author of the book.
	 * 
	 * @parameter
	 * @required
	 */
	private String author;

	/**
	 * Version of the book.
	 * 
	 * @parameter
	 * @required
	 */
	private String version;

	/**
	 * Project logo.
	 * 
	 * @parameter
	 * @required
	 */
	private String projectLogo;

	/**
	 * Base URL for links.
	 * 
	 * @parameter
	 * @required
	 */
	private String baseUrl;

	/**
	 * Table of contents depth.
	 * 
	 * @parameter default-value="3"
	 */
	private int tocDepth;

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

		// Write the pdf.xml for PDF plug-in
		try {
			PrintWriter pdf = new PrintWriter(new FileWriter(new File(
					aggregateDir, "pdf.xml")));
			pdf.println("<document xmlns=\"http://maven.apache.org/DOCUMENT/1.0.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/DOCUMENT/1.0.1  http://maven.apache.org/xsd/document-1.0.1.xsd\" outputName=\""
					+ this.title + "\">");
			pdf.println("\t<meta>");
			pdf.println("\t\t<title>" + this.title + "</title>");
			pdf.println("\t\t<author>" + this.author + "</author>");
			pdf.println("\t</meta>");
			pdf.println("\t<toc name=\"Table of Contents\" depth=\""
					+ this.tocDepth + "\">");
			for (Chapter chapter : chapters) {
				for (Section section : chapter.sections) {
					pdf.println("\t\t<item name=\"" + section.name
							+ "\" ref=\"" + section.reference + "\"/>");
				}
			}
			pdf.println("\t</toc>");
			pdf.println("\t<cover>");
			pdf.println("\t\t<coverTitle>" + this.title + "</coverTitle>");
			pdf.println("\t\t<coverSubTitle>" + this.version
					+ "</coverSubTitle>");
			pdf.println("\t\t<author>" + this.author + "</author>");
			pdf.println("\t\t<companyName>" + this.author + "</companyName>");
			pdf.println("\t\t<projectLogo>" + this.projectLogo
					+ "</projectLogo>");
			pdf.println("\t</cover>");
			pdf.println("</document>");
			pdf.close();
		} catch (IOException ex) {
			throw new MojoFailureException(
					"Failed writing PDF configuration file", ex);
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
			try {
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
							 * TODO consider resolving POM, however for now just
							 * the properties known to be used for OfficeFloor.
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

				// Look to source section name
				String sectionName = null;

				// Specifics for APT files
				if (fileName.endsWith(".apt")) {

					// Obtain text content
					String text = new String(contents);

					// Parse out the section name
					BufferedReader aptReader = new BufferedReader(
							new StringReader(text));
					String titleIndicator = aptReader.readLine();
					if (titleIndicator != null) {
						// Skip -------------------
						titleIndicator = titleIndicator.trim();
						if (titleIndicator.length() > 0) {
							titleIndicator = titleIndicator.replace("-", "");
							if (titleIndicator.length() == 0) {
								// First line title indicator, so obtain title
								sectionName = aptReader.readLine();
								if (sectionName != null) {
									sectionName = sectionName.trim();
								}
							}
						}
					}

					// Transform APT links
					text = text.replace("{{{/", "{{{" + baseUrl + "/");
					text = text.replace("{{{.", "{{{" + baseUrl + "/"
							+ moduleRelativePath);

					// Transform code-snippet macro
					text = transformCodeSnippetMacro(text, moduleBaseDir);

					// Use transformed APT content
					contents = text.getBytes();
				}

				// Prefix module to file name
				fileName = ("".equals(modulePrefix) ? "" : modulePrefix + "-")
						+ fileName;

				// Include section for the file
				int extensionIndex = fileName.indexOf('.');
				if (extensionIndex > 0) {

					// Obtain the file Id
					String fileId = fileName.substring(0, extensionIndex);

					// Default section name to file Id
					if (sectionName == null) {
						sectionName = fileId;
					}

					// Include the section
					chapter.sections.add(new Section(fileId, sectionName,
							fileName));
				}
			} catch (IOException ex) {
				throw new MojoFailureException("Failed to transform APT file: "
						+ sourceFile.getPath(), ex);
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

	/**
	 * Transforms the <code>code-snippet</code> macro.
	 * 
	 * @param text
	 *            Text of the APT file.
	 * @param baseDir
	 *            Base directory.
	 * @return Transformed APT file.
	 * @throws MojoFailureException
	 *             If fails to transform.
	 */
	private static String transformCodeSnippetMacro(String text, File baseDir)
			throws MojoFailureException {
		try {
			// Parse APT file line by line
			BufferedReader reader = new BufferedReader(new StringReader(text));

			// Read through lines transforming
			final String CODE_SNIPPET_MACRO = "%{code-snippet";
			StringWriter transformedContent = new StringWriter();
			PrintWriter writer = new PrintWriter(transformedContent);
			String line;
			while ((line = reader.readLine()) != null) {

				// Determine if code-snippet macro line
				if (!(line.startsWith(CODE_SNIPPET_MACRO))) {
					// Normal line, so include
					writer.println(line);
					continue;
				}

				// code-snippet macro so parse out contents
				line = line.trim();
				line = line.substring(CODE_SNIPPET_MACRO.length());
				if (line.endsWith("}")) {
					line = line.substring(0, (line.length() - 1));
				}
				Map<String, String> parameters = new HashMap<String, String>();
				for (String nameValue : line.split("\\|")) {
					String[] tokens = nameValue.split("=");
					String name = tokens[0].trim();
					String value = (tokens.length >= 2 ? tokens[1].trim() : "");
					parameters.put(name, value);
				}

				// Obtain the file
				String filePath = parameters.get("file");
				if (filePath == null) {
					throw new IOException(
							"No file specified in code-snippet macro for "
									+ text);
				}
				File snippetFile;
				if (filePath.startsWith("/")) {
					// Absolute path
					snippetFile = new File(filePath);
				} else {
					// Relative path to base directory
					snippetFile = new File(baseDir, filePath);
				}

				// Obtain the appropriate snippet
				String id = parameters.get("id");
				StringWriter snippet = new StringWriter();
				PrintWriter snippetWriter = new PrintWriter(snippet);
				BufferedReader snippetReader = new BufferedReader(
						new FileReader(snippetFile));
				try {
					if (id == null) {
						// Use full contents of file
						String snippetLine;
						while ((snippetLine = snippetReader.readLine()) != null) {
							snippetWriter.println(snippetLine);
						}
					} else {
						// Parse out the snippet
						String snippetLine;
						boolean isStarted = false;
						while ((!isStarted)
								&& ((snippetLine = snippetReader.readLine()) != null)) {
							// Remove extra spacing from line
							while (snippetLine.contains("  ")) {
								snippetLine = snippetLine.replace("  ", " ");
							}
							if (snippetLine.contains("START SNIPPET: " + id)) {
								isStarted = true; // found start of snippet
							}
						}
						if (!isStarted) {
							throw new IOException("Did not find snippet '" + id
									+ "' in file "
									+ snippetFile.getAbsolutePath());
						}

						// Found start of snippet so now include the snippet
						boolean isFinished = false;
						while ((!isFinished)
								&& ((snippetLine = snippetReader.readLine()) != null)) {

							// Determine if end of snippet
							String checkLine = snippetLine;
							while (checkLine.contains("  ")) {
								checkLine = checkLine.replace("  ", " ");
							}
							if (checkLine.contains("END SNIPPET: " + id)) {
								isFinished = true; // found end of snippet
							}

							// Include line if not finished
							if (!isFinished) {
								snippetWriter.println(snippetLine);
							}
						}
					}
					snippetWriter.flush();
					
				} finally {
					// Ensure close the snippet reader
					snippetReader.close();
				}

				// Write the code-snippet
				writer.println("+-----+");
				writer.print(snippet.toString());
				writer.println("+-----+");
			}
			writer.flush();

			// Return the transformed content
			return transformedContent.toString();

		} catch (IOException ex) {
			throw new MojoFailureException(
					"Failed parsing 'code-snippet' macro from " + text, ex);
		}
	}

}