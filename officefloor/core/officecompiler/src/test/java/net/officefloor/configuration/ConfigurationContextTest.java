/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.configuration;

import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.function.Supplier;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.configuration.impl.ConfigurationContextImpl;
import net.officefloor.configuration.impl.ConfigurationContextImpl.ConfigurationSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ConfigurationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationContextTest extends OfficeFrameTestCase {

	/**
	 * Location.
	 */
	private static final String LOCATION = "LOCATION";

	/**
	 * {@link ConfigurationSource}.
	 */
	private final ConfigurationSource source = this.createMock(ConfigurationSource.class);

	/**
	 * {@link SourceProperties}.
	 */
	private final SourceProperties sourceProperties = this.createMock(SourceProperties.class);

	/**
	 * {@link ConfigurationContext} to test.
	 */
	private final ConfigurationContext context = new ConfigurationContextImpl(this.source, this.sourceProperties);

	/**
	 * Override {@link PropertyList}.
	 */
	private PropertyList overrideProperties = new PropertyListImpl();

	/**
	 * Ensure can obtain simple text configuration.
	 */
	public void testSimple() {
		this.record("TEST", null, true);
		this.verify(true, "TEST");
	}

	/**
	 * Ensure tag replace.
	 */
	public void testTagReplace() {
		this.record("${tag}", null, true, "tag", "replaced");
		this.verify(true, "replaced");
	}

	/**
	 * Ensure can use alternate charset.
	 */
	public void testAlternateCharset() {

		final Charset alternateCharset = Charset.forName("UTF-16");
		assertNotEquals("Invalid test as same charset", Charset.defaultCharset().name(), alternateCharset.name());

		// Validate
		this.record("TEST", alternateCharset, true, ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_CHARSET,
				alternateCharset.name());
		this.verify(true, "TEST");
	}

	/**
	 * Ensure can configure the tag prefix/suffix.
	 */
	public void testAlternateTagPrefixSuffix() {
		this.record("[tag]", null, true, "tag", "replaced",
				ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX, "[",
				ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX, "]");
		this.verify(true, "replaced");
	}

	/**
	 * Ensure issue as configuration not available when required.
	 */
	public void testFailOnRequiredConfiguration() {
		this.record(null, null, false);
		try {
			this.verify(false, null);
			fail("Should not be successful");
		} catch (ConfigurationError ex) {
			assertEquals("Incorrect resource", LOCATION, ex.getConfigurationLocation());
			assertNull("Should be no tag", ex.getNonconfiguredTagName());
			assertNull("Should not have cause", ex.getCause());
		}
	}

	/**
	 * Ensure no issue if obtaining optional configuration.
	 */
	public void testOptionalConfiguration() {
		this.record(null, null, false);
		this.verify(true, null);
	}

	/**
	 * Ensure all tags are replaced.
	 */
	public void testFailAsTagNotReplaced() {
		this.record("Should replace ${tag} as no property", null, true);
		try {
			this.verify(false, null);
			fail("Should not be successful");
		} catch (ConfigurationError ex) {
			assertEquals("Incorrect resource", LOCATION, ex.getConfigurationLocation());
			assertEquals("Incorrect tag", "tag", ex.getNonconfiguredTagName());
			assertNull("Should not have cause", ex.getCause());
		}
	}

	/**
	 * Ensure handle failure in obtaining configuration.
	 */
	public void testFailAsIOIssue() {
		final IOException failure = new IOException("TEST");
		this.record(() -> {
			try {
				this.recordThrows(this.source, this.source.getConfigurationInputStream(LOCATION), failure);
				return false;
			} catch (IOException ex) {
				throw fail(ex);
			}
		});
		try {
			this.verify(false, null);
			fail("Should not be successful");
		} catch (ConfigurationError ex) {
			assertEquals("Incorrect resource", LOCATION, ex.getConfigurationLocation());
			assertNull("Should not have tag", ex.getNonconfiguredTagName());
			assertSame("Should have cause", failure, ex.getCause());
		}
	}

	/**
	 * Ensure can be <code>null</code> override {@link PropertyList}.
	 */
	public void testNullOverrideProperties() {
		this.overrideProperties = null;
		this.record("TEST", null, true);
		this.verify(false, "TEST");
	}

	/**
	 * Ensure can override the properties.
	 */
	public void testOverrideProperties() {

		// Obtain alternate Charset
		final Charset alternateCharset = Charset.forName("UTF-16");
		assertNotEquals("Invalid test as same charset", Charset.defaultCharset().name(), alternateCharset.name());

		// Load override properties
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_CHARSET)
				.setValue(alternateCharset.name());
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX).setValue("[");
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX).setValue("]");
		this.overrideProperties.addProperty("tag").setValue("override");

		// Ensure override
		this.record("should [tag] property", alternateCharset, true, "tag", "replace");
		this.verify(false, "should override property");
	}

	/**
	 * Ensure can override the properties back to the defaults.
	 */
	public void testOverridePropertiesToDefaults() {
		// Obtain alternate Charset
		final Charset alternateCharset = Charset.forName("UTF-16");
		assertNotEquals("Invalid test as same charset", Charset.defaultCharset().name(), alternateCharset.name());

		// Load reset to default overrides
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_CHARSET).setValue("");
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX).setValue("");
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX).setValue("");
		this.overrideProperties.addProperty(ConfigurationContext.PROPERTY_CONFIGURATION_OUTPUT_CHARSET).setValue("");
		this.overrideProperties.addProperty("tag").setValue("");

		// Ensure override
		this.record("should ${tag} change", null, true, ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_CHARSET,
				alternateCharset.name(), ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX, "[",
				ConfigurationContext.PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX, "]", "tag", "replace",
				ConfigurationContext.PROPERTY_CONFIGURATION_OUTPUT_CHARSET, alternateCharset.name());
		this.verify(false, "should  change");
	}

	/**
	 * Ensure can use default output {@link Charset}.
	 */
	public void testDefaultOutputCharset() {
		Charset defaultCharset = Charset.defaultCharset();
		this.record("default output charset", null, true, ConfigurationContext.PROPERTY_CONFIGURATION_OUTPUT_CHARSET,
				defaultCharset.name());
		this.verify(defaultCharset, "default output charset");
	}

	/**
	 * Ensure can configure output {@link Charset}.
	 */
	public void testAlternateOutputCharset() {

		// Obtain the alternate Charset
		final Charset alternateCharset = Charset.forName("UTF-16");
		assertNotEquals("Invalid test as same charset", Charset.defaultCharset().name(), alternateCharset.name());

		// Undertake test
		this.record("change output charset", null, true, ConfigurationContext.PROPERTY_CONFIGURATION_OUTPUT_CHARSET,
				alternateCharset.name());
		this.verify(alternateCharset, "change output charset");
	}

	/**
	 * Records the functionality.
	 * 
	 * @param content                Configuration content.
	 * @param charset                {@link Charset}. May be <code>null</code>.
	 * @param isSuccessful           Indicates if successful.
	 * @param propertyNameValuePairs {@link Property} name/value pairs.
	 */
	private void record(String content, Charset charset, boolean isSuccessful, String... propertyNameValuePairs) {

		// Obtain the charset
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		final Charset finalCharset = charset;

		// Record functionality
		this.record(() -> {

			try {
				// Obtain the input stream
				InputStream inputStream = null;
				if (content != null) {

					// Obtain the input
					inputStream = new ByteArrayInputStream(content.getBytes(finalCharset));
				}

				// Record obtaining the byte input
				this.recordReturn(this.source, this.source.getConfigurationInputStream(LOCATION), inputStream);

				// Return whether successful
				return isSuccessful;

			} catch (IOException ex) {
				throw fail(ex);
			}

		}, propertyNameValuePairs);

	}

	/**
	 * Records the functionality.
	 * 
	 * @param sourcer                {@link Supplier} to record sourcing the
	 *                               configuration.
	 * @param propertyNameValuePairs {@link Property} name/value pairs.
	 */
	private void record(Supplier<Boolean> sourcer, String... propertyNameValuePairs) {

		// Obtain content
		if (!sourcer.get()) {
			return; // does not complete
		}

		// Obtain the listing of properties
		String[] propertyNames = new String[propertyNameValuePairs.length / 2];
		Properties properties = new Properties();
		for (int i = 0; i < propertyNameValuePairs.length; i += 2) {
			String propertyName = propertyNameValuePairs[i];
			String propertyValue = propertyNameValuePairs[i + 1];
			propertyNames[i / 2] = propertyName;
			properties.setProperty(propertyName, propertyValue);
		}

		// Record obtaining the property names
		this.recordReturn(this.sourceProperties, this.sourceProperties.getPropertyNames(), propertyNames);

		// Record obtaining the properties
		for (String propertyName : propertyNames) {
			String propertyValue = properties.getProperty(propertyName);
			this.recordReturn(this.sourceProperties, this.sourceProperties.getProperty(propertyName), propertyValue);
		}
	}

	/**
	 * Verifies the functionality.
	 * 
	 * @param isOptional      Whether optional or required configuration.
	 * @param expectedContent Expected content. May be <code>null</code>.
	 */
	private void verify(boolean isOptional, String expectedContent) {

		// Record
		this.replayMockObjects();

		// Obtain the configuration item
		ConfigurationItem item = (isOptional
				? this.context.getOptionalConfigurationItem(LOCATION, this.overrideProperties)
				: this.context.getConfigurationItem(LOCATION, this.overrideProperties));

		// Obtain the content
		if (expectedContent == null) {
			assertNull("Should not obtain configuration", item);

		} else {
			try {
				// Load the configuration
				StringWriter buffer = new StringWriter();
				Reader reader = item.getReader();
				for (int character = reader.read(); character >= 0; character = reader.read()) {
					buffer.write(character);
				}
				buffer.flush();

				// Ensure expected configuration
				assertEquals("Incorrect content", expectedContent, buffer.toString());
			} catch (IOException ex) {
				throw fail(ex);
			}
		}

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Verifies {@link Charset} output test.
	 * 
	 * @param charset         {@link Charset} to use for output.
	 * @param expectedContent Expected content.
	 */
	private void verify(Charset charset, String expectedContent) {
		this.replayMockObjects();

		// Obtain the configuration item
		ConfigurationItem item = this.context.getConfigurationItem(LOCATION, null);

		try {
			// Obtain the configuration content
			InputStream input = item.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			for (int byteValue = input.read(); byteValue >= 0; byteValue = input.read()) {
				buffer.write(byteValue);
			}
			buffer.flush();

			// Obtain the content
			String actualContent = new String(buffer.toByteArray(), charset);
			assertEquals("Incorrect output content", expectedContent, actualContent);

		} catch (IOException ex) {
			throw fail(ex);
		}

		// Verify functionality
		this.verifyMockObjects();
	}

}
