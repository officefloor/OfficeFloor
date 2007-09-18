/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.api.build.OfficeScope;
import net.officefloor.frame.impl.execute.AdministratorMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.configuration.ConfigurationException;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;

/**
 * Raw meta-data of the
 * {@link net.officefloor.frame.spi.administration.source.AdministratorSource}.
 * 
 * @author Daniel
 */
public class RawAdministratorMetaData {

	/**
	 * Creates a {@link RawAdministratorMetaData}.
	 * 
	 * @param asConfig
	 *            {@link AdministratorSourceConfiguration}.
	 * @return {@link RawAdministratorMetaData}.
	 * @throws Exception
	 *             If fails to create the meta-data.
	 */
	@SuppressWarnings("unchecked")
	public static RawAdministratorMetaData createRawAdministratorMetaData(
			AdministratorSourceConfiguration asConfig) throws Exception {
		// Create the instance of the task administrator source
		AdministratorSource<?, ?> source;
		try {
			source = asConfig.getAdministratorSourceClass().newInstance();
		} catch (InstantiationException ex) {
			throw new ConfigurationException(ex.getClass().getName() + ": "
					+ ex.getMessage());
		} catch (IllegalAccessException ex) {
			throw new ConfigurationException(ex.getClass().getName() + ": "
					+ ex.getMessage());
		}

		// Create the context
		AdministratorSourceContext context = new AdministratorSourceContextImpl(
				asConfig.getProperties());

		// Initialise the Administrator Source
		source.init(context);

		// Obtain the scope of the Administrator
		OfficeScope adminScope = asConfig.getAdministratorScope();

		// Return the raw Managed Object meta-data
		return new RawAdministratorMetaData(asConfig, source, adminScope);
	}

	/**
	 * {@link AdministratorSourceConfiguration}.
	 */
	protected final AdministratorSourceConfiguration adminSourceConfig;

	/**
	 * {@link AdministratorSource}.
	 */
	protected final AdministratorSource administratorSource;

	/**
	 * {@link OfficeScope}.
	 */
	protected final OfficeScope adminScope;

	/**
	 * Listing of {@link AdministratorMetaData} for this
	 * {@link RawAdministratorMetaData}.
	 */
	protected final List<AdministratorMetaDataImpl> adminMetaData = new LinkedList<AdministratorMetaDataImpl>();

	/**
	 * Index of this
	 * {@link net.officefloor.frame.spi.administration.Administrator} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	protected int processScopeIndex = -1;

	/**
	 * Initiate detail.
	 * 
	 * @param adminSourceConfig
	 *            {@link AdministratorSourceConfiguration}.
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param adminScope
	 *            {@link OfficeScope}.
	 */
	private RawAdministratorMetaData(
			AdministratorSourceConfiguration adminSourceConfig,
			AdministratorSource administratorSource, OfficeScope adminScope) {
		this.adminSourceConfig = adminSourceConfig;
		this.administratorSource = administratorSource;
		this.adminScope = adminScope;
	}

	/**
	 * Obtains the {@link AdministratorSourceConfiguration}.
	 * 
	 * @return {@link AdministratorSourceConfiguration}.
	 */
	public AdministratorSourceConfiguration getAdministratorSourceConfiguration() {
		return this.adminSourceConfig;
	}

	/**
	 * Obtains the {@link AdministratorSource}.
	 * 
	 * @return {@link AdministratorSource}.
	 */
	public AdministratorSource getAdministratorSource() {
		return this.administratorSource;
	}

	/**
	 * Obtains the {@link OfficeScope} of the
	 * {@link net.officefloor.frame.spi.administration.Administrator}.
	 * 
	 * @return {@link OfficeScope} of the
	 *         {@link net.officefloor.frame.spi.administration.Administrator}.
	 */
	public OfficeScope getAdministratorScope() {
		return this.adminScope;
	}

	/**
	 * Obtains the {@link AdministratorMetaData} listing.
	 * 
	 * @return {@link AdministratorMetaData} listing.
	 */
	public List<AdministratorMetaDataImpl> getAdministratorMetaData() {
		return this.adminMetaData;
	}

	/**
	 * Creates a new {@link AdministratorMetaData} proxied to the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @return New {@link AdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public AdministratorMetaData createAdministratorMetaData() {
		// Create the administrator meta-data
		AdministratorMetaDataImpl metaData = new AdministratorMetaDataImpl(
				this.processScopeIndex);

		// Register the meta-data
		this.adminMetaData.add(metaData);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Creates a new {@link AdministratorMetaData} to administer the input
	 * extension interfaces.
	 * 
	 * @return New {@link AdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public AdministratorMetaData createAdministratorMetaData(
			ExtensionInterfaceMetaData<?>[] eiMetaData) {
		// Create the administrator meta-data
		AdministratorMetaDataImpl metaData = new AdministratorMetaDataImpl(
				this.administratorSource, eiMetaData);

		// Register the meta-data
		this.adminMetaData.add(metaData);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Specifies the index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @param processStateIndex
	 *            Index for the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            within the
	 *            {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	public void setProcessStateIndex(int processStateIndex) {
		this.processScopeIndex = processStateIndex;
	}

	/**
	 * Obtains the index of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} within the
	 * {@link net.officefloor.frame.internal.structure.ProcessState}.
	 * 
	 * @return Index of the
	 *         {@link net.officefloor.frame.spi.administration.Administrator}
	 *         within the
	 *         {@link net.officefloor.frame.internal.structure.ProcessState}.
	 */
	public int getProcessStateIndex() {
		return this.processScopeIndex;
	}

}