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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.ObjectDependency;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link OfficeSource} implementation that auto-wires the configuration based
 * on type.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeSource extends AbstractOfficeSource {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link AutoWireSection} instances.
	 */
	private final List<AutoWireSection> sections = new LinkedList<AutoWireSection>();

	/**
	 * {@link Link} instances.
	 */
	private final List<Link> links = new LinkedList<Link>();

	/**
	 * {@link AutoWireResponsibility} instances.
	 */
	private final List<AutoWireResponsibility> responsibilities = new LinkedList<AutoWireResponsibility>();

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public AutoWireOfficeSource(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * Default constructor.
	 */
	public AutoWireOfficeSource() {
		this(OfficeFloorCompiler.newOfficeFloorCompiler());
	}

	/**
	 * Adds an {@link OfficeSection}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param sectionLocation
	 *            {@link OfficeSection} location.
	 * @return {@link AutoWireSection} to configure properties and link flows.
	 */
	public <S extends SectionSource> AutoWireSection addSection(
			String sectionName, Class<S> sectionSourceClass,
			String sectionLocation) {

		// Create the properties
		PropertyList properties = this.compiler.createPropertyList();

		// Create and add the section
		AutoWireSection section = new AutoWireSection(sectionName,
				sectionSourceClass, sectionLocation, properties);
		this.sections.add(section);

		// Return the section
		return section;
	}

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
	public void link(AutoWireSection sourceSection, String sourceOutputName,
			AutoWireSection targetSection, String targetInputName) {
		this.links.add(new Link(sourceSection, sourceOutputName, targetSection,
				targetInputName));
	}

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
	public boolean isLinked(AutoWireSection section, String sectionOutputName) {

		// Determine if linked
		for (Link link : this.links) {
			if ((link.sourceSection.getSectionName().equals(section
					.getSectionName()))
					&& (link.sourceOutputName.equals(sectionOutputName))) {
				// Matching section output so configured for linking
				return true;
			}
		}

		// As here, not linked
		return false;
	}

	/**
	 * Adds an {@link OfficeTeam} responsible for executing {@link Task}
	 * instances that has an object dependency of the input type.
	 * 
	 * @param dependencyType
	 *            Object dependency type for the {@link Task}.
	 * @return {@link AutoWireResponsibility} for the {@link OfficeTeam}.
	 */
	public AutoWireResponsibility addResponsibility(Class<?> dependencyType) {
		AutoWireResponsibility responsibility = new AutoWireResponsibility(
				dependencyType, "team-" + dependencyType.getName());
		this.responsibilities.add(responsibility);
		return responsibility;
	}

	/*
	 * ===================== OfficeSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect,
			OfficeSourceContext context) throws Exception {

		// Add the default team
		OfficeTeam defaultTeam = architect.addOfficeTeam("team");

		// Add the responsibility teams
		List<ResponsibleTeam> responsibleTeams = new ArrayList<ResponsibleTeam>(
				this.responsibilities.size());
		for (AutoWireResponsibility responsibility : this.responsibilities) {

			// Add the Office Team
			OfficeTeam officeTeam = architect.addOfficeTeam(responsibility
					.getOfficeTeamName());

			// Register the responsible team
			Class<?> dependencyType = responsibility.getDependencyType();
			ResponsibleTeam responsibleTeam = new ResponsibleTeam(
					dependencyType, officeTeam);

			// Add the responsible team
			responsibleTeams.add(responsibleTeam);
		}

		// Load the sections
		Map<String, Map<String, OfficeSectionInput>> inputs = new HashMap<String, Map<String, OfficeSectionInput>>();
		Map<String, Map<String, OfficeSectionOutput>> outputs = new HashMap<String, Map<String, OfficeSectionOutput>>();
		Map<String, OfficeObject> objects = new HashMap<String, OfficeObject>();
		for (AutoWireSection section : this.sections) {

			// Obtain the section name
			String sectionName = section.getSectionName();

			// Add the section
			OfficeSection officeSection = architect.addOfficeSection(
					sectionName, section.getSectionSourceClass().getName(),
					section.getSectionLocation(),
					section.getSectionProperties());

			// Link the objects
			for (OfficeSectionObject object : officeSection
					.getOfficeSectionObjects()) {
				String objectType = object.getObjectType();
				OfficeObject officeObject = objects.get(objectType);
				if (officeObject == null) {
					officeObject = architect.addOfficeObject(objectType,
							objectType);
					objects.put(objectType, officeObject);
				}
				architect.link(object, officeObject);
			}

			// Register the inputs
			Map<String, OfficeSectionInput> sectionInputs = new HashMap<String, OfficeSectionInput>();
			inputs.put(sectionName, sectionInputs);
			for (OfficeSectionInput input : officeSection
					.getOfficeSectionInputs()) {
				String inputName = input.getOfficeSectionInputName();
				sectionInputs.put(inputName, input);
			}

			// Register the outputs
			Map<String, OfficeSectionOutput> sectionOutputs = new HashMap<String, OfficeSectionOutput>();
			outputs.put(sectionName, sectionOutputs);
			for (OfficeSectionOutput output : officeSection
					.getOfficeSectionOutputs()) {
				String outputName = output.getOfficeSectionOutputName();
				sectionOutputs.put(outputName, output);
			}

			// Link section tasks to team.
			// Must be after objects to ensure linked.
			for (OfficeTask task : officeSection.getOfficeTasks()) {
				this.assignTeam(task, responsibleTeams, defaultTeam, architect);
			}

			// Link sub section tasks to team
			for (OfficeSubSection subSection : officeSection
					.getOfficeSubSections()) {
				this.linkTasksToTeams(subSection, responsibleTeams,
						defaultTeam, architect);
			}
		}

		// Link outputs to inputs
		for (Link link : this.links) {

			// Obtain the link details
			String sourceSectionName = link.sourceSection.getSectionName();
			String sourceOutputName = link.sourceOutputName;
			String outputName = sourceSectionName + ":" + sourceOutputName;
			String targetSectionName = link.targetSection.getSectionName();
			String targetInputName = link.targetInputName;
			String inputName = targetSectionName + ":" + targetInputName;

			// Obtain the output
			OfficeSectionOutput sectionOutput = null;
			Map<String, OfficeSectionOutput> sectionOutputs = outputs
					.get(sourceSectionName);
			if (sectionOutputs != null) {
				sectionOutput = sectionOutputs.get(sourceOutputName);
			}
			if (sectionOutput == null) {
				architect.addIssue("Unknown section output '" + outputName
						+ "' to link to section input '" + inputName + "'",
						AssetType.TASK, outputName);
				continue; // no output so can not link
			}

			// Obtain the input
			OfficeSectionInput sectionInput = null;
			Map<String, OfficeSectionInput> sectionInputs = inputs
					.get(targetSectionName);
			if (sectionInputs != null) {
				sectionInput = sectionInputs.get(targetInputName);
			}
			if (sectionInput == null) {
				architect.addIssue("Unknown section input '" + inputName
						+ "' for linking section output '" + outputName + "'",
						AssetType.TASK, inputName);
				continue; // no input so can not link
			}

			// Link the output to the input
			architect.link(sectionOutput, sectionInput);
		}
	}

	/**
	 * Links the {@link OfficeTask} instances of the {@link OfficeSubSection} to
	 * the {@link OfficeTeam}.
	 * 
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void linkTasksToTeams(OfficeSubSection subSection,
			List<ResponsibleTeam> responsibleTeams, OfficeTeam defaultTeam,
			OfficeArchitect architect) {

		// Link section tasks to team
		for (OfficeTask task : subSection.getOfficeTasks()) {
			this.assignTeam(task, responsibleTeams, defaultTeam, architect);
		}

		// Recursively link the sub sections
		for (OfficeSubSection subSubSection : subSection.getOfficeSubSections()) {
			this.linkTasksToTeams(subSubSection, responsibleTeams, defaultTeam,
					architect);
		}
	}

	/**
	 * Assigns the {@link OfficeTeam} for the {@link OfficeTask}.
	 * 
	 * @param task
	 *            {@link OfficeTask}.
	 * @param responsibleTeams
	 *            {@link ResponsibleTeam} instances.
	 * @param defaultTeam
	 *            Default {@link OfficeTeam}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void assignTeam(OfficeTask task,
			List<ResponsibleTeam> responsibleTeams, OfficeTeam defaultTeam,
			OfficeArchitect architect) {

		// Determine if team to be responsible
		for (ResponsibleTeam responsibleTeam : responsibleTeams) {
			if (responsibleTeam.isResponsible(task)) {
				// Team responsible for the task, so link
				architect.link(task.getTeamResponsible(),
						responsibleTeam.officeTeam);

				// Team assigned
				return;
			}
		}

		// As here, default team is responsible
		architect.link(task.getTeamResponsible(), defaultTeam);
	}

	/**
	 * Link of {@link SectionOutput} to {@link SectionInput}.
	 */
	private static class Link {

		/**
		 * Source {@link AutoWireSection}.
		 */
		public final AutoWireSection sourceSection;

		/**
		 * Source {@link SectionOutput} name.
		 */
		public final String sourceOutputName;

		/**
		 * Target {@link AutoWireSection}.
		 */
		public final AutoWireSection targetSection;

		/**
		 * Target {@link SectionInput} name.
		 */
		public final String targetInputName;

		/**
		 * Initiate.
		 * 
		 * @param sourceSection
		 *            Source {@link AutoWireSection}.
		 * @param sourceOutputName
		 *            Source {@link SectionOutput} name.
		 * @param targetSection
		 *            Target {@link AutoWireSection}.
		 * @param targetInputName
		 *            Target {@link SectionInput} name.
		 */
		public Link(AutoWireSection sourceSection, String sourceOutputName,
				AutoWireSection targetSection, String targetInputName) {
			this.sourceSection = sourceSection;
			this.sourceOutputName = sourceOutputName;
			this.targetSection = targetSection;
			this.targetInputName = targetInputName;
		}
	}

	/**
	 * Responsible {@link Team}.
	 */
	private static class ResponsibleTeam {

		/**
		 * Dependency type.
		 */
		private final Class<?> dependencyType;

		/**
		 * {@link OfficeTeam}.
		 */
		public final OfficeTeam officeTeam;

		/**
		 * Initiate.
		 * 
		 * @param dependencyType
		 *            Dependency type.
		 * @param officeTeam
		 *            {@link OfficeTeam}.
		 */
		public ResponsibleTeam(Class<?> dependencyType, OfficeTeam officeTeam) {
			this.dependencyType = dependencyType;
			this.officeTeam = officeTeam;
		}

		/**
		 * Determines if the {@link OfficeTeam} is responsible for the
		 * {@link OfficeTask}.
		 * 
		 * @param task
		 *            {@link OfficeTeam}.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link OfficeTask}.
		 */
		public boolean isResponsible(OfficeTask task) {
			return this.isResponsible(task.getObjectDependencies(),
					new HashSet<DependentManagedObject>());
		}

		/**
		 * Determines if {@link OfficeTeam} is responsible for the
		 * {@link ObjectDependency} instances.
		 * 
		 * @param dependencies
		 *            {@link ObjectDependency} instances.
		 * @param objects
		 *            Set of {@link DependentManagedObject} instances.
		 * @return <code>true</code> if the {@link OfficeTeam} is potentially
		 *         responsible for the {@link ObjectDependency} instances.
		 */
		private boolean isResponsible(ObjectDependency[] dependencies,
				Set<DependentManagedObject> objects) {

			// Determine if any objects are of dependency type
			for (ObjectDependency dependency : dependencies) {
				if (this.dependencyType.equals(dependency
						.getObjectDependencyType())) {
					// Matching dependency type, so is responsible
					return true;
				}
			}

			// Determine if responsible for dependent objects
			for (ObjectDependency dependency : dependencies) {

				// Obtain the dependent object
				DependentManagedObject object = dependency
						.getDependentManagedObject();
				if (object == null) {
					continue; // ignore if dependency not linked
				}

				// Determine if already processed dependent object
				if (objects.contains(object)) {
					continue; // ignore as already processed object
				}
				objects.add(object);

				// Recursively determine if responsible
				if (this.isResponsible(object.getObjectDependencies(), objects)) {
					return true; // responsible
				}
			}

			// As here, not responsible
			return false;
		}
	}

}