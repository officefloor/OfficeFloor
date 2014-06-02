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
package net.officefloor.plugin.gwt.comet.section;

import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.gwt.comet.internal.CometEvent;
import net.officefloor.plugin.gwt.comet.section.CometSectionSource;
import net.officefloor.plugin.gwt.comet.spi.CometRequestServicer;
import net.officefloor.plugin.gwt.comet.spi.CometService;
import net.officefloor.plugin.gwt.service.ServerGwtRpcConnection;

/**
 * Tests the {@link CometSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CometSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		SectionLoaderUtil.validateSpecification(CometSectionSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil
				.createSectionDesigner(CometSectionSource.class);
		type.addSectionInput(CometSectionSource.SUBSCRIBE_INPUT_NAME, null);
		type.addSectionInput(CometSectionSource.PUBLISH_INPUT_NAME, null);
		type.addSectionObject(CometRequestServicer.class.getName(),
				CometRequestServicer.class.getName());
		type.addSectionObject(CometService.class.getName(),
				CometService.class.getName());
		type.addSectionObject(ServerGwtRpcConnection.class.getName(),
				ServerGwtRpcConnection.class.getName());

		// Validate type
		SectionLoaderUtil.validateSectionType(type, CometSectionSource.class,
				"COMET");
	}

	/**
	 * Validate type with manual publish.
	 */
	public void testTypeWithManualPublish() {

		// Create the expected type
		SectionDesigner type = SectionLoaderUtil
				.createSectionDesigner(CometSectionSource.class);
		type.addSectionInput(CometSectionSource.SUBSCRIBE_INPUT_NAME, null);
		type.addSectionInput(CometSectionSource.PUBLISH_INPUT_NAME, null);
		type.addSectionOutput(CometSectionSource.PUBLISH_OUTPUT_PREFIX
				+ "template", CometEvent.class.getName(), false);
		type.addSectionObject(CometRequestServicer.class.getName(),
				CometRequestServicer.class.getName());
		type.addSectionObject(CometService.class.getName(),
				CometService.class.getName());
		type.addSectionObject(ServerGwtRpcConnection.class.getName(),
				ServerGwtRpcConnection.class.getName());

		// Validate type
		SectionLoaderUtil.validateSectionType(type, CometSectionSource.class,
				"COMET", CometSectionSource.PROPERTY_MANUAL_PUBLISH_URI_PREFIX
						+ "template", "template");
	}

}