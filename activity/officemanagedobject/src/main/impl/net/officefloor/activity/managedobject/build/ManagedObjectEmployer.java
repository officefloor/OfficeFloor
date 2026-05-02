package net.officefloor.activity.managedobject.build;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeLinkHandler;
import net.officefloor.activity.compose.build.ComposeSource;
import net.officefloor.activity.managedobject.ManagedObjectConfiguration;
import net.officefloor.activity.managedobject.ManagedObjectSourceConfiguration;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

import java.util.HashMap;
import java.util.Map;

public class ManagedObjectEmployer {

    /**
     * Employs the {@link ManagedObjectArchitect}.
     *
     * @param officeArchitect  {@link OfficeArchitect}.
     * @param composeArchitect {@link ComposeArchitect}.
     * @param context          {@link OfficeSourceContext}.
     * @return {@link ManagedObjectArchitect}.
     */
    public static ManagedObjectArchitect employManagedObjectArchitect(OfficeArchitect officeArchitect,
                                                                      ComposeArchitect composeArchitect,
                                                                      OfficeSourceContext context) {
        return new ManagedObjectArchitect() {

            @Override
            public OfficeManagedObject addManagedObject(String managedObjectName, String managedObjectLocation, PropertyList properties) throws Exception {
                return composeArchitect.addComposition(managedObjectName, new ManagedObjectComposeSource(), managedObjectLocation, properties, ManagedObjectConfiguration.class);
            }

            @Override
            public Map<String, OfficeManagedObject> addManagedObjects(String managedObjectDirectory, PropertyList properties) throws Exception {
                Map<String, OfficeManagedObject> managedObjects = new HashMap<>();
                composeArchitect.addCompositions((composeContext, listener) -> {
                    listener.composition(composeContext.getItemName(),
                            composeContext.addComposition(composeContext.getItemName(), new ManagedObjectComposeSource(), ManagedObjectConfiguration.class));
                }, managedObjectDirectory, properties, managedObjects::put);
                return managedObjects;
            }
        };
    }

    protected static class ManagedObjectComposeSource implements ComposeSource<OfficeManagedObject, ManagedObjectConfiguration> {

        @Override
        public OfficeManagedObject source(ComposeContext<ManagedObjectConfiguration> context) throws Exception {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeContext = context.getOfficeSourceContext();

            // Obtain the name of the managed object
            String managedObjectName = context.getItemName();

            // Configure the managed object
            OfficeManagedObjectSource managedObjectSource;
            ManagedObjectType<?> managedObjectType;
            ManagedObjectConfiguration moConfiguration = context.getConfiguration();
            ManagedObjectSourceConfiguration configuration = moConfiguration.getManagedObject();

            // Determine if class
            String className = configuration.getClassName();
            if (className != null) {

                // Load the class based managed object
                managedObjectSource = officeArchitect.addOfficeManagedObjectSource(managedObjectName, ClassManagedObjectSource.class.getName());
                managedObjectSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, className);

                // Load governance type
                PropertyList propertyList = officeContext.createPropertyList();
                propertyList.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME).setValue(className);
                managedObjectType = officeContext.loadManagedObjectType(managedObjectName, ClassManagedObjectSource.class.getName(), propertyList);

            } else {

                // Load the source based managed object
                String source = configuration.getSource();
                managedObjectSource = officeArchitect.addOfficeManagedObjectSource(managedObjectName, source);
                Map<String, String> properties = configuration.getProperties();
                PropertyList propertyList = officeContext.createPropertyList();
                if (properties != null) {
                    properties.forEach((name, value) -> {
                        managedObjectSource.addProperty(name, value);
                        propertyList.addProperty(name).setValue(value);
                    });
                }

                // Load the managed object type
                managedObjectType = officeContext.loadManagedObjectType(managedObjectName, source, propertyList);
            }

            // Load the composition handling
            context.linkFlows(configuration.getOutputs(), managedObjectType.getFlowTypes(), new ComposeLinkHandler<ManagedObjectFlowType<?>>() {
                @Override
                public String getFlowName(ManagedObjectFlowType<?> flowType) {
                    return flowType.getFlowName();
                }

                @Override
                public void link(ManagedObjectFlowType<?> flowType, OfficeSectionInput handler) {
                    OfficeManagedObjectFlow flow = managedObjectSource.getOfficeManagedObjectFlow(flowType.getFlowName());
                    officeArchitect.link(flow, handler);
                }

                @Override
                public void handleNonConfiguredFlow(ManagedObjectFlowType<?> flowType) {
                    officeArchitect.addIssue("Must configure handler for " + ManagedObject.class.getSimpleName() + " output " + flowType.getFlowName());
                }

                @Override
                public void handleExtraConfiguredFlow(String flowName, String handlerName) {
                    officeArchitect.addIssue(ManagedObject.class.getSimpleName() + " does not define flow " + flowName);
                }
            });

            // Determine the scope (with default)
            ManagedObjectScope scope = configuration.getScope();
            if (scope == null) {
                scope = ManagedObjectScope.THREAD;
            }

            // Add the managed object (with type qualification)
            OfficeManagedObject managedObject = managedObjectSource.addOfficeManagedObject(managedObjectName, scope);

            // Provide the type qualifications
            String objectType = managedObjectType.getObjectType().getName();
            managedObject.addTypeQualification(null, objectType);
            managedObject.addTypeQualification(managedObjectName, objectType);

            // Return the managed object
            return managedObject;
        }
    }

}
