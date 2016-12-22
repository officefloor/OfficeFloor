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
package net.officefloor.autowire;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * Application which has auto-wired configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireApplication {

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorCompiler} being used.
	 * <p>
	 * This allows manipulation of the {@link OfficeFloorCompiler} before
	 * auto-wiring to compile and open the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloorCompiler} being used.
	 */
	OfficeFloorCompiler getOfficeFloorCompiler();

	/**
	 * Adds an {@link AutoWireSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource}. May be an alias.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	AutoWireSection addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation);

	/**
	 * Adds an {@link AutoWireSection} with the ability to override the
	 * {@link AutoWireSection} used.
	 * 
	 * @param <A>
	 *            {@link AutoWireSection} type.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClassName
	 *            Class name of the {@link SectionSource}. May be an alias.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @param sectionFactory
	 *            {@link AutoWireSectionFactory} to allow overriding the
	 *            {@link AutoWireSection} utilised.
	 * @return Overridden {@link AutoWireSection} to configure properties and
	 *         link flows.
	 */
	<A extends AutoWireSection> AutoWireSection addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			AutoWireSectionFactory<A> sectionFactory);

	/**
	 * <p>
	 * Obtains the {@link AutoWireSection} by its name.
	 * <p>
	 * This is useful to obtain an existing {@link AutoWireSection} to link to.
	 * 
	 * @param sectionName
	 *            Name of the {@link AutoWireSection}.
	 * @return {@link AutoWireSection} or <code>null</code> if not
	 *         {@link AutoWireSection} by the name.
	 */
	AutoWireSection getSection(String sectionName);

	/**
	 * Adds a {@link AutoWireSectionTransformer}.
	 * 
	 * @param transformer
	 *            {@link AutoWireSectionTransformer}.
	 */
	void addSectionTransformer(AutoWireSectionTransformer transformer);

	/**
	 * Links the source {@link SectionOutput} to a target {@link SectionInput}.
	 * 
	 * @param sourceSection
	 *            Source section.
	 * @param sourceOutputName
	 *            Name of the source {@link SectionOutput}.
	 * @param targetSection
	 *            Target section.
	 * @param targetInputName
	 *            Name of the target {@link SectionInput}.
	 */
	void link(AutoWireSection sourceSection, String sourceOutputName,
			AutoWireSection targetSection, String targetInputName);

	/**
	 * <p>
	 * Determines if the {@link AutoWireSection} output is configured for
	 * linking.
	 * <p>
	 * This aids configuration by allowing to know if {@link SectionOutput}
	 * flows have been configured (linked).
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param sectionOutputName
	 *            {@link SectionOutput} name.
	 * @return <code>true</code> if configured for linking, otherwise
	 *         <code>false</code>.
	 */
	boolean isLinked(AutoWireSection section, String sectionOutputName);

	/**
	 * Links the {@link Escalation} to be handled by the
	 * {@link OfficeSectionInput}.
	 * 
	 * @param escalation
	 *            {@link Escalation}.
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link AutoWireSection} input.
	 */
	void linkEscalation(Class<? extends Throwable> escalation,
			AutoWireSection section, String inputName);

	/**
	 * Adds a flow to be triggered on start-up.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link AutoWireSection} input to trigger on
	 *            start-up.
	 */
	void addStartupFlow(AutoWireSection section, String inputName);

	/**
	 * Adds a raw object for dependency injection.
	 * 
	 * @param object
	 *            Object implementing the type to be dependency injected.
	 * @param autoWiring
	 *            {@link AutoWire} matches that the object is to provide
	 *            dependency injection. Should no {@link AutoWire} instances be
	 *            provided the type is defaulted from the object without a
	 *            qualifier.
	 */
	void addObject(Object object, AutoWire... autoWiring);

	/**
	 * Adds a {@link ManagedObjectSource} for dependency injection.
	 * 
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}. May be an
	 *            alias.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer} to assist in configuring the
	 *            {@link ManagedObjectSource}. May be <code>null</code> if no
	 *            assistance is required.
	 * @param autoWiring
	 *            {@link AutoWire} matches that the {@link ManagedObjectSource}
	 *            is to provide dependency injection. At least one
	 *            {@link AutoWire} must be provided.
	 * @return {@link AutoWireObject} for the {@link ManagedObjectSource}.
	 */
	AutoWireObject addManagedObject(String managedObjectSourceClassName,
			ManagedObjectSourceWirer wirer, AutoWire... autoWiring);

	/**
	 * Adds a {@link SupplierSource} to provide {@link ManagedObject} instances
	 * for dependency injection.
	 * 
	 * @param supplierSourceClassName
	 *            Class name of the {@link SupplierSource}. May be an alias.
	 * @return {@link AutoWireSupplier}.
	 */
	AutoWireSupplier addSupplier(String supplierSourceClassName);

	/**
	 * Indicates if the {@link AutoWireObject} is already configured for the
	 * {@link AutoWire}.
	 * 
	 * @param autoWiring
	 *            {@link AutoWire} to determine if available (configured).
	 * @return <code>true</code> if an {@link AutoWireObject} has been
	 *         configured for the {@link AutoWire}.
	 */
	boolean isObjectAvailable(AutoWire autoWiring);

	/**
	 * Adds {@link Governance} over the {@link ManagedObject} and object
	 * instances.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceClassName
	 *            Class name of the {@link GovernanceSource}. May be an alias.
	 * @return {@link AutoWireGovernance}.
	 */
	AutoWireGovernance addGovernance(String governanceName,
			String governanceSourceClassName);

	/**
	 * Assigns a {@link Team} responsible for:
	 * <ol>
	 * <li>{@link ManagedFunction} dependent on the specified object types</li>
	 * <li>{@link Governance} with the extension interface object type</li>
	 * </ol>
	 * 
	 * @param teamSourceClassName
	 *            Class name of the {@link TeamSource}. May be an alias.
	 * @param autoWiring
	 *            {@link AutoWire} instances to identify dependent {@link ManagedFunction}
	 *            object types and {@link Governance} extension interfaces the
	 *            {@link Team} is responsible for. Must have at least one
	 *            {@link AutoWire} provided.
	 * @return {@link AutoWireTeam}.
	 */
	AutoWireTeam assignTeam(String teamSourceClassName, AutoWire... autoWiring);

	/**
	 * Assigns a {@link Team} responsible for unassigned {@link ManagedFunction} instances.
	 * 
	 * @param teamSourceClassName
	 *            Class name of the {@link TeamSource}. May be an alias.
	 * @return {@link AutoWireTeam}.
	 */
	AutoWireTeam assignDefaultTeam(String teamSourceClassName);

	/**
	 * Specifies the {@link Profiler}.
	 * 
	 * @param profiler
	 *            {@link Profiler}.
	 */
	void setProfiler(Profiler profiler);

	/**
	 * Opens the {@link AutoWireOfficeFloor}.
	 * 
	 * @return {@link AutoWireOfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link AutoWireOfficeFloor}.
	 */
	AutoWireOfficeFloor openOfficeFloor() throws Exception;

}