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
package net.officefloor.compile.test.officefloor;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Provides easier compiling of {@link OfficeFloor} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloor extends AbstractOfficeFloorSource {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * {@link CompileOfficeFloorExtension} instances.
	 */
	private final List<CompileOfficeFloorExtension> officeFloorExtensions = new LinkedList<>();

	/**
	 * {@link CompileOfficeExtension} instances.
	 */
	private final List<CompileOfficeExtension> officeExtensions = new LinkedList<>();

	/**
	 * {@link CompileSectionExtension} instances.
	 */
	private final List<CompileSectionExtension> sectionExtensions = new LinkedList<>();

	/**
	 * Instantiate.
	 */
	public CompileOfficeFloor() {
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.compiler.setCompilerIssues(new FailTestCompilerIssues());
	}

	/**
	 * <p>
	 * Obtains the {@link OfficeFloorCompiler}.
	 * <p>
	 * Note the {@link OfficeFloorSource} is overridden on compiling to ensure the
	 * extensions are available.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Adds a {@link CompileOfficeFloorExtension}.
	 * 
	 * @param extension
	 *            {@link CompileOfficeFloorExtension}.
	 */
	public void officeFloor(CompileOfficeFloorExtension extension) {
		this.officeFloorExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileOfficeExtension}.
	 * 
	 * @param extension
	 *            {@link CompileOfficeExtension}.
	 */
	public void office(CompileOfficeExtension extension) {
		this.officeExtensions.add(extension);
	}

	/**
	 * Adds a {@link CompileSectionExtension}.
	 * 
	 * @param extension
	 *            {@link CompileSectionExtension}.
	 */
	public void section(CompileSectionExtension extension) {
		this.sectionExtensions.add(extension);
	}

	/**
	 * Compiles the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOfficeFloor() throws Exception {

		// Ensure use this to source OfficeFloor with extensions
		this.compiler.setOfficeFloorSource(this);

		// Compile and return the OfficeFloor
		return this.compiler.compile("OfficeFloor");
	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOfficeFloor() throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOfficeFloor();

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/*
	 * ======================= OfficeFloorSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void specifyConfigurationProperties(RequiredProperties requiredProperties, OfficeFloorSourceContext context)
			throws Exception {
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer, OfficeFloorSourceContext context) throws Exception {

		// Add the default office (if office configuration)
		DeployedOffice office = null;
		if (this.officeExtensions.size() > 0) {
			office = deployer.addDeployedOffice("OFFICE", new CompileOfficeSource(), null);
		}
		final DeployedOffice deployedOffice = office;

		// Run the extensions
		for (CompileOfficeFloorExtension extension : this.officeFloorExtensions) {
			extension.extend(new CompileOfficeFloorContext() {

				@Override
				public OfficeFloorSourceContext getOfficeFloorSourceContext() {
					return context;
				}

				@Override
				public OfficeFloorDeployer getOfficeFloorDeployer() {
					return deployer;
				}

				@Override
				public DeployedOffice getDeployedOffice() {
					return deployedOffice;
				}

				@Override
				public OfficeFloorManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
						ManagedObjectScope scope) {
					OfficeFloorManagedObjectSource mos = deployer.addManagedObjectSource(managedObjectName + "_SOURCE",
							ClassManagedObjectSource.class.getName());
					mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, managedObjectClass.getName());
					return mos.addOfficeFloorManagedObject(managedObjectName, scope);
				}
			});
		}
	}

	/**
	 * {@link OfficeSource} that allows {@link CompileOfficeExtension} instances to
	 * configure it.
	 */
	@PrivateSource
	private class CompileOfficeSource extends AbstractOfficeSource {

		/**
		 * {@link OfficeSection}.
		 */
		private OfficeSection section = null;

		/**
		 * Obtains the {@link OfficeSection}.
		 * 
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @return {@link OfficeSection}.
		 */
		private OfficeSection getSection(OfficeArchitect architect) {
			if (this.section == null) {
				if (CompileOfficeFloor.this.sectionExtensions.size() > 0) {
					this.section = architect.addOfficeSection("SECTION", new CompileSectionSource(), null);
				}
			}
			return this.section;
		}

		/*
		 * ====================== OfficeSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Auto-wire
			officeArchitect.enableAutoWireObjects();

			// Load the extensions
			for (CompileOfficeExtension extension : CompileOfficeFloor.this.officeExtensions) {
				extension.extend(new CompileOfficeContext() {

					@Override
					public OfficeSection getOfficeSection() {
						return CompileOfficeSource.this.getSection(officeArchitect);
					}

					@Override
					public OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass,
							String sectionLocation) {
						if (CompileOfficeSource.this.section != null) {
							throw new IllegalStateException("OfficeSection already specified");
						}
						CompileOfficeSource.this.section = officeArchitect.addOfficeSection("SECTION",
								sectionSourceClass.getName(), sectionLocation);
						return CompileOfficeSource.this.section;
					}

					@Override
					public OfficeSourceContext getOfficeSourceContext() {
						return context;
					}

					@Override
					public OfficeArchitect getOfficeArchitect() {
						return officeArchitect;
					}

					@Override
					public OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
							ManagedObjectScope scope) {
						OfficeManagedObjectSource mos = officeArchitect.addOfficeManagedObjectSource(
								managedObjectName + "_SOURCE", ClassManagedObjectSource.class.getName());
						mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
								managedObjectClass.getName());
						return mos.addOfficeManagedObject(managedObjectName, scope);
					}

					@Override
					public OfficeSection addSection(String sectionName, Class<?> sectionClass) {
						return officeArchitect.addOfficeSection(sectionName, ClassSectionSource.class.getName(),
								sectionClass.getName());
					}
				});
			}

			// Ensure have section
			this.getSection(officeArchitect);
		}
	}

	/**
	 * {@link SectionSource} that allows {@link CompileSectionExtension} instances
	 * to configure it.
	 */
	@PrivateSource
	private class CompileSectionSource extends AbstractSectionSource {

		/*
		 * ====================== SectionSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

			// Load the extensions
			for (CompileSectionExtension extension : CompileOfficeFloor.this.sectionExtensions) {
				extension.extend(new CompileSectionContext() {

					@Override
					public SectionSourceContext getSectionSourceContext() {
						return context;
					}

					@Override
					public SectionDesigner getSectionDesigner() {
						return designer;
					}
				});
			}
		}
	}

}