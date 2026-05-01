package net.officefloor.activity.admin.build;

import net.officefloor.activity.admin.AdminConfiguration;
import net.officefloor.activity.admin.AdministrationConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeLinkHandler;
import net.officefloor.activity.compose.build.ComposeSource;
import net.officefloor.compile.administration.AdministrationEscalationType;
import net.officefloor.compile.administration.AdministrationFlowType;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Employs the {@link AdministrationArchitect}.
 */
public class AdministrationEmployer {

    /**
     * Employs a {@link AdministrationArchitect}.
     *
     * @param officeArchitect  {@link OfficeArchitect}.
     * @param composeArchitect {@link ComposeArchitect}.
     * @param officeContext    {@link OfficeSourceContext}.
     * @return {@link AdministrationArchitect}.
     */
    public static AdministrationArchitect employAdministrationArchitect(OfficeArchitect officeArchitect,
                                                                        ComposeArchitect composeArchitect,
                                                                        OfficeSourceContext officeContext) {
        return new AdministrationArchitect() {

            private final Map<String, OfficeGovernance> governances = new HashMap<>();

            @Override
            public void addGovernance(String governanceName, OfficeGovernance goverance) {
                this.governances.put(governanceName, goverance);
            }

            @Override
            public OfficeAdministration addAdministration(String administrationName, String administrationLocation, PropertyList properties) throws Exception {
                return composeArchitect.addComposition(administrationName, new AdministrationComposeSource(), administrationLocation, properties, AdminConfiguration.class);
            }

            @Override
            public Map<String, OfficeAdministration> addAdministrations(String administrationDirectory, PropertyList properties) throws Exception {
                Map<String, OfficeAdministration> administrations = new HashMap<>();
                composeArchitect.addCompositions((composeContext, listener) -> {
                    listener.composition(composeContext.getItemName(),
                            composeContext.addComposition(composeContext.getItemName(), new AdministrationComposeSource(), AdminConfiguration.class));
                }, administrationDirectory, properties, administrations::put);
                return administrations;
            }
        };
    }

    protected static class AdministrationComposeSource implements ComposeSource<OfficeAdministration, AdminConfiguration> {

        @Override
        public OfficeAdministration source(ComposeContext<AdminConfiguration> context) throws Exception {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeContext = context.getOfficeSourceContext();

            // Obtain name of administration
            String administrationName = context.getItemName();

            // Configure the administration
            OfficeAdministration administration;
            AdministrationType<?, ?, ?> administrationType;
            AdminConfiguration adminConfiguration = context.getConfiguration();
            AdministrationConfiguration configuration = adminConfiguration.getAdministration();

            // Determine if class
            String className = configuration.getClassName();
            if (className != null) {

                // Load the class based administration
                administration = officeArchitect.addOfficeAdministration(administrationName, ClassAdministrationSource.class.getName());
                administration.addProperty(ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME, className);

                // Load governance type
                PropertyList propertyList = officeContext.createPropertyList();
                propertyList.addProperty(ClassAdministrationSource.CLASS_NAME_PROPERTY_NAME).setValue(className);
                administrationType = officeContext.loadAdministrationType(administrationName, ClassAdministrationSource.class.getName(), propertyList);

            } else {

                // Load the source based administration
                String source = configuration.getSource();
                administration = officeArchitect.addOfficeAdministration(administrationName, source);
                Map<String, String> properties = configuration.getProperties();
                PropertyList propertyList = officeContext.createPropertyList();
                if (properties != null) {
                    properties.forEach((name, value) -> {
                        administration.addProperty(name, value);
                        propertyList.addProperty(name).setValue(value);
                    });
                }

                // Load the administration type
                administrationType = officeContext.loadAdministrationType(administrationName, source, propertyList);
            }

            // Load the composition handling
            context.linkFlows(configuration.getOutputs(), administrationType.getFlowTypes(), new ComposeLinkHandler<AdministrationFlowType<?>>() {
                @Override
                public String getFlowName(AdministrationFlowType<?> flowType) {
                    return flowType.getFlowName();
                }

                @Override
                public void link(AdministrationFlowType<?> flowType, OfficeSectionInput handler) {
                    // TODO support mapping flows
                }

                @Override
                public void handleNonConfiguredFlow(AdministrationFlowType<?> flowType) {
                    officeArchitect.addIssue("Must configure handler for " + Administration.class.getSimpleName() + " output " + flowType.getFlowName());
                }

                @Override
                public void handleExtraConfiguredFlow(String flowName, String handlerName) {
                    officeArchitect.addIssue(Administration.class.getSimpleName() + " does not define flow " + flowName);
                }
            });
            context.linkEscalations(configuration.getEscalations(), administrationType.getEscalationTypes(), new ComposeLinkHandler<AdministrationEscalationType>() {
                @Override
                public String getFlowName(AdministrationEscalationType flowType) {
                    return flowType.getEscalationType().getName();
                }

                @Override
                public void link(AdministrationEscalationType flowType, OfficeSectionInput handler) {
                    // TODO support mapping escalations
                }

                @Override
                public void handleNonConfiguredFlow(AdministrationEscalationType escalationType) {
                    officeArchitect.addIssue("Must configure handler for " + Administration.class.getSimpleName() + " escalation " + escalationType.getEscalationType().getName());
                }

                @Override
                public void handleExtraConfiguredFlow(String flowName, String handlerName) {
                    officeArchitect.addIssue(Administration.class.getSimpleName() + " does not define escalation " + flowName);
                }
            });

            // Enable auto wire extensions
            administration.enableAutoWireExtensions();

            // Return the administration
            return administration;
        }
    }
}
