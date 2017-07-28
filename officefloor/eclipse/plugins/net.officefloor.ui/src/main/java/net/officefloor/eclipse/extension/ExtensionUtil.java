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
package net.officefloor.eclipse.extension;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.editor.AbstractOfficeFloorEditor;
import net.officefloor.eclipse.extension.administrationsource.AdministrationSourceExtension;
import net.officefloor.eclipse.extension.governancesource.GovernanceSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedobjectsource.ManagedObjectSourceExtension;
import net.officefloor.eclipse.extension.officesource.OfficeSourceExtension;
import net.officefloor.eclipse.extension.open.ExtensionOpener;
import net.officefloor.eclipse.extension.open.ExtensionOpenerContext;
import net.officefloor.eclipse.extension.sectionsource.SectionSourceExtension;
import net.officefloor.eclipse.extension.teamsource.TeamSourceExtension;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.eclipse.util.LogUtil;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Utility class for working with extensions.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtensionUtil {

	/**
	 * Obtains the extension id for the input extension name.
	 * 
	 * @param name
	 *            Name of the extension.
	 * @return Id for the extension.
	 */
	public static String getExtensionId(String name) {
		return OfficeFloorPlugin.PLUGIN_ID + "." + name;
	}

	/**
	 * {@link SourceClassExtractor} for the
	 * {@link ManagedFunctionSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<ManagedFunctionSourceExtension> MANAGED_FUNCTION_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getManagedFunctionSourceClass();

	/**
	 * Creates the map of {@link ManagedFunctionSourceExtension} instances by
	 * their respective {@link ManagedFunctionSource} class name.
	 * 
	 * @return Map of {@link ManagedFunctionSourceExtension} instances by their
	 *         respective {@link ManagedFunctionSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ManagedFunctionSourceExtension> createManagedFunctionSourceExtensionMap() {
		return createSourceExtensionMap(ManagedFunctionSourceExtension.EXTENSION_ID,
				ManagedFunctionSourceExtension.class, MANAGED_FUNCTION_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link ManagedFunctionSourceExtension} instances.
	 * 
	 * @return Listing of {@link ManagedFunctionSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<ManagedFunctionSourceExtension> createWorkSourceExtensionList() {
		return createSourceExtensionList(createManagedFunctionSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for {@link ManagedObjectSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<ManagedObjectSourceExtension> MANAGED_OBJECT_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getManagedObjectSourceClass();

	/**
	 * Creates the map of {@link ManagedObjectSourceExtension} instances by
	 * their respective {@link ManagedObjectSource} class name.
	 * 
	 * @return Map of {@link ManagedObjectSourceExtension} instances by their
	 *         respective {@link ManagedObjectSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, ManagedObjectSourceExtension> createManagedObjectSourceExtensionMap() {
		return createSourceExtensionMap(ManagedObjectSourceExtension.EXTENSION_ID, ManagedObjectSourceExtension.class,
				MANAGED_OBJECT_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link ManagedObjectSourceExtension} instances.
	 * 
	 * @return Listing of {@link ManagedObjectSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<ManagedObjectSourceExtension> createManagedObjectSourceExtensionList() {
		return createSourceExtensionList(createManagedObjectSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for {@link GovernanceSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<GovernanceSourceExtension> GOVERNANCE_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getGovernanceSourceClass();

	/**
	 * Creates the map of {@link GovernanceSourceExtension} instances by their
	 * respective {@link GovernanceSource} class name.
	 * 
	 * @return Map of {@link GovernanceSourceExtension} instances by their
	 *         respective {@link GovernanceSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, GovernanceSourceExtension> createGovernanceSourceExtensionMap() {
		return createSourceExtensionMap(GovernanceSourceExtension.EXTENSION_ID, GovernanceSourceExtension.class,
				GOVERNANCE_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link GovernanceSourceExtension} instances.
	 * 
	 * @return Listing of {@link GovernanceSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<GovernanceSourceExtension> createGovernanceSourceExtensionList() {
		return createSourceExtensionList(createGovernanceSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for {@link AdministrationSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<AdministrationSourceExtension> ADMINISTRATION_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getAdministrationSourceClass();

	/**
	 * Creates the map of {@link AdministrationSourceExtension} instances by
	 * their respective {@link AdministrationSource} class name.
	 * 
	 * @return Map of {@link AdministrationSourceExtension} instances by their
	 *         respective {@link AdministrationSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, AdministrationSourceExtension> createAdministrationSourceExtensionMap() {
		return createSourceExtensionMap(AdministrationSourceExtension.EXTENSION_ID, AdministrationSourceExtension.class,
				ADMINISTRATION_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link AdministrationSourceExtension} instances.
	 * 
	 * @return Listing of {@link AdministrationSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<AdministrationSourceExtension> createAdministrationSourceExtensionList() {
		return createSourceExtensionList(createAdministrationSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for the {@link TeamSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<TeamSourceExtension> TEAM_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getTeamSourceClass();

	/**
	 * Creates the map of {@link TeamSourceExtension} instances by their
	 * respective {@link TeamSource} class name.
	 * 
	 * @return Map of {@link TeamSourceExtension} instances by their respective
	 *         {@link TeamSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, TeamSourceExtension> createTeamSourceExtensionMap() {
		return createSourceExtensionMap(TeamSourceExtension.EXTENSION_ID, TeamSourceExtension.class,
				TEAM_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link TeamSourceExtension} instances.
	 * 
	 * @return Listing of {@link TeamSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<TeamSourceExtension> createTeamSourceExtensionList() {
		return createSourceExtensionList(createTeamSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for the {@link SectionSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<SectionSourceExtension> SECTION_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getSectionSourceClass();

	/**
	 * Creates the map of {@link SectionSourceExtension} instances by their
	 * respective {@link SectionSource} class name.
	 * 
	 * @return Map of {@link SectionSourceExtension} instances by their
	 *         respective {@link SectionSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, SectionSourceExtension> createSectionSourceExtensionMap() {
		return createSourceExtensionMap(SectionSourceExtension.EXTENSION_ID, SectionSourceExtension.class,
				SECTION_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link SectionSourceExtension} instances.
	 * 
	 * @return Listing of {@link SectionSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<SectionSourceExtension> createSectionSourceExtensionList() {
		return createSourceExtensionList(createSectionSourceExtensionMap());
	}

	/**
	 * {@link SourceClassExtractor} for the {@link OfficeSourceExtension}.
	 */
	@SuppressWarnings("rawtypes")
	private static final SourceClassExtractor<OfficeSourceExtension> OFFICE_SOURCE_CLASS_EXTRACTOR = (
			sourceExtension) -> sourceExtension.getOfficeSourceClass();

	/**
	 * Creates the map of {@link OfficeSourceExtension} instances by their
	 * respective {@link OfficeSource} class name.
	 * 
	 * @return Map of {@link OfficeSourceExtension} instances by their
	 *         respective {@link OfficeSource} class name.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, OfficeSourceExtension> createOfficeSourceExtensionMap() {
		return createSourceExtensionMap(OfficeSourceExtension.EXTENSION_ID, OfficeSourceExtension.class,
				OFFICE_SOURCE_CLASS_EXTRACTOR);
	}

	/**
	 * Creates the listing of {@link OfficeSourceExtension} instances.
	 * 
	 * @return Listing of {@link OfficeSourceExtension} instances.
	 */
	@SuppressWarnings("rawtypes")
	public static List<OfficeSourceExtension> createOfficeSourceExtensionList() {
		return createSourceExtensionList(createOfficeSourceExtensionMap());
	}

	/**
	 * Opens the {@link ManagedFunctionSource}.
	 * 
	 * @param managedFunctionSourceClassName
	 *            {@link ManagedFunctionSource} class name.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedFunctionSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openManagedFunctionSource(String managedFunctionSourceClassName, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(ManagedFunctionSourceExtension.EXTENSION_ID, ManagedFunctionSourceExtension.class,
				MANAGED_FUNCTION_SOURCE_CLASS_EXTRACTOR, managedFunctionSourceClassName, null, properties, editor);
	}

	/**
	 * Opens the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClassName
	 *            {@link ManagedObjectSource} class name.
	 * @param properties
	 *            {@link PropertyList} for the {@link ManagedObjectSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openManagedObjectSource(String managedObjectSourceClassName, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(ManagedObjectSourceExtension.EXTENSION_ID, ManagedObjectSourceExtension.class,
				MANAGED_OBJECT_SOURCE_CLASS_EXTRACTOR, managedObjectSourceClassName, null, properties, editor);
	}

	/**
	 * Opens the {@link TeamSource}.
	 * 
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name.
	 * @param properties
	 *            {@link PropertyList} for the {@link TeamSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openTeamSource(String teamSourceClassName, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(TeamSourceExtension.EXTENSION_ID, TeamSourceExtension.class, TEAM_SOURCE_CLASS_EXTRACTOR,
				teamSourceClassName, null, properties, editor);
	}

	/**
	 * Opens the {@link GovernanceSource}.
	 * 
	 * @param governanceSourceClassName
	 *            {@link GovernanceSource} class name.
	 * @param properties
	 *            {@link PropertyList} for the {@link GovernanceSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openGovernanceSource(String governanceSourceClassName, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(GovernanceSourceExtension.EXTENSION_ID, GovernanceSourceExtension.class,
				GOVERNANCE_SOURCE_CLASS_EXTRACTOR, governanceSourceClassName, null, properties, editor);
	}

	/**
	 * Opens the {@link AdministrationSource}.
	 * 
	 * @param administrationSourceClassName
	 *            {@link AdministrationSource} class name.
	 * @param properties
	 *            {@link PropertyList} for the {@link AdministrationSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openAdministrationSource(String administrationSourceClassName, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(AdministrationSourceExtension.EXTENSION_ID, AdministrationSourceExtension.class,
				ADMINISTRATION_SOURCE_CLASS_EXTRACTOR, administrationSourceClassName, null, properties, editor);
	}

	/**
	 * Opens the {@link SectionSource}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionLocation
	 *            Location of the section.
	 * @param properties
	 *            {@link PropertyList} for the {@link SectionSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openSectionSource(String sectionSourceClassName, String sectionLocation, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(SectionSourceExtension.EXTENSION_ID, SectionSourceExtension.class, SECTION_SOURCE_CLASS_EXTRACTOR,
				sectionSourceClassName, sectionLocation, properties, editor);
	}

	/**
	 * Opens the {@link OfficeSource}.
	 * 
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param properties
	 *            {@link PropertyList} for the {@link OfficeSource}.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor}.
	 */
	public static void openOfficeSource(String officeSourceClassName, String officeLocation, PropertyList properties,
			AbstractOfficeFloorEditor<?, ?> editor) {
		openSource(OfficeSourceExtension.EXTENSION_ID, OfficeSourceExtension.class, OFFICE_SOURCE_CLASS_EXTRACTOR,
				officeSourceClassName, officeLocation, properties, editor);
	}

	/**
	 * Opens the source.
	 * 
	 * @param extensionId
	 *            Extension Id.
	 * @param extensionType
	 *            Type of extension.
	 * @param extractor
	 *            {@link SourceClassExtractor}.
	 * @param sourceClassName
	 *            Class name of the source.
	 * @param sourceLocation
	 *            Location of the source. May be <code>null</code> if not
	 *            applicable.
	 * @param properties
	 *            {@link PropertyList} containing the properties for the source.
	 * @param editor
	 *            {@link AbstractOfficeFloorEditor} requiring to open the
	 *            source.
	 */
	private static <E> void openSource(String extensionId, Class<E> extensionType, SourceClassExtractor<E> extractor,
			String sourceClassName, String sourceLocation, final PropertyList properties,
			final AbstractOfficeFloorEditor<?, ?> editor) {

		// Obtain the source extension map
		Map<String, E> extensionMap = createSourceExtensionMap(extensionId, extensionType, extractor);

		// Obtain the source extension
		E extension = extensionMap.get(sourceClassName);

		// Determine if have extension opener for source
		if (extension != null) {
			if (extension instanceof ExtensionOpener) {
				// Allow extension opener to open the source
				ExtensionOpener opener = (ExtensionOpener) extension;
				try {
					opener.openSource(new ExtensionOpenerContext() {
						@Override
						public PropertyList getPropertyList() {
							return properties;
						}

						@Override
						public void openClasspathResource(String resourcePath) {
							ClasspathUtil.openClasspathResource(resourcePath, editor);
						}
					});

					// Opened source
					return;

				} catch (Exception ex) {
					// Indicate failure opening source
					editor.messageError("Failed to open via source extension", ex);

					// Carry on to attempt to open source instead
				}
			}
		}

		// No extension, attempt to use location
		if (!EclipseUtil.isBlank(sourceLocation)) {
			// Open the source from its location
			ClasspathUtil.openClasspathResource(sourceLocation, editor);
			return; // opened source
		}

		// No extension or source location, so open the source class directly
		String resourcePath = sourceClassName.replace('.', '/') + ".class";
		ClasspathUtil.openClasspathResource(resourcePath, editor);
	}

	/**
	 * Extracts the Source {@link Class} from the extension.
	 */
	protected static interface SourceClassExtractor<E> {

		/**
		 * Obtains the source {@link Class} from the extension.
		 * 
		 * @param sourceExtension
		 *            Extension for the source {@link Class}.
		 * @return Source {@link Class} for the extension.
		 */
		Class<?> getSourceClass(E sourceExtension);
	}

	/**
	 * Transforms the map of source extensions into a list of source extension
	 * sorted by the source class name of the source extension.
	 * 
	 * @param <E>
	 *            Extension type.
	 * @param sourceExtensionMap
	 *            Map of source extensions.
	 * @return List of source extensions.
	 */
	public static <E> List<E> createSourceExtensionList(Map<String, E> sourceExtensionMap) {

		// Obtain the sorted source class names
		List<String> sourceClassNames = new ArrayList<String>(sourceExtensionMap.keySet());
		Collections.sort(sourceClassNames);

		// Create the listing of source extensions
		List<E> list = new ArrayList<E>(sourceClassNames.size());
		for (String sourceClassName : sourceClassNames) {
			E sourceExtension = sourceExtensionMap.get(sourceClassName);
			list.add(sourceExtension);
		}

		// Return the source extension list
		return list;
	}

	/**
	 * Creates the map of Source {@link Class} extensions by the Source
	 * {@link Class} name.
	 * 
	 * @param <E>
	 *            Extension type.
	 * @param extensionId
	 *            Extension Id.
	 * @param extensionType
	 *            Type of extension.
	 * @param extractor
	 *            {@link SourceClassExtractor}.
	 * @return {@link Map} of source class name to source extension.
	 */
	protected static <E> Map<String, E> createSourceExtensionMap(String extensionId, Class<E> extensionType,
			SourceClassExtractor<E> extractor) {

		// Create the map of source extensions
		Map<String, E> extensions = new HashMap<String, E>();

		// Obtain the extensions for sources
		List<E> sourceExtensions = createExecutableExtensions(extensionId, extensionType);

		// Add the source extensions (that are not test sources)
		for (E sourceExtension : sourceExtensions) {
			try {

				// Obtain the source class
				Class<?> sourceClass = extractor.getSourceClass(sourceExtension);
				if (sourceClass == null) {
					LogUtil.logError("Source extension " + sourceExtension.getClass().getName()
							+ " did not provide source class");
					continue; // carry on and not include
				}

				// Determine if ignore source
				if (isIgnoreSource(sourceClass)) {
					continue;
				}

				// Map in the extension
				extensions.put(sourceClass.getName(), sourceExtension);

			} catch (Throwable ex) {
				LogUtil.logError("Failed extension " + sourceExtension.getClass().getName(), ex);
				continue; // carry on and not include
			}
		}

		// Return the source extension map
		return extensions;
	}

	/**
	 * Indicates if the input {@link Class} is to be ignored (in other words
	 * annotated with {@link TestSource} or {@link PrivateSource}).
	 * 
	 * @param sourceClass
	 *            {@link Class} to determine if a {@link TestSource}.
	 * @return <code>true</code> if annotated with {@link TestSource}.
	 */
	public static boolean isIgnoreSource(Class<?> sourceClass) {

		// Do textual comparison (as may be different class loaders)
		String testSourceName = TestSource.class.getName();
		String privateSourceName = PrivateSource.class.getName();

		// Obtain the tags for the source class
		for (Annotation annotation : sourceClass.getAnnotations()) {
			Class<?> annotationClass = annotation.annotationType();

			// Ignore if a test source
			if (testSourceName.equals(annotationClass.getName())) {
				return true;
			}

			// Ignore if a private source
			if (privateSourceName.equals(annotationClass.getName())) {
				return true;
			}
		}

		// If at this point, not a test source
		return false;
	}

	/**
	 * Convenience method to determine if the source class is annotated with
	 * {@link TestSource} or {@link PrivateSource}.
	 * 
	 * @param sourceClassName
	 *            Fully qualified name of the source class.
	 * @param classLoader
	 *            {@link ClassLoader} to obtain the source class.
	 * @return <code>true</code> if annotated with {@link TestSource}.
	 */
	public static boolean isIgnoreSource(String sourceClassName, ClassLoader classLoader) {
		try {
			// Obtain the source class
			Class<?> sourceClass = classLoader.loadClass(sourceClassName);

			// Return whether a source to ignore
			return isIgnoreSource(sourceClass);

		} catch (Throwable ex) {
			LogUtil.logError(
					"Failed to load source class " + sourceClassName + " to determine if annotated to be ignored", ex);
			return false; // benefit of the doubt that not source class
		}
	}

	/**
	 * Creates the executable extensions for the particular extension.
	 * 
	 * @param <T>
	 *            Extension type.
	 * @param extensionId
	 *            Id of the extension.
	 * @param type
	 *            Type expected for the executable extension.
	 * @return Listing of executable extensions.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> createExecutableExtensions(String extensionId, Class<T> type) {

		final String CLASS_ATTRIBUTE = "class";

		// Obtain the extensions for extension point
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionId);

		// Obtain the listing of executable extension
		List<T> typedExecutableExtensions = new LinkedList<T>();
		for (IConfigurationElement element : configurationElements) {
			try {
				// Create the executable extension
				Object executableExtension = element.createExecutableExtension(CLASS_ATTRIBUTE);

				// Add only if appropriate type
				if (type.isAssignableFrom(executableExtension.getClass())) {
					// Appropriate type, therefore include
					typedExecutableExtensions.add((T) executableExtension);
				} else {
					// Indicate error in configuring the extension
					LogUtil.logError("Executable extension did not adhere to type " + type.getName()
							+ " [executable extension=" + executableExtension.getClass().getName() + "]");
				}

			} catch (Throwable ex) {
				String extensionClassName = element.getAttribute(CLASS_ATTRIBUTE);
				LogUtil.logError("Failed loading executable extension " + extensionClassName + " for " + extensionId,
						ex);
				continue; // carry on for next extension
			}
		}

		// Return the listing of typed executable extensions
		return typedExecutableExtensions;
	}

	/**
	 * All access via static methods.
	 */
	protected ExtensionUtil() {
	}

}