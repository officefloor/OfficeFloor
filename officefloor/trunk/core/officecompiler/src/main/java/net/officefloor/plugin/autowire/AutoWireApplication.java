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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
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
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	<S extends SectionSource> AutoWireSection addSection(String sectionName,
			Class<S> sectionSourceClass, String sectionLocation);

	/**
	 * Adds an {@link AutoWireSection} with the ability to override the
	 * {@link AutoWireSection} used.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @param sectionFactory
	 *            {@link AutoWireSectionFactory} to allow overriding the
	 *            {@link AutoWireSection} utilised.
	 * @return Overridden {@link AutoWireSection} to configure properties and
	 *         link flows.
	 */
	<S extends SectionSource, A extends AutoWireSection> AutoWireSection addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation, AutoWireSectionFactory<A> sectionFactory);

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
	 * Adds a raw object for dependency injection.
	 * 
	 * @param object
	 *            Object implementing the type to be dependency injected.
	 * @param objectTypes
	 *            Types that the object is to provide dependency injection via
	 *            auto-wiring. Should no types be provided the type is defaulted
	 *            from the object.
	 */
	void addObject(Object object, Class<?>... objectTypes);

	/**
	 * Adds a {@link ManagedObjectSource} for dependency injection.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param wirer
	 *            {@link ManagedObjectSourceWirer} to assist in configuring the
	 *            {@link ManagedObjectSource}. May be <code>null</code> if no
	 *            assistance is required.
	 * @param objectTypes
	 *            Types that the {@link ManagedObjectSource} is to provide
	 *            dependency injection via auto-wiring.
	 * @return {@link AutoWireObject} for the {@link ManagedObjectSource}.
	 */
	<D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> AutoWireObject addManagedObject(
			Class<S> managedObjectSourceClass, ManagedObjectSourceWirer wirer,
			Class<?>... objectTypes);

	/**
	 * Indicates if the {@link AutoWireObject} is already configured for the
	 * type.
	 * 
	 * @param objectType
	 *            Type of object to determine if available (configured).
	 * @return <code>true</code> if an {@link AutoWireObject} has been
	 *         configured for the type.
	 */
	boolean isObjectAvailable(Class<?> objectType);

	/**
	 * Adds {@link Governance} over the {@link ManagedObject} and object
	 * instances.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSource
	 *            {@link GovernanceSource} class.
	 * @return {@link AutoWireGovernance}.
	 */
	<I, F extends Enum<F>, S extends GovernanceSource<I, F>> AutoWireGovernance addGovernance(
			String governanceName, Class<S> governanceSource);

	/**
	 * Assigns a {@link Team} responsible for:
	 * <ol>
	 * <li>{@link Task} dependent on the specified object types</li>
	 * <li>{@link Governance} with the extension interfance object type</li>
	 * </ol>
	 * 
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @param objectTypes
	 *            Dependent {@link Task} object types and {@link Governance}
	 *            extension interfaces the {@link Team} is responsible for. Must
	 *            have at least one object type provided.
	 * @return {@link AutoWireTeam}.
	 */
	<T extends TeamSource> AutoWireTeam assignTeam(Class<T> teamSourceClass,
			Class<?>... objectTypes);

	/**
	 * Assigns a {@link Team} responsible for unassigned {@link Task} instances.
	 * 
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @return {@link AutoWireTeam}.
	 */
	<T extends TeamSource> AutoWireTeam assignDefaultTeam(
			Class<T> teamSourceClass);

	/**
	 * Opens the {@link AutoWireOfficeFloor}.
	 * 
	 * @return {@link AutoWireOfficeFloor}.
	 * @throws Exception
	 *             If fails to open the {@link AutoWireOfficeFloor}.
	 */
	AutoWireOfficeFloor openOfficeFloor() throws Exception;

}