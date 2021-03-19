/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.supply;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.chain.ServletSectionSource;
import net.officefloor.servlet.inject.FieldDependencyExtractor;
import net.officefloor.servlet.inject.FieldDependencyExtractorServiceFactory;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.tomcat.TomcatServletManager;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * {@link WoofExtensionService} to provide a {@link Servlet} container.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletWoofExtensionService implements WoofExtensionServiceFactory, WoofExtensionService {

	/**
	 * {@link Property} to specify path to web application (WAR).
	 */
	public static final String PROPERTY_WAR_PATH = "war.path";

	/**
	 * Obtains the {@link Property} name for
	 * {@link ServletSupplierSource#PROPERTY_CHAIN_SERVLETS}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return {@link Property} name for
	 *         {@link ServletSupplierSource#PROPERTY_CHAIN_SERVLETS}.
	 */
	public static String getChainServletsPropertyName(String officeName) {
		return officeName + ".SERVLET." + ServletSupplierSource.PROPERTY_CHAIN_SERVLETS;
	}

	/*
	 * ================ WoofExtensionServiceFactory ===================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== WoofExtensionService =========================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Load possible location of web application
		String webAppPath = extensionContext.getProperty(PROPERTY_WAR_PATH, null);

		// Load the field dependency extractors
		List<FieldDependencyExtractor> extractors = new LinkedList<>();
		for (FieldDependencyExtractor extractor : extensionContext
				.loadOptionalServices(FieldDependencyExtractorServiceFactory.class)) {
			extractors.add(extractor);
		}

		// Create the injector registry
		InjectionRegistry injectionRegistry = new InjectionRegistry(
				extractors.toArray(new FieldDependencyExtractor[extractors.size()]));

		// Create the embedded servlet container
		TomcatServletManager servletContainer = new TomcatServletManager("/", injectionRegistry, extensionContext,
				webAppPath);

		// Register the Servlet Supplier (to capture required inject thread locals)
		office.addSupplier("SERVLET", new ServletSupplierSource(servletContainer, injectionRegistry));

		// Hook in WebApp servicing
		OfficeSection servicer = office.addOfficeSection("SERVLET_SERVICER_SECTION",
				new ServletSectionSource(servletContainer), null);
		context.getWebArchitect().chainServicer(servicer.getOfficeSectionInput(ServletSectionSource.INPUT),
				servicer.getOfficeSectionOutput(ServletSectionSource.OUTPUT));
	}

}
