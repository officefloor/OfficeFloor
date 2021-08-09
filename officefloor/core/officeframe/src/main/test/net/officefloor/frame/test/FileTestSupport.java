/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link File} {@link TestSupport}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileTestSupport implements TestSupport {

	/*
	 * ===================== TestSupport ===========================
	 */

	public FileTestSupport() {
		// Nothing to initialise
	}

	@Override
	public void init(ExtensionContext context) throws Exception {
		// Nothing to initialise
	}

	/**
	 * Obtains the file at the relative path.
	 * 
	 * @param relativePath Relative path to the file.
	 * @return {@link File}.
	 * @throws FileNotFoundException If file could not be found.
	 */
	public File findFile(String relativePath) throws FileNotFoundException {

		// Obtain the current directory
		File currentDirectory = new File(".");

		// Create the listing of paths to find the file
		List<File> paths = new LinkedList<File>();
		paths.add(new File(currentDirectory, relativePath));
		paths.add(new File(new File(currentDirectory, "target/test-classes"), relativePath));
		paths.add(new File(new File(currentDirectory, "target/classes"), relativePath));

		// As last resource, use src as target resources not copied
		paths.add(new File(new File(currentDirectory, "src/test/resources/"), relativePath));

		// Obtain the file
		for (File path : paths) {
			if (path.exists()) {
				return path;
			}
		}

		// File not found
		throw new FileNotFoundException("Can not find file with relative path '" + relativePath + "'");
	}

	/**
	 * Obtains the file by the input file name located in the package of the input
	 * class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException Should the file not be found.
	 */
	public File findFile(Class<?> packageClass, String fileName) throws FileNotFoundException {

		// Obtain the relative file path
		File relativePath = new File(this.getPackageRelativePath(packageClass), fileName);

		// Obtain the file
		return this.findFile(relativePath.getPath());
	}

	/**
	 * Obtains the file location of the input file located in the package of the
	 * input class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of the file within the package directory.
	 * @return Path to the file.
	 */
	public String getFileLocation(Class<?> packageClass, String fileName) {
		return this.getPackageRelativePath(packageClass) + "/" + fileName;
	}

	/**
	 * Creates the input directory.
	 * 
	 * @param directory Directory to be cleared.
	 */
	public void clearDirectory(File directory) {

		// Ensure have a directory
		if (directory == null) {
			return;
		}

		// Clear only if directory
		if (directory.isDirectory()) {
			// Clear the directory
			for (File child : directory.listFiles()) {
				deleteDirectory(child);
			}
		}
	}

	/**
	 * Deletes the input directory.
	 * 
	 * @param directory Directory to be deleted.
	 */
	public void deleteDirectory(File directory) {

		// Ensure have a directory
		if (directory == null) {
			return;
		}

		// Determine if directory
		if (directory.isDirectory()) {
			// Recursively delete children of directory
			for (File child : directory.listFiles()) {
				deleteDirectory(child);
			}
		}

		// Delete the directory (or file)
		Assertions.assertTrue(directory.delete(), "Failed deleting " + directory.getPath());
	}

	/**
	 * Copies the contents of the <code>source</code> directory to the
	 * <code>target</code> directory.
	 * 
	 * @param source Source directory.
	 * @param target Target directory.
	 * @throws IOException If fails to copy the directory.
	 */
	public void copyDirectory(File source, File target) throws IOException {

		// Ensure the source directory exists
		Assertions.assertTrue(source.isDirectory(), "Can not find source directory " + source.getAbsolutePath());

		// Ensure the target directory is available
		if (target.exists()) {
			// Ensure is a directory
			Assertions.assertTrue(target.isDirectory(), "Target is not a directory " + target.getAbsolutePath());
		} else {
			// Create the target directory
			target.mkdir();
		}

		// Copy the files of the source directory to the target directory
		for (File file : source.listFiles()) {
			if (file.isDirectory()) {
				// Recursively copy sub directories
				this.copyDirectory(new File(source, file.getName()), new File(target, file.getName()));
			} else {
				// Copy the file
				InputStream reader = new FileInputStream(file);
				OutputStream writer = new FileOutputStream(new File(target, file.getName()));
				int value;
				while ((value = reader.read()) != -1) {
					writer.write((byte) value);
				}
				writer.close();
				reader.close();
			}
		}
	}

	/**
	 * <p>
	 * Obtains the input stream to the file by the input file name located in the
	 * package of the input class.
	 * <p>
	 * Note: this also searches the class path for the file.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @param fileName     Name of file within the package directory.
	 * @return File within the package directory.
	 * @throws FileNotFoundException Should the file not be found.
	 */
	public InputStream findInputStream(Class<?> packageClass, String fileName) throws FileNotFoundException {

		// Obtain the relative file path
		File relativePath = new File(this.getPackageRelativePath(packageClass), fileName);

		// Attempt to obtain input stream to file from class path
		InputStream inputStream = ClassLoader.getSystemResourceAsStream(relativePath.getPath());
		if (inputStream != null) {
			return inputStream;
		}

		// Not found on class path, thus obtain via finding the file
		return new FileInputStream(this.findFile(relativePath.getPath()));
	}

	/**
	 * Obtains the relative path of the package of the class.
	 * 
	 * @param packageClass Class to obtain the relative path from for its package.
	 * @return Relative path of class's package.
	 */
	public String getPackageRelativePath(Class<?> packageClass) {
		// Obtain package
		String packageName = packageClass.getPackage().getName();

		// Return package name as relative path
		return packageName.replace('.', '/');
	}

	/**
	 * Obtains the contents of the output file.
	 * 
	 * @param file File to obtain contents from.
	 * @return Contents of the output file.
	 * @throws FileNotFoundException Should output file not yet be created.
	 * @throws IOException           Should fail to read from output file.
	 */
	public String getFileContents(File file) throws FileNotFoundException, IOException {

		// Read in contents of file
		StringWriter contents = new StringWriter();
		Reader reader = new FileReader(file);
		for (int value = reader.read(); value != -1; value = reader.read()) {
			contents.write(value);
		}
		reader.close();

		// Return file contents
		return contents.toString();
	}

	/**
	 * Creates the target file with the content.
	 * 
	 * @param content Content for the file.
	 * @param target  Target file.
	 * @throws IOException If fails to create.
	 */
	public void createFile(File target, InputStream content) throws IOException {

		// Ensure the target file does not exist
		if (target.exists()) {
			throw new IOException("Target file already exists [" + target.getAbsolutePath() + "]");
		}

		// Load the file content
		OutputStream outputStream = new FileOutputStream(target);
		int value;
		while ((value = content.read()) != -1) {
			outputStream.write(value);
		}
		outputStream.close();
	}

}
