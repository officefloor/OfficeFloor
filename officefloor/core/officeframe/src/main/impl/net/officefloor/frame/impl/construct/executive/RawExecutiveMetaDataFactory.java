/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.executive;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.OfficeFloorIssueTarget;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Factory for the construction of the {@link RawExecutiveMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawExecutiveMetaDataFactory {

	/**
	 * {@link AssetType} name for the {@link Executive}.
	 */
	private final String EXECUTIVE_NAME = "Executive";

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext {@link SourceContext}.
	 */
	public RawExecutiveMetaDataFactory(SourceContext sourceContext) {
		this.sourceContext = sourceContext;
	}

	/**
	 * Creates the {@link RawExecutiveMetaData}.
	 *
	 * @param                 <XS> {@link ExecutiveSource} type.
	 * @param configuration   {@link ExecutiveConfiguration}.
	 * @param officeFloorName Name of the {@link OfficeFloor}.
	 * @param issues          {@link OfficeFloorIssues}.
	 * @return {@link RawExecutiveMetaData} or <code>null</code> if fails to
	 *         construct.
	 */
	public <XS extends ExecutiveSource> RawExecutiveMetaData constructRawExecutiveMetaData(
			ExecutiveConfiguration<XS> configuration, String officeFloorName, OfficeFloorIssues issues) {

		// Obtain the executive source
		XS executiveSource = configuration.getExecutiveSource();
		if (executiveSource == null) {
			Class<XS> executiveSourceClass = configuration.getExecutiveSourceClass();
			if (executiveSourceClass == null) {
				issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "No ExecutiveSource class provided");
				return null; // can not carry on
			}

			// Instantiate the executive source
			executiveSource = ConstructUtil.newInstance(executiveSourceClass, ExecutiveSource.class, "Executive Source",
					AssetType.EXECUTIVE, EXECUTIVE_NAME, issues);
			if (executiveSource == null) {
				return null; // can not carry on
			}
		}

		Executive executive;
		try {
			// Create the team source context
			SourceProperties properties = configuration.getProperties();
			ExecutiveSourceContextImpl context = new ExecutiveSourceContextImpl(false, this.sourceContext, properties);

			// Create the executive
			executive = executiveSource.createExecutive(context);
			if (executive == null) {
				// Indicate failed to provide executive
				issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, ExecutiveSource.class.getSimpleName()
						+ " failed to provide " + Executive.class.getSimpleName());
				return null; // can not carry on
			}

		} catch (AbstractSourceError ex) {
			ex.addIssue(new OfficeFloorIssueTarget(issues, AssetType.EXECUTIVE, EXECUTIVE_NAME));
			return null; // can not carry on

		} catch (Throwable ex) {
			// Indicate failure to initialise
			issues.addIssue(AssetType.EXECUTIVE, EXECUTIVE_NAME, "Failed to create " + Executive.class.getSimpleName(),
					ex);
			return null; // can not carry on
		}

		// Return the executive meta-data
		return new RawExecutiveMetaData(executive);
	}

}