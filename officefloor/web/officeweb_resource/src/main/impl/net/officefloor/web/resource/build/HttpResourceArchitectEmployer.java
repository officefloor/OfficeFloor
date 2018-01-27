/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.resource.build;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.spi.ResourceSystemService;

/**
 * Employs a {@link HttpResourceArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceArchitectEmployer implements HttpResourceArchitect {

	/**
	 * Employs the {@link HttpResourceArchitect}.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 * @return {@link HttpResourceArchitect}.
	 */
	public static HttpResourceArchitect employHttpResourceArchitect(WebArchitect webArchitect,
			OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) {
		return new HttpResourceArchitectEmployer(webArchitect, officeArchitect, officeSourceContext);
	}

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link OfficeSourceContext}.
	 */
	private final OfficeSourceContext officeSourceContext;

	/**
	 * {@link ResourceLink} instances.
	 */
	private final List<ResourceLink> resourceLinks = new LinkedList<>();

	/**
	 * {@link EscalationResource} instances.
	 */
	private final List<EscalationResource> escalationResources = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param webArchitect
	 *            {@link WebArchitect}.
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeSourceContext}.
	 */
	private HttpResourceArchitectEmployer(WebArchitect webArchitect, OfficeArchitect officeArchitect,
			OfficeSourceContext officeSourceContext) {
		this.webArchitect = webArchitect;
		this.officeArchitect = officeArchitect;
		this.officeSourceContext = officeSourceContext;
	}

	/*
	 * =================== HttpResourceArchitect ========================
	 */

	@Override
	public void link(OfficeSectionOutput output, String resourcePath) {
		this.resourceLinks.add(new ResourceLink(output, resourcePath));
	}

	@Override
	public void link(OfficeEscalation escalation, String resourcePath) {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpResourcesBuilder addHttpResources(ResourceSystemService resourceSystemFactory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResourcesBuilder addHttpResources(String protocolLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void informWebArchitect() {

		// Link to resources
		if ((this.resourceLinks.size() > 0) || (this.escalationResources.size() > 0)) {

		}
	}

	/**
	 * Resource link.
	 */
	private static class ResourceLink {

		/**
		 * {@link OfficeSectionOutput}.
		 */
		public final OfficeSectionOutput sectionOutput;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link OfficeSection}.
		 * @param outputName
		 *            Name of the {@link SectionOutput}.
		 * @param resourcePath
		 *            Resource path.
		 */
		public ResourceLink(OfficeSectionOutput sectionOutput, String resourcePath) {
			this.sectionOutput = sectionOutput;
			this.resourcePath = resourcePath;
		}
	}

	/**
	 * Resource to handle {@link Escalation}.
	 */
	private static class EscalationResource {

		/**
		 * {@link Escalation} type.
		 */
		public final Class<? extends Throwable> escalationType;

		/**
		 * Resource path.
		 */
		public final String resourcePath;

		/**
		 * Initiate.
		 * 
		 * @param escalationType
		 *            {@link Escalation} type.
		 * @param resourcePath
		 *            Resource path.
		 */
		public EscalationResource(Class<? extends Throwable> escalationType, String resourcePath) {
			this.escalationType = escalationType;
			this.resourcePath = resourcePath;
		}
	}

}