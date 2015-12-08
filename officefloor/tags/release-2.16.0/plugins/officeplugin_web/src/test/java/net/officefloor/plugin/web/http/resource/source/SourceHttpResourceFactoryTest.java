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
package net.officefloor.plugin.web.http.resource.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.web.http.resource.FileExtensionHttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpFileDescriber;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.classpath.ClasspathHttpResourceFactory;
import net.officefloor.plugin.web.http.resource.war.WarHttpResourceFactory;

/**
 * Tests the {@link SourceHttpResourceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class SourceHttpResourceFactoryTest extends OfficeFrameTestCase {

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext context = this.createMock(SourceContext.class);

	/**
	 * {@link HttpResourceFactory}.
	 */
	private HttpResourceFactory factory;

	/**
	 * Ensure if no properties specified that none are copied (handles nulls).
	 */
	public void testCopyNoProperties() {

		final SourceProperties properties = this
				.createMock(SourceProperties.class);
		final PropertyConfigurable target = this
				.createMock(PropertyConfigurable.class);

		// Record no properties
		this.recordReturn(properties, properties.getProperty(
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, null),
				null);
		this.recordReturn(properties, properties.getProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES, null),
				null);
		this.recordReturn(
				properties,
				properties
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
								null), null);
		this.recordReturn(
				properties,
				properties
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
								null), null);

		// Test no properties
		this.replayMockObjects();
		SourceHttpResourceFactory.copyProperties(properties, target);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to copy properties.
	 */
	public void testCopyProperties() {

		final SourceProperties properties = this
				.createMock(SourceProperties.class);
		final PropertyConfigurable target = this
				.createMock(PropertyConfigurable.class);

		// Record properties
		this.recordReturn(properties, properties.getProperty(
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, null),
				"CLASSPATH");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				"CLASSPATH");
		this.recordReturn(properties, properties.getProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES, null),
				"WAR_DIRECTORY");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES,
				"WAR_DIRECTORY");
		this.recordReturn(
				properties,
				properties
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
								null), "DEFAULT_FILE");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
				"DEFAULT_FILE");
		this.recordReturn(
				properties,
				properties
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
								null), "DIRECT_CONTENT");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
				"DIRECT_CONTENT");

		// Test no properties
		this.replayMockObjects();
		SourceHttpResourceFactory.copyProperties(properties, target);
		this.verifyMockObjects();
	}

	/**
	 * Ensure loads the properties if no configuration provided.
	 */
	public void testLoadNoProperties() {

		final PropertyConfigurable target = this
				.createMock(PropertyConfigurable.class);

		// Test
		this.replayMockObjects();

		// Null for no configuration
		SourceHttpResourceFactory
				.loadProperties(null, null, null, null, target);

		// Empty values for no configuration
		SourceHttpResourceFactory.loadProperties("", new File[0],
				new String[0], null, target);

		this.verifyMockObjects();
	}

	/**
	 * Ensure loads the properties.
	 */
	public void testLoadProperties() throws Exception {

		final PropertyConfigurable target = this
				.createMock(PropertyConfigurable.class);

		File dirOne = this.findFile(this.getClass(), "index.html")
				.getParentFile();
		File dirTwo = this.findFile("PUBLIC/resource.html").getParentFile();

		// Record properties
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX, "PREFIX");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES,
				dirOne.getAbsolutePath() + ";" + dirTwo.getAbsolutePath());
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
				"another.html;test.html");
		target.addProperty(
				SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
				"false");

		// Test
		this.replayMockObjects();
		SourceHttpResourceFactory.loadProperties("PREFIX", new File[] { dirOne,
				dirTwo }, new String[] { "another.html", "test.html" },
				Boolean.FALSE, target);
		this.verifyMockObjects();
	}

	/**
	 * Ensure defaults with
	 * {@link WebAutoWireApplication#WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX} on
	 * no configuration.
	 */
	public void testNoConfiguration() throws Exception {
		this.createSourceHttpResourceFactory(null, null, true);
		this.doCreateHttpResourceTest("resource.html", true);
		this.doCreateHttpResourceTest("unknown.html", false);
	}

	/**
	 * Ensure able to obtain {@link HttpFile} from class path configuration.
	 */
	public void testClasspathHttpFile() throws Exception {
		String classPathPrefix = this.getClass().getPackage().getName();
		this.createSourceHttpResourceFactory(null, classPathPrefix, true);
		this.doCreateHttpResourceTest("OverrideFileNotFound.html", true);
		this.doCreateHttpResourceTest("unknown.html", false);
	}

	/**
	 * Ensure able to obtain {@link HttpFile} from WAR configuration.
	 */
	public void testWarHttpFile() throws IOException {
		File warDirectory = this.findFile(this.getClass(), "index.html")
				.getParentFile();
		this.createSourceHttpResourceFactory(warDirectory.getAbsolutePath(),
				null, true);
		this.doCreateHttpResourceTest("OverrideFileNotFound.html", true);
		this.doCreateHttpResourceTest("unknown.html", false);
	}

	/**
	 * <p>
	 * Ensure able to provide multiple WAR directories.
	 * <p>
	 * Typically within production only one WAR directory is necessary. This
	 * however is useful in development to have multiple (one for source webapp
	 * directory and other for generated content such as GWT).
	 */
	public void testMultipleWarDirectories() throws IOException {
		File warDirOne = this.findFile(this.getClass(), "index.html")
				.getParentFile().getParentFile();
		File warDirTwo = this.findFile(this.getClass(), "index.html")
				.getParentFile();
		this.createSourceHttpResourceFactory(warDirOne.getAbsolutePath() + ";"
				+ warDirTwo.getAbsolutePath(), null, true);
		HttpResource resourceOne = this.doCreateHttpResourceTest("index.html",
				true);
		assertHttpFileContents(
				"Should obtain index.html from first war directory",
				"Test file", resourceOne);
		HttpResource resourceTwo = this.doCreateHttpResourceTest(
				"OverrideFileNotFound.html", true);
		assertHttpFileContents(
				"Should be able to get file from second war directory",
				"File not found", resourceTwo);
	}

	/**
	 * Ensure issue if WAR directory not found.
	 */
	public void testWarDirectoryNotExist() throws IOException {
		try {
			this.createSourceHttpResourceFactory("not existing war directory",
					null, false);
		} catch (FileNotFoundException ex) {
			assertEquals("Incorrect cause",
					"Can not find WAR directory 'not existing war directory'",
					ex.getMessage());
		}
	}

	/**
	 * Ensure WAR look up is used as priority over class path lookup.
	 */
	public void testWarBeforeClasspath() throws IOException {
		File warDirectory = this.findFile(this.getClass(), "index.html")
				.getParentFile();
		this.createSourceHttpResourceFactory(warDirectory.getAbsolutePath(),
				null, true);
		HttpResource resource = this.doCreateHttpResourceTest("index.html",
				true);
		assertHttpFileContents(
				"Should obtain index.html from war directory rather than PUBLIC",
				"Hello World", resource);
	}

	/**
	 * Ensure using direct {@link ByteBuffer} for performance of non-live
	 * directory static content.
	 */
	public void testDirectHttpFile() throws IOException {
		this.createSourceHttpResourceFactory(null, null, true);
		HttpResource resource = this.doCreateHttpResourceTest("index.html",
				true);
		HttpFile file = (HttpFile) resource;
		ByteBuffer contents = file.getContents();
		assertTrue("Should be direct buffer", contents.isDirect());
		assertTrue("Should be read-only", contents.isReadOnly());
		assertHttpFileContents("Should be able to retrieve direct content",
				"test", resource);
	}

	/**
	 * Ensure using non-direct {@link ByteBuffer} for dynamic content.
	 */
	public void testNonDirectHttpFile() throws IOException {
		this.createSourceHttpResourceFactory(null, null, false);
		HttpResource resource = this.doCreateHttpResourceTest("index.html",
				true);
		HttpFile file = (HttpFile) resource;
		ByteBuffer contents = file.getContents();
		assertFalse("Should NOT be direct buffer", contents.isDirect());
		assertTrue("Should however still be read-only", contents.isReadOnly());
		assertHttpFileContents("Should be able to retrieve non-direct content",
				"test", resource);
	}

	/**
	 * Ensure configured {@link HttpResourceFactory} instances are provided the
	 * {@link HttpFileDescriber}.
	 */
	public void testHttpFileDescriber() throws IOException {

		// Add the HTTP File Describer
		this.createSourceHttpResourceFactory(null, null, true);
		FileExtensionHttpFileDescriber fileDescriber = new FileExtensionHttpFileDescriber();
		fileDescriber.loadDefaultDescriptions();
		this.factory.addHttpFileDescriber(fileDescriber);

		// Obtain the HTTP file with description
		HttpResource resource = this.doCreateHttpResourceTest("index.html",
				true);
		HttpFile file = (HttpFile) resource;

		// Validate description
		assertEquals("Should not have Content-Encoding", "",
				file.getContentEncoding());
		assertEquals("Incorrect Content-Type", "text/html",
				file.getContentType());
		assertEquals("Incorrect Charset", Charset.defaultCharset(),
				file.getCharset());
	}

	/**
	 * Creates the {@link SourceHttpResourceFactory}.
	 * 
	 * @param warDirectory
	 *            War directory path.
	 * @param classpathPrefix
	 *            Class path prefix.
	 * @param isDirect
	 *            Flag indicating if direct {@link ByteBuffer} for performance.
	 */
	private void createSourceHttpResourceFactory(String warDirectory,
			String classpathPrefix, boolean isDirect) throws IOException {
		
		// Clear the factories
		ClasspathHttpResourceFactory.clearHttpResourceFactories();
		WarHttpResourceFactory.clearHttpResourceFactories();

		// Record the creation
		this.recordReturn(this.context, this.context.getProperty(
				SourceHttpResourceFactory.PROPERTY_RESOURCE_DIRECTORIES, null),
				warDirectory);
		this.recordReturn(
				this.context,
				this.context
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
								WebAutoWireApplication.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX),
				(classpathPrefix == null ? WebAutoWireApplication.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX
						: classpathPrefix));
		this.recordReturn(this.context, this.context.getClassLoader(), Thread
				.currentThread().getContextClassLoader());
		this.recordReturn(this.context, this.context.getProperty(
				SourceHttpResourceFactory.PROPERTY_DIRECT_STATIC_CONTENT,
				String.valueOf(Boolean.TRUE.booleanValue())), String
				.valueOf(isDirect));
		this.recordReturn(
				this.context,
				this.context
						.getProperty(
								SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES,
								"index.html"), "default.html");

		// Create the factory
		this.replayMockObjects();
		this.factory = SourceHttpResourceFactory
				.createHttpResourceFactory(this.context);
		this.verifyMockObjects();
	}

	/**
	 * Does the create {@link HttpResource} test.
	 * 
	 * @param requestUriPath
	 *            Request URI path.
	 * @return {@link HttpResource}.
	 */
	private HttpResource doCreateHttpResourceTest(String requestUriPath,
			boolean isExpectingExists) {
		try {
			// Ensure leading slash to request URI path
			if (!(requestUriPath.startsWith("/"))) {
				requestUriPath = "/" + requestUriPath;
			}

			// Obtain the resource
			HttpResource resource = this.factory
					.createHttpResource(requestUriPath);
			assertEquals("Incorrectly existing", isExpectingExists,
					resource.isExist());
			return resource;
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Asserts the {@link HttpFile} contents.
	 * 
	 * @param message
	 *            Message.
	 * @param expectedToken
	 *            Expected token within the {@link HttpFile} contents.
	 * @param resource
	 *            {@link HttpResource} for {@link HttpFile}.
	 */
	private static void assertHttpFileContents(String message,
			String expectedToken, HttpResource resource) {
		assertTrue("Resource should be HttpFile", resource instanceof HttpFile);
		HttpFile file = (HttpFile) resource;
		ByteBuffer buffer = file.getContents().duplicate();
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		String contents = new String(data);
		assertTrue(message + "\n\n" + contents,
				contents.contains(expectedToken));
	}
}