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
package net.officefloor.frame.impl.construct.administrator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.administrator.AdministratorIndexImpl;
import net.officefloor.frame.impl.execute.administrator.AdministratorMetaDataImpl;
import net.officefloor.frame.impl.execute.administrator.ExtensionInterfaceMetaDataImpl;
import net.officefloor.frame.impl.execute.duty.DutyMetaDataImpl;
import net.officefloor.frame.internal.configuration.AdministratorSourceConfiguration;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.AdministratorScope;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.DutyMetaData;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.ExtensionInterfaceMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceContext;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.spi.managedobject.extension.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data for the bound {@link Administrator}.
 * 
 * @author Daniel
 */
public class RawBoundAdministratorMetaDataImpl<I, A extends Enum<A>> implements
		RawBoundAdministratorMetaDataFactory,
		RawBoundAdministratorMetaData<I, A> {

	/**
	 * Obtains the {@link RawBoundAdministratorMetaDataFactory}.
	 * 
	 * @return {@link RawBoundAdministratorMetaDataFactory}.
	 */
	@SuppressWarnings("unchecked")
	public static RawBoundAdministratorMetaDataFactory getFactory() {
		return new RawBoundAdministratorMetaDataImpl(null, null, null, null,
				null, null);
	}

	/**
	 * Name of the {@link Administrator}.
	 */
	private final String administratorName;

	/**
	 * {@link AdministratorIndex}.
	 */
	private final AdministratorIndex administratorIndex;

	/**
	 * {@link AdministratorSource}.
	 */
	private final AdministratorSource<I, A> administratorSource;

	/**
	 * {@link Team} responsible for the {@link Administrator} {@link Duty}
	 * instances.
	 */
	private final Team team;

	/**
	 * Duty keys.
	 */
	private final A[] dutyKeys;

	/**
	 * Listing of {@link RawAdministeredManagedObjectMetaData}.
	 */
	private final RawAdministeredManagedObjectMetaData<I>[] rawAdministeredManagedObjects;

	/**
	 * Initiate.
	 * 
	 * @param administratorName
	 *            Name of the {@link Administrator}.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param administratorSource
	 *            {@link AdministratorSource}.
	 * @param team
	 *            {@link Team}.
	 * @param dutyKeys
	 *            Keys to the {@link Duty} instances.
	 * @param rawAdministeredManagedObjects
	 *            Listing of {@link RawAdministeredManagedObjectMetaData}.
	 */
	private RawBoundAdministratorMetaDataImpl(
			String administratorName,
			AdministratorIndex administratorIndex,
			AdministratorSource<I, A> administratorSource,
			Team team,
			A[] dutyKeys,
			RawAdministeredManagedObjectMetaData<I>[] rawAdministeredManagedObjects) {
		this.administratorName = administratorName;
		this.administratorIndex = administratorIndex;
		this.administratorSource = administratorSource;
		this.team = team;
		this.dutyKeys = dutyKeys;
		this.rawAdministeredManagedObjects = rawAdministeredManagedObjects;
	}

	/*
	 * =========== RawBoundAdministratorMetaDataFactory ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public RawBoundAdministratorMetaData<?, ?>[] constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<?, ?>[] configuration,
			OfficeFloorIssues issues, AdministratorScope administratorScope,
			AssetType assetType, String assetName,
			Map<String, Team> officeTeams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo) {

		// Register the bound administrators
		List<RawBoundAdministratorMetaData<?, ?>> boundAdministrators = new LinkedList<RawBoundAdministratorMetaData<?, ?>>();
		int boundAdminIndex = 0;
		for (AdministratorSourceConfiguration config : configuration) {

			// Create the administrator index
			AdministratorIndex adminIndex = new AdministratorIndexImpl(
					administratorScope, boundAdminIndex++);

			// Construct the bound administrator
			RawBoundAdministratorMetaData<?, ?> rawMetaData = constructRawBoundAdministratorMetaData(
					config, issues, adminIndex, assetType, assetName,
					officeTeams, scopeMo);
			if (rawMetaData != null) {
				boundAdministrators.add(rawMetaData);
			}
		}

		// Return the bound administrators
		return boundAdministrators
				.toArray(new RawBoundAdministratorMetaData[0]);
	}

	/**
	 * Provides typed construction of a {@link RawBoundAdministratorMetaData}.
	 * 
	 * @param configuration
	 *            {@link AdministratorSourceConfiguration} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @param administratorIndex
	 *            {@link AdministratorIndex}.
	 * @param assetType
	 *            {@link AssetType} constructing {@link Administrator}
	 *            instances.
	 * @param assetName
	 *            Name of {@link Asset} constructing {@link Administrator}
	 *            instances.
	 * @param officeTeams
	 *            {@link Team} instances by their {@link Office} registered
	 *            names.
	 * @param scopeMo
	 *            {@link RawBoundManagedObjectMetaData} by their scope names.
	 * @return Constructed {@link RawBoundAdministratorMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private <a extends Enum<a>, i, AS extends AdministratorSource<i, a>> RawBoundAdministratorMetaData<i, a> constructRawBoundAdministratorMetaData(
			AdministratorSourceConfiguration<a, AS> configuration,
			OfficeFloorIssues issues, AdministratorIndex administratorIndex,
			AssetType assetType, String assetName,
			Map<String, Team> officeTeams,
			Map<String, RawBoundManagedObjectMetaData<?>> scopeMo) {

		// Obtain the administrator name
		String adminName = configuration.getAdministratorName();
		if (ConstructUtil.isBlank(adminName)) {
			issues.addIssue(assetType, assetName,
					"Administrator added without a name");
			return null; // no name
		}

		// Obtain the administrator source
		Class<AS> adminSourceClass = configuration
				.getAdministratorSourceClass();
		if (adminSourceClass == null) {
			issues.addIssue(assetType, assetName, "Administrator '" + adminName
					+ "' did not provide an "
					+ AdministratorSource.class.getSimpleName() + " class");
			return null; // no class
		}

		// Obtain the administrator source instance
		AS adminSource = (AS) ConstructUtil.newInstance(adminSourceClass,
				AdministratorSource.class, "Administrator '" + adminName + "'",
				assetType, assetName, issues);
		if (adminSource == null) {
			return null; // no instance
		}

		// Initialise the administrator source
		Properties properties = configuration.getProperties();
		AdministratorSourceContext context = new AdministratorSourceContextImpl(
				properties);
		try {
			adminSource.init(context);
		} catch (Throwable ex) {
			issues.addIssue(assetType, assetName,
					"Failed to initialise Administrator " + adminName, ex);
			return null; // not initialised
		}

		// Ensure have the meta-data
		AdministratorSourceMetaData<i, a> metaData = adminSource.getMetaData();
		if (metaData == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " must provide "
					+ AdministratorSourceMetaData.class.getSimpleName());
			return null; // must provide meta-data
		}

		// Obtain the team responsible for the duties
		String teamName = configuration.getOfficeTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " must specify team responsible for duties");
			return null; // must have team specified
		}
		Team team = officeTeams.get(teamName);
		if (team == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " team '" + teamName + "' can not be found");
			return null; // unknown team
		}

		// Obtain the extension interface
		Class<i> extensionInterfaceType = metaData.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " did not provide extension interface type");
			return null; // no extension interface
		}

		// Obtain the managed objects being administered
		List<RawAdministeredManagedObjectMetaData<i>> administeredMo = new LinkedList<RawAdministeredManagedObjectMetaData<i>>();
		for (String moName : configuration.getAdministeredManagedObjectNames()) {

			// Ensure have managed object name
			if (ConstructUtil.isBlank(moName)) {
				issues.addIssue(assetType, assetName, "Administrator "
						+ adminName + " specifying no name for managed object");
				return null; // unspecified managed object name
			}

			// Obtain the managed object
			RawBoundManagedObjectMetaData<?> mo = scopeMo.get(moName);
			if (mo == null) {
				issues.addIssue(assetType, assetName, "Managed Object '"
						+ moName + "' not available to Administrator "
						+ adminName);
				return null; // unknown managed object
			}

			// Obtain the extension factory for the managed object
			ExtensionInterfaceFactory<i> extensionInterfaceFactory = null;
			ManagedObjectExtensionInterfaceMetaData<?>[] eiMetaDatas = mo
					.getRawManagedObjectMetaData()
					.getManagedObjectSourceMetaData()
					.getExtensionInterfacesMetaData();
			if (eiMetaDatas != null) {
				for (ManagedObjectExtensionInterfaceMetaData<?> eiMetaData : eiMetaDatas) {

					// Obtain the extension interface
					Class<?> moEiType = eiMetaData.getExtensionInterfaceType();
					if ((moEiType != null)
							&& (extensionInterfaceType
									.isAssignableFrom(moEiType))) {

						// Specify the extension interface factory
						extensionInterfaceFactory = (ExtensionInterfaceFactory<i>) eiMetaData
								.getExtensionInterfaceFactory();
						if (extensionInterfaceFactory == null) {
							issues
									.addIssue(
											assetType,
											assetName,
											"Managed Object did not provide "
													+ ExtensionInterfaceFactory.class
															.getSimpleName()
													+ " for Administrator "
													+ adminName);
							return null; // managed object invalid
						}
						break; // have extension interface factory
					}
				}
			}
			if (extensionInterfaceFactory == null) {
				issues.addIssue(assetType, assetName, "Managed Object '"
						+ moName + "' does not support extension interface "
						+ extensionInterfaceType.getName()
						+ " required by Administrator " + adminName);
				return null; // managed object invalid
			}

			// Add the administered managed object
			administeredMo.add(new RawAdministeredManagedObjectMetaDataImpl<i>(
					mo, extensionInterfaceFactory));
		}

		// Obtain the keys to the duties
		Class<a> dutyKeyClass = metaData.getAministratorDutyKeys();
		a[] dutyKeys = (dutyKeyClass == null ? null : dutyKeyClass
				.getEnumConstants());
		if ((dutyKeys == null) || (dutyKeys.length == 0)) {
			issues.addIssue(assetType, assetName, "Administrator " + adminName
					+ " does not provide duties");
			return null; // must have duties
		}

		// Ensure the duty keys are in ordinal order
		Arrays.sort(dutyKeys, new Comparator<a>() {
			@Override
			public int compare(a objA, a objB) {
				return objA.ordinal() - objB.ordinal();
			}
		});

		// Return the constructed the bound meta-data
		return new RawBoundAdministratorMetaDataImpl<i, a>(adminName,
				administratorIndex, adminSource, team, dutyKeys, administeredMo
						.toArray(new RawAdministeredManagedObjectMetaData[0]));
	}

	/*
	 * ========= RawBoundAdministratorMetaData =========================
	 */

	@Override
	public AdministratorSource<I, A> getAdministratorSource() {
		return this.administratorSource;
	}

	@Override
	public AdministratorIndex getAdministratorIndex() {
		return this.administratorIndex;
	}

	@Override
	public String getAdministratorName() {
		return this.administratorName;
	}

	@Override
	public Team getResponsibleTeam() {
		return this.team;
	}

	@Override
	public A[] getDutyKeys() {
		return this.dutyKeys;
	}

	@Override
	public RawAdministeredManagedObjectMetaData<I>[] getAdministeredManagedObjectMetaData() {
		return this.rawAdministeredManagedObjects;
	}

	@Override
	@SuppressWarnings("unchecked")
	public AdministratorMetaData<?, ?> getAdministratorMetaData() {

		// Create the listing of managed object meta-data
		ExtensionInterfaceMetaData<I>[] eiMetaData = new ExtensionInterfaceMetaData[this.rawAdministeredManagedObjects.length];
		for (int i = 0; i < eiMetaData.length; i++) {

			// Obtain the details of extension interface
			RawAdministeredManagedObjectMetaData<I> adminMo = this.rawAdministeredManagedObjects[i];
			int index = -1; // TODO obtain the index
			ExtensionInterfaceFactory<I> factory = adminMo
					.getExtensionInterfaceFactory();

			// Create and load the extension interface meta-data
			eiMetaData[i] = new ExtensionInterfaceMetaDataImpl<I>(index,
					factory);
		}

		// TODO obtain the escalation procedure
		EscalationProcedure escalationProcedure = null;

		// Create the administrator meta-data
		AdministratorMetaDataImpl<I, A> metaData = new AdministratorMetaDataImpl<I, A>(
				this.administratorSource, eiMetaData, this.team,
				escalationProcedure);

		// TODO look to support duty invoking other tasks
		Map<A, DutyMetaData> dutyMetaData = new HashMap<A, DutyMetaData>();
		for (A dutyKey : this.dutyKeys) {
			dutyMetaData
					.put(dutyKey, new DutyMetaDataImpl(new FlowMetaData[0]));
		}
		metaData.loadRemainingState(dutyMetaData);

		// Returns the administrator meta-data
		return metaData;
	}

	/**
	 * {@link RawAdministeredManagedObjectMetaData} implementation.
	 */
	private static class RawAdministeredManagedObjectMetaDataImpl<i> implements
			RawAdministeredManagedObjectMetaData<i> {

		/**
		 * {@link RawBoundManagedObjectMetaData}.
		 */
		private final RawBoundManagedObjectMetaData<?> managedObjectMetaData;

		/**
		 * {@link ExtensionInterfaceFactory}.
		 */
		private final ExtensionInterfaceFactory<i> extensionInterfaceFactory;

		/**
		 * Initiate.
		 * 
		 * @param managedObjectMetaData
		 *            {@link RawBoundManagedObjectMetaData}.
		 * @param extensionInterfaceFactory
		 *            {@link ExtensionInterfaceFactory}.
		 */
		public RawAdministeredManagedObjectMetaDataImpl(
				RawBoundManagedObjectMetaData<?> managedObjectMetaData,
				ExtensionInterfaceFactory<i> extensionInterfaceFactory) {
			this.managedObjectMetaData = managedObjectMetaData;
			this.extensionInterfaceFactory = extensionInterfaceFactory;
		}

		/*
		 * ============== RawAdministeredManagedObjectMetaData ================
		 */

		@Override
		public RawBoundManagedObjectMetaData<?> getManagedObjectMetaData() {
			return this.managedObjectMetaData;
		}

		@Override
		public ExtensionInterfaceFactory<i> getExtensionInterfaceFactory() {
			return extensionInterfaceFactory;
		}
	}

}