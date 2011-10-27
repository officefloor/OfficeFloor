/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownPropertyError;
import net.officefloor.frame.spi.source.UnknownResourceError;

/**
 * Raw meta-data for a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataImpl<I, F extends Enum<F>> implements
		RawGovernanceMetaDataFactory, RawGovernanceMetaData {

	/**
	 * Obtains the {@link RawGovernanceMetaDataFactory}.
	 * 
	 * @return {@link RawGovernanceMetaDataFactory}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RawGovernanceMetaDataFactory getFactory() {
		return new RawGovernanceMetaDataImpl(null, -1, null, null);
	}

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Index of this {@link RawGovernanceMetaData} within the
	 * {@link ProcessState}.
	 */
	private final int governanceIndex;

	/**
	 * Extension interface type.
	 */
	private final Class<I> extensionInterfaceType;

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> governanceMetaData;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceIndex
	 *            Index of this {@link RawGovernanceMetaData} within the
	 *            {@link ProcessState}.
	 * @param extensionInterfaceType
	 *            Extension interface type.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaData}.
	 */
	public RawGovernanceMetaDataImpl(String governanceName,
			int governanceIndex, Class<I> extensionInterfaceType,
			GovernanceMetaData<I, F> governanceMetaData) {
		this.governanceName = governanceName;
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceType = extensionInterfaceType;
		this.governanceMetaData = governanceMetaData;
	}

	/*
	 * ==================== RawGovernanceMetaDataFactory ==================
	 */

	@Override
	public <i, f extends Enum<f>, GS extends GovernanceSource<i, f>> RawGovernanceMetaData createRawGovernanceMetaData(
			GovernanceConfiguration<i, f, GS> configuration,
			int governanceIndex, SourceContext sourceContext,
			String officeName, OfficeFloorIssues issues) {

		// Obtain the governance name
		String governanceName = configuration.getGovernanceName();
		if (ConstructUtil.isBlank(governanceName)) {
			issues.addIssue(AssetType.OFFICE, officeName,
					"Governance added without a name");
			return null; // can not carry on
		}

		// Attempt to obtain the governance source
		GS governanceSource = configuration.getGovernanceSource();
		if (governanceSource == null) {
			// No instance, so by governance source class
			Class<GS> governanceSourceClass = configuration
					.getGovernanceSourceClass();
			if (governanceSourceClass == null) {
				issues.addIssue(AssetType.GOVERNANCE, governanceName,
						"No GovernanceSource class provided");
				return null; // can not carry on
			}

			// Instantiate the managed object source
			governanceSource = ConstructUtil.newInstance(governanceSourceClass,
					GovernanceSource.class, "Governance Source '"
							+ governanceName + "'", AssetType.GOVERNANCE,
					governanceName, issues);
			if (governanceSource == null) {
				return null; // can not carry on
			}
		}

		// Obtain the properties to initialise the managed object source
		SourceProperties properties = configuration.getProperties();

		// Create the context for the governance source
		GovernanceSourceContextImpl context = new GovernanceSourceContextImpl(
				properties, sourceContext);

		try {
			// Initialise the governance source
			governanceSource.init(context);

		} catch (UnknownPropertyError ex) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName, "Property '"
					+ ex.getUnknownPropertyName() + "' must be specified");
			return null; // can not carry on

		} catch (UnknownClassError ex) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Can not load class '" + ex.getUnknownClassName() + "'");
			return null; // can not carry on

		} catch (UnknownResourceError ex) {
			issues.addIssue(
					AssetType.GOVERNANCE,
					governanceName,
					"Can not obtain resource at location '"
							+ ex.getUnknownResourceLocation() + "'");
			return null; // can not carry on

		} catch (Throwable ex) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Failed to initialise "
							+ governanceSource.getClass().getName(), ex);
			return null; // can not carry on
		}

		// Flag initialising over
		context.flagInitOver();

		// Obtain the meta-data
		GovernanceSourceMetaData<i, f> metaData = governanceSource
				.getMetaData();
		if (metaData == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Must provide meta-data");
			return null; // can not carry on
		}

		// Obtain the extension interface type
		Class<i> extensionInterfaceType = metaData.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No extension interface type provided");
			return null; // can not carry on
		}

		// Create the Governance Meta-Data
		GovernanceMetaData<i, f> governanceMetaData = new GovernanceMetaDataImpl<i, f>(
				governanceName);

		// Create the raw Governance meta-data
		RawGovernanceMetaData rawGovernanceMetaData = new RawGovernanceMetaDataImpl<i, f>(
				governanceName, governanceIndex, extensionInterfaceType,
				governanceMetaData);

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

	/*
	 * =================== RawGovernanceMetaDataMetaData ==================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public Class<?> getExtensionInterfaceType() {
		return this.extensionInterfaceType;
	}

	@Override
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	@Override
	public GovernanceMetaData<I, F> getGovernanceMetaData() {
		return this.governanceMetaData;
	}

}