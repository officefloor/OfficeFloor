/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.war;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import net.officefloor.building.decorate.OfficeFloorDecorator;
import net.officefloor.building.decorate.OfficeFloorDecoratorContext;

/**
 * {@link OfficeFloorDecorator} for a WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WarOfficeFloorDecorator implements OfficeFloorDecorator {

	/**
	 * WEB-INF directory name.
	 */
	public static final String WEB_INF = "WEB-INF/";

	/**
	 * Directory within the Web Archive containing classes for the class path.
	 */
	public static final String WEB_INF_CLASSES = WEB_INF + "classes/";

	/**
	 * Directory within the Web Archive containing additional archives for the
	 * class path.
	 */
	public static final String WEB_INF_LIB = WEB_INF + "lib/";

	/**
	 * <code>web.xml</code> location within the Web Archive.
	 */
	public static final String WEB_INF_WEB_XML = WEB_INF + "web.xml";

	/**
	 * Directory to contain the public web content.
	 */
	public static final String WEB_PUBLIC = "PUBLIC/";

	/**
	 * Determine if WoOF resource to stay on class path.
	 * 
	 * @param entryName
	 *            Entry name.
	 * @return <code>true</code> if WoOF resource.
	 */
	private static boolean isWoofResource(String entryName) {

		// Strip off to just file name (no directory names)
		int index = entryName.lastIndexOf('/');
		if (index >= 0) {
			entryName = entryName.substring(index + "/".length());
		}

		// Strip off the extension
		index = entryName.lastIndexOf('.');
		if (index < 0) {
			return false; // must have extension for WoOF resource
		}
		entryName = entryName.substring(0, index);

		// Strip off woof marker
		index = entryName.lastIndexOf('.');
		if (index < 0) {
			return false; // must have woof marker in name
		}
		entryName = entryName.substring(index + ".".length());

		// WoOF resource if WoOF marker
		return ("woof".equalsIgnoreCase(entryName));
	}

	/*
	 * ==================== OfficeFloorDecorator ========================
	 */

	@Override
	public void decorate(OfficeFloorDecoratorContext context) throws Exception {

		// Ignore if not exists
		File rawClassPathFile = new File(context.getRawClassPathEntry());
		if (!rawClassPathFile.exists()) {
			return; // not exists, so do not decorate
		}

		// Decorate based on whether directory or file (archive)
		if (rawClassPathFile.isDirectory()) {
			// Attempt to decorate as directory
			this.decorateDirectory(rawClassPathFile, context);

		} else {
			// Attempt to decorate as archive
			this.decorateArchive(rawClassPathFile, context);
		}
	}

	/**
	 * Decorates the archive.
	 * 
	 * @param rawClassPathFile
	 *            Raw class path file.
	 * @param context
	 *            {@link OfficeFloorDecoratorContext}.
	 * @throws IOException
	 *             If fails to decorate.
	 */
	private boolean decorateArchive(File rawClassPathFile,
			OfficeFloorDecoratorContext context) throws IOException {

		// Determine if Web Archive (archive containing web.xml file)
		try {
			JarFile archive = new JarFile(rawClassPathFile);
			JarEntry webXml = archive.getJarEntry(WEB_INF_WEB_XML);
			if (webXml == null) {
				return false; // no web.xml, so do not decorate
			}
		} catch (IOException ex) {
			return false; // not an archive, so do not decorate
		}

		// Read in the web archive
		JarInputStream input = new JarInputStream(new FileInputStream(
				rawClassPathFile));

		// Strip off the extension (if has extension)
		String webArchiveName = rawClassPathFile.getName();
		int extIndex = webArchiveName.lastIndexOf('.');
		if (extIndex >= 0) {
			webArchiveName = webArchiveName.substring(0, extIndex);
		}

		// Create output archive for web class path content
		File warFile = context.createWorkspaceFile(webArchiveName, "jar");
		JarOutputStream output = new JarOutputStream(new FileOutputStream(
				warFile));

		// Iterate over contents of WAR
		List<String> resolvedLibEntries = new LinkedList<String>();
		for (JarEntry entry = input.getNextJarEntry(); entry != null; entry = input
				.getNextJarEntry()) {

			// Obtain the WAR entry
			String entryName = entry.getName();

			// Obtain the data for the entry
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int value = input.read(); value != -1; value = input.read()) {
				buffer.write(value);
			}
			byte[] data = buffer.toByteArray();

			// Transform the name
			String outputName;
			if (entryName.startsWith(WEB_INF_CLASSES)) {
				// Include entry at top level so on class path
				outputName = entryName.substring(WEB_INF_CLASSES.length());

			} else if (entryName.startsWith(WEB_INF_LIB)) {
				// Ignore directories within lib folder
				if (entry.isDirectory()) {
					continue; // ignore directory in lib folder
				}

				// Obtain name of the lib
				int splitIndex = entryName.lastIndexOf('/');
				String libName = entryName.substring(splitIndex);
				String libExtension = "jar";
				splitIndex = libName.lastIndexOf('.');
				if (splitIndex >= 0) {
					// Has extension
					libExtension = libName.substring(splitIndex + ".".length());
					libName = libName.substring(0, splitIndex);
				}

				// Extract the lib file
				File libFile = context.createWorkspaceFile(libName,
						libExtension);
				FileOutputStream libOutput = new FileOutputStream(libFile);
				libOutput.write(data);
				libOutput.close();

				// Include the resolve lib file
				resolvedLibEntries.add(libFile.getAbsolutePath());

				// Do not include in transformed archive
				outputName = "";

			} else if (entryName.startsWith(WEB_INF)) {
				// Leave in current location
				outputName = entryName;

			} else if (isWoofResource(entryName)) {
				// Leave in current location
				outputName = entryName;

			} else {
				// Public resource
				outputName = WEB_PUBLIC + entryName;
			}

			// Ignore blank transformed name
			if (outputName.trim().length() == 0) {
				continue; // ignore blank transformed name
			}

			// Create the transformed entry
			JarEntry jarEntry = new JarEntry(outputName);
			jarEntry.setComment(entry.getComment());
			jarEntry.setCompressedSize(entry.getCompressedSize());
			jarEntry.setCrc(entry.getCrc());
			jarEntry.setExtra(entry.getExtra());
			jarEntry.setMethod(entry.getMethod());
			jarEntry.setSize(entry.getSize());
			jarEntry.setTime(entry.getTime());

			// Write the entry to the output file
			output.putNextEntry(jarEntry);
			output.write(data);
		}

		// Include the web public directory
		output.putNextEntry(new JarEntry(WEB_PUBLIC));

		// Close streams
		output.close();
		input.close();

		// Include the transformed war
		context.includeResolvedClassPathEntry(warFile.getAbsolutePath());

		// Include the resolved lib entries (after war in class path)
		for (String resolvedLibEntry : resolvedLibEntries) {
			context.includeResolvedClassPathEntry(resolvedLibEntry);
		}

		// Return is a WAR
		return true;
	}

	/**
	 * Decorates the archive.
	 * 
	 * @param rawClassPathDirectory
	 *            Raw class path directory.
	 * @param context
	 *            {@link OfficeFloorDecoratorContext}.
	 * @return <code>true</code> if WAR.
	 * @throws IOException
	 *             If fails to decorate.
	 */
	private boolean decorateDirectory(File rawClassPathDirectory,
			OfficeFloorDecoratorContext context) throws IOException {

		// Determine if extracted Web Archive directory
		File webXml = new File(rawClassPathDirectory, WEB_INF_WEB_XML);
		if (!webXml.isFile()) {
			return false; // no web.xml, so not war, so no decoration
		}

		// Create archive for war class path contents
		File warFile = context.createWorkspaceFile(
				rawClassPathDirectory.getName(), "jar");
		JarOutputStream output = new JarOutputStream(new FileOutputStream(
				warFile));

		// Output directory content to archive
		this.outputDirectory(rawClassPathDirectory, null, output);

		// Include the public directory
		output.putNextEntry(new JarEntry(WEB_PUBLIC));

		// Close the output
		output.close();

		// Decorate with war
		context.includeResolvedClassPathEntry(warFile.getAbsolutePath());

		// Include the lib archives
		File libDir = new File(rawClassPathDirectory, WEB_INF_LIB);
		if (libDir.isDirectory()) {
			for (File libArchive : libDir.listFiles()) {
				context.includeResolvedClassPathEntry(libArchive
						.getAbsolutePath());
			}
		}

		// Return indicating a WAR
		return true;
	}

	/**
	 * Outputs the directory.
	 * 
	 * @param root
	 *            Root directory.
	 * @param relativePath
	 *            Relative path of entry from root.
	 * @param output
	 *            {@link JarOutputStream}.
	 * @throws IOException
	 *             If fails to output directory content.
	 */
	private void outputDirectory(File root, String relativePath,
			JarOutputStream output) throws IOException {

		// Obtain relative directory
		File relativeDirectory = (relativePath == null ? root : new File(root,
				relativePath));

		// Load children of the directory
		for (File child : relativeDirectory.listFiles()) {

			// Create the child path
			String childPath = (relativePath == null ? "" : relativePath + "/")
					+ child.getName();

			// Load based on type (directory/file)
			if (child.isDirectory()) {
				// Add entry for the directory (including trailing '/')
				this.outputEntry(childPath + "/", new byte[0], output);

				// Add the directory's children
				this.outputDirectory(root, childPath, output);

			} else {
				// Obtain data for the file
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				InputStream input = new FileInputStream(child);
				for (int value = input.read(); value != -1; value = input
						.read()) {
					buffer.write(value);
				}
				input.close();
				byte[] data = buffer.toByteArray();

				// Add entry for file
				this.outputEntry(childPath, data, output);
			}
		}
	}

	/**
	 * Outputs the entry to the archive.
	 * 
	 * @param rawEntryName
	 *            Raw entry name.
	 * @param entryData
	 *            Entry data.
	 * @param output
	 *            {@link JarOutputStream}.
	 * @throws IOException
	 *             If fails to output the entry.
	 */
	private void outputEntry(String rawEntryName, byte[] entryData,
			JarOutputStream output) throws IOException {

		// Transform the name
		String outputName;
		if (rawEntryName.startsWith(WEB_INF_CLASSES)) {
			// Include entry at top level so on class path
			outputName = rawEntryName.substring(WEB_INF_CLASSES.length());

		} else if (rawEntryName.startsWith(WEB_INF_LIB)) {
			// Ignore lib folder as included directly
			return;

		} else if (rawEntryName.startsWith(WEB_INF)) {
			// Leave in current location
			outputName = rawEntryName;

		} else if (isWoofResource(rawEntryName)) {
			// Leave in current location
			outputName = rawEntryName;

		} else {
			// Public resource
			outputName = WEB_PUBLIC + rawEntryName;
		}

		// Ignore blank transformed name
		if (outputName.trim().length() == 0) {
			return; // ignore blank transformed name
		}

		// Include the entry
		JarEntry entry = new JarEntry(outputName);
		output.putNextEntry(entry);
		output.write(entryData);
	}

}