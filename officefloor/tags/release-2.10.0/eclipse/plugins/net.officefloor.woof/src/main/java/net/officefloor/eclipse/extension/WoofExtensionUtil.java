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
package net.officefloor.eclipse.extension;

import java.util.List;
import java.util.Map;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.extension.access.HttpSecuritySourceExtension;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * Utility class for working with extensions.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofExtensionUtil extends ExtensionUtil {

	/**
	 * Obtains the extension id for the input extension name.
	 * 
	 * @param extensionName
	 *            Name of the extension.
	 * @return Id for the extension.
	 */
	public static String getExtensionId(String name) {
		return WoofPlugin.PLUGIN_ID + "." + name;
	}

	/**
	 * {@link SourceClassExtractor} for the {@link HttpSecuritySourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<HttpSecuritySourceExtension> HTTP_SECURITY_SOURCE_CLASS_EXTRACTOR = new SourceClassExtractor<HttpSecuritySourceExtension>() {
		@Override
		public Class<?> getSourceClass(
				HttpSecuritySourceExtension sourceExtension) {
			return sourceExtension.getHttpSecuritySourceClass();
		}
	};

	/**
	 * Creates the map of {@link HttpSecuritySourceExtension} instances by their
	 * respective {@link HttpSecuritySource} class name.
	 * 
	 * @return Map of {@link HttpSecuritySourceExtension} instances by their
	 *         respective {@link HttpSecuritySource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, HttpSecuritySourceExtension> createHttpSecuritySourceExtensionMap() {
		return ExtensionUtil.createSourceExtensionMap(
				HttpSecuritySourceExtension.EXTENSION_ID,
				HttpSecuritySourceExtension.class,
				HTTP_SECURITY_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link SectionSourceExtension} instances.
	 * 
	 * @return Listing of {@link SectionSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<HttpSecuritySourceExtension> createHttpSecuritySourceExtensionList() {
		return createSourceExtensionList(createHttpSecuritySourceExtensionMap());
	}

	/**
	 * All access via static methods.
	 */
	private WoofExtensionUtil() {
	}

}