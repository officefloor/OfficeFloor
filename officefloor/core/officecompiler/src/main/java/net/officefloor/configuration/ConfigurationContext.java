/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.configuration;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.source.UnknownResourceError;

/**
 * Context of the {@link ConfigurationItem} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConfigurationContext {

	/**
	 * Name of the {@link Property} containing the {@link Charset} to use for
	 * the {@link ConfigurationItem} {@link Reader}. Should this not be
	 * specified, the {@link Charset#defaultCharset()} will be used.
	 */
	String PROPERTY_CONFIGURATION_INPUT_CHARSET = "configuration.input.charset";

	/**
	 * Name of {@link Property} containing the prefix of the tag name.
	 */
	String PROPERTY_CONFIGURATION_INPUT_TAG_PREFIX = "configuration.input.tag.prefix";

	/**
	 * Default tag prefix.
	 */
	String DEFAULT_TAG_PREFIX = "${";

	/**
	 * Name of {@link Property} containing the suffix of the tag name.
	 */
	String PROPERTY_CONFIGURATION_INPUT_TAG_SUFFIX = "configuration.input.tag.suffix";

	/**
	 * Default tag suffix.
	 */
	String DEFAULT_TAG_SUFFIX = "}";

	/**
	 * {@link Charset} to use for the {@link InputStream} for the
	 * {@link ConfigurationItem}. Should this not be specified, the
	 * {@link #PROPERTY_CONFIGURATION_INPUT_CHARSET} will be used (followed by
	 * its defaults).
	 */
	String PROPERTY_CONFIGURATION_OUTPUT_CHARSET = "configuration.output.charset";

	/**
	 * <p>
	 * Obtains the {@link ConfigurationItem} at the location.
	 * <p>
	 * Tags within the configuration are replaced by the {@link PropertyList}
	 * following naming convention <code>${PropertyName}</code>.
	 * 
	 * @param location
	 *            Location of the {@link ConfigurationItem} to obtain.
	 * @param overrideProperties
	 *            Override {@link PropertyList} for tag replacement. May be
	 *            <code>null</code>.
	 * @return {@link ConfigurationItem}.
	 * @throws UnknownResourceError
	 *             Let this propagate to let OfficeFloor handle the
	 *             {@link ConfigurationItem} not available at the location.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	ConfigurationItem getConfigurationItem(String location, PropertyList overrideProperties)
			throws UnknownResourceError, ConfigurationError;

	/**
	 * <p>
	 * Obtains the optional {@link ConfigurationItem} at the location. May
	 * return <code>null</code> if not {@link ConfigurationItem} at the
	 * location.
	 * <p>
	 * Also, undertakes tag replacement from the {@link PropertyList}.
	 * 
	 * @param location
	 *            Location of the {@link ConfigurationItem} to obtain.
	 * @param overrideProperties
	 *            Override {@link PropertyList} for tag replacement. May be
	 *            <code>null</code>.
	 * @return {@link ConfigurationItem} or <code>null</code> if no
	 *         configuration at location.
	 * @throws ConfigurationError
	 *             Let this propagate to let OfficeFloor handle failure in
	 *             loading {@link ConfigurationItem}.
	 */
	ConfigurationItem getOptionalConfigurationItem(String location, PropertyList overrideProperties)
			throws ConfigurationError;

}
