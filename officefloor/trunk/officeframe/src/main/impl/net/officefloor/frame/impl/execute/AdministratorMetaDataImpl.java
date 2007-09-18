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
package net.officefloor.frame.impl.execute;

import java.util.Map;

import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.AdministratorMetaData}.
 * 
 * @author Daniel
 */
public class AdministratorMetaDataImpl<I extends Object, A extends Enum<A>>
		implements AdministratorMetaData<I, A> {

	/**
	 * Index of the {@link Administrator} for this
	 * {@link net.officefloor.frame.internal.structure.AdministratorContainer}
	 * within the {@link ProcessState} or
	 * {@link AdministratorMetaData#NON_PROCESS_INDEX}.
	 */
	protected final int processStateAdministratorIndex;

	/**
	 * {@link AdministratorSource}.
	 */
	protected final AdministratorSource<I, A> administratorSource;

	/**
	 * {@link ExtensionInterfaceMetaData}.
	 */
	protected final ExtensionInterfaceMetaData<I>[] eiMetaData;

	/**
	 * <p>
	 * Registry of {@link DutyMetaData} by its
	 * {@link net.officefloor.frame.spi.administration.Duty} key.
	 * <p>
	 * This is treated as <code>final</code>.
	 */
	protected Map<A, DutyMetaData> dutyMetaData;

	/**
	 * Initiate with meta-data of the {@link Administrator} scoped to the
	 * {@link ProcessState}.
	 * 
	 * @param processStateAdministratorIndex
	 *            Index of the {@link Administrator} within the
	 *            {@link ProcessState}.
	 */
	public AdministratorMetaDataImpl(int processStateAdministratorIndex) {
		this.processStateAdministratorIndex = processStateAdministratorIndex;
		this.eiMetaData = null;
		this.administratorSource = null;
	}

	/**
	 * Initiate with meta-data of the {@link Administrator} scope to the
	 * {@link net.officefloor.frame.api.execute.Work}.
	 * 
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param eiMetaData
	 *            {@link ExtensionInterfaceMetaData}.
	 */
	public AdministratorMetaDataImpl(
			AdministratorSource<I, A> administratorSource,
			ExtensionInterfaceMetaData<I>[] eiMetaData) {
		this.processStateAdministratorIndex = AdministratorMetaData.NON_PROCESS_INDEX;
		this.eiMetaData = eiMetaData;
		this.administratorSource = administratorSource;
	}

	/**
	 * Loads the remaining state.
	 * 
	 * @param dutyMetaData
	 *            {@link DutyMetaData} for each
	 *            {@link net.officefloor.frame.spi.administration.Duty} of the
	 *            {@link Administrator}.
	 */
	public void loadRemainingState(Map<A, DutyMetaData> dutyMetaData) {
		this.dutyMetaData = dutyMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getProcessStateAdministratorIndex()
	 */
	public int getProcessStateAdministratorIndex() {
		return this.processStateAdministratorIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getAdministratorSource()
	 */
	public AdministratorSource<I, A> getAdministratorSource() {
		return this.administratorSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getExtensionInterfaceMetaData()
	 */
	public ExtensionInterfaceMetaData<I>[] getExtensionInterfaceMetaData() {
		return this.eiMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.AdministratorMetaData#getDutyMetaData(A)
	 */
	public DutyMetaData getDutyMetaData(A key) {
		return this.dutyMetaData.get(key);
	}

}
