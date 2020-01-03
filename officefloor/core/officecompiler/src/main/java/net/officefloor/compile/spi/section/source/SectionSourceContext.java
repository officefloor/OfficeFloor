package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for loading a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeSection}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the {@link SectionSource}.
	 * 
	 * @return Location of the {@link OfficeSection}.
	 */
	String getSectionLocation();

	/**
	 * Creates a {@link PropertyList} for loading types.
	 * 
	 * @return New {@link PropertyList} to aid in loading types.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link FunctionNamespaceType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedFunctionSource} to allow reflective configuration by the
	 * {@link SectionSource}.
	 * 
	 * @param functionNamespace              Name space of the
	 *                                       {@link ManagedFunctionSource}.
	 * @param managedFunctionSourceClassName Name of the implementing
	 *                                       {@link ManagedFunctionSource} class.
	 *                                       May also be an alias.
	 * @param properties                     {@link PropertyList} to configure the
	 *                                       implementing
	 *                                       {@link ManagedFunctionSource}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if fails to load
	 *         the {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType loadManagedFunctionType(String functionNamespace, String managedFunctionSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link SectionSource}.
	 * 
	 * @param managedObjectSourceName      Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName Name of the implementing
	 *                                     {@link ManagedObjectSource} class. May
	 *                                     also be an alias.
	 * @param properties                   {@link PropertyList} to configure the
	 *                                     {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load the
	 *         {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(String managedObjectSourceName, String managedObjectSourceClassName,
			PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link SectionType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link SubSection}
	 * to allow reflective configuration by the {@link SectionSource}.
	 * 
	 * @param sectionName            Name of the {@link SubSection}.
	 * @param sectionSourceClassName Name of the implementing {@link SectionSource}
	 *                               class. May also be an alias.
	 * @param location               Location of the {@link SubSection}.
	 * @param properties             {@link PropertyList} to configure the
	 *                               {@link SectionSource}.
	 * @return {@link SectionType} or <code>null</code> if fails to load the
	 *         {@link SectionType}.
	 */
	SectionType loadSectionType(String sectionName, String sectionSourceClassName, String location,
			PropertyList properties);

}