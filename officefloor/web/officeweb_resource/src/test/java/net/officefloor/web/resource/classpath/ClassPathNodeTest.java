/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.classpath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ClassPathNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathNodeTest extends OfficeFrameTestCase {

	/**
	 * Original <code>java.class.path</code> property value to reinstate after
	 * test.
	 */
	private String originalJavaClassPath;

	@Override
	protected void setUp() throws Exception {
		// Record original java.class.path to reinstate after test
		this.originalJavaClassPath = System.getProperty("java.class.path");
	}

	@Override
	protected void tearDown() throws Exception {
		// Reinstate the java.class.path
		System.setProperty("java.class.path", this.originalJavaClassPath);
	}

	/**
	 * Obtains the test resource directory.
	 * 
	 * @return Test resource directory.
	 */
	public File getTestResourceDirectory() throws IOException {
		return this.findFile(this.getClass(), "test.jar").getParentFile();
	}

	/**
	 * Ensure correctly loads a directory tree.
	 */
	public void testDirectoryTree() throws IOException {

		// Specify java.class.path for isolation of testing
		File resourceDirectory = new File(this.getTestResourceDirectory(), "directory");
		System.setProperty("java.class.path", resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree(null);

		// Validate the tree (checking directory, sub directory, file)
		assertNode(tree, "/", "", true, "/index.html", "/sub_directory/");
		ClassPathNode directory = tree.getChild("sub_directory");
		assertNode(directory, "/sub_directory/", "sub_directory", true, "/sub_directory/empty.txt");
		ClassPathNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "index.html", false);
	}

	/**
	 * Ensure correctly loads a directory tree with class path prefix.
	 */
	public void testDirectoryTreeWithClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File resourceDirectory = this.getTestResourceDirectory();
		System.setProperty("java.class.path", resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree("directory");

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", "directory", true, "/index.html", "/sub_directory/");
		ClassPathNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "directory/index.html", false);
	}

	/**
	 * Ensure correctly loads directory not containing the class path prefix
	 * directory.
	 */
	public void testDirectoryTreeNotContainingClassPathPrefix() throws IOException {

		final String CLASS_PATH_PREFIX = "not_exist";

		// Ensure test valid by class path prefix directory not existing
		File resourceDirectory = this.getTestResourceDirectory();
		assertFalse("Invalid test as class path prefix directory exists",
				new File(resourceDirectory, CLASS_PATH_PREFIX).exists());

		// Specify java.class.path for isolation of testing
		System.setProperty("java.class.path", resourceDirectory.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree(CLASS_PATH_PREFIX);

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", CLASS_PATH_PREFIX, true);
	}

	/**
	 * Ensure correctly loads a JAR tree.
	 */
	public void testJarTree() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(this.getTestResourceDirectory(), "test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree(null);

		// Validate the tree (checking directory, sub directory, file)
		assertNode(tree, "/", "", true, "/directory/", "/empty/", "/index.html", "/META-INF/");
		ClassPathNode directory = tree.getChild("directory");
		assertNode(directory, "/directory/", "directory", true, "/directory/index.html", "/directory/sub_directory/");
		ClassPathNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "index.html", false);
	}

	/**
	 * Ensure correctly loads a JAR tree with class path prefix.
	 */
	public void testJarTreeWithClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(this.getTestResourceDirectory(), "test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree("directory");

		// Validate the tree (checking directory, file)
		assertNode(tree, "/", "directory", true, "/index.html", "/sub_directory/");
		ClassPathNode file = tree.getChild("index.html");
		assertNode(file, "/index.html", "directory/index.html", false);
	}

	/**
	 * Ensure correctly loads JAR not containing the class path prefix
	 * directory.
	 */
	public void testJarTreeNotContainingClassPathPrefix() throws IOException {

		// Specify java.class.path for isolation of testing
		File jarFile = new File(this.getTestResourceDirectory(), "test.jar");
		System.setProperty("java.class.path", jarFile.getAbsolutePath());

		// Create the class path resource tree (no class prefix)
		ClassPathNode tree = ClassPathNode.createClassPathResourceTree("not_exist");

		// Validate the tree (no children as not exist)
		assertNode(tree, "/", "not_exist", true);
	}

	/**
	 * Validates the {@link ClassPathNode}.
	 * 
	 * @param node
	 *            {@link ClassPathNode} to be validated.
	 * @param expectedResourcePath
	 *            Expected resource path.
	 * @param expectedClassPath
	 *            Expected class path.
	 * @param isExpectedToBeDirectory
	 *            <code>true</code> if expecting to be directory.
	 * @param expectedChildren
	 *            Resource paths of the expected children.
	 */
	private static void assertNode(ClassPathNode node, String expectedResourcePath, String expectedClassPath,
			boolean isExpectedToBeDirectory, String... expectedChildren) {

		// Validate details of the node
		assertEquals("Incorrect resource path", expectedResourcePath, node.getResourcePath());
		assertEquals("Incorrect class path", expectedClassPath, node.getClassPath());
		assertEquals("Incorrect indication if directory", isExpectedToBeDirectory, node.isDirectory());

		// Obtain the children (ignoring SVN entries)
		ClassPathNode[] children = node.getChildren();
		List<ClassPathNode> svnCleanup = new ArrayList<ClassPathNode>(Arrays.asList(children));
		for (Iterator<ClassPathNode> iterator = svnCleanup.iterator(); iterator.hasNext();) {
			ClassPathNode child = iterator.next();
			if (child.getResourcePath().toLowerCase().contains(".svn")) {
				iterator.remove(); // remove SVN entry
			}
		}
		children = svnCleanup.toArray(new ClassPathNode[svnCleanup.size()]);

		// Validate children of the node
		assertEquals("Incorrect number of children", expectedChildren.length, children.length);
		for (int i = 0; i < expectedChildren.length; i++) {
			ClassPathNode child = children[i];
			String expectedChildResourcePath = expectedChildren[i];
			assertEquals("Incorrect child " + i, expectedChildResourcePath, child.getResourcePath());
		}
	}

}
