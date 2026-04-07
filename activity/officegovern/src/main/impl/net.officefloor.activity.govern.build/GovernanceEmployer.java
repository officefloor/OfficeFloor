package net.officefloor.activity.govern.build;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeLinkHandler;
import net.officefloor.activity.compose.build.ComposeSource;
import net.officefloor.activity.compose.section.ComposeSectionSource;
import net.officefloor.activity.govern.GovernConfiguration;
import net.officefloor.activity.govern.GovernanceConfiguration;
import net.officefloor.compile.governance.GovernanceEscalationType;
import net.officefloor.compile.governance.GovernanceFlowType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.plugin.governance.clazz.ClassGovernanceSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Employs the {@link GovernanceArchitect}.
 */
public class GovernanceEmployer {

    /**
     * Employs a {@link GovernanceArchitect}.
     *
     * @param officeArchitect {@link OfficeArchitect}.
     * @param composeArchitect {@link ComposeArchitect}.
     * @param officeContext {@link OfficeSourceContext}.
     * @return {@link GovernanceArchitect}.
     */
    public static GovernanceArchitect employGovernanceArchitect(OfficeArchitect officeArchitect,
                                                                ComposeArchitect composeArchitect, OfficeSourceContext officeContext) {
        return new GovernanceArchitect() {

            @Override
            public OfficeGovernance addGovernance(String governanceName, String governanceLocation, PropertyList properties) throws Exception {
                return composeArchitect.addComposition(governanceName, new GovernanceComposeSource(), governanceLocation, properties, GovernConfiguration.class);
            }

            @Override
            public Map<String, OfficeGovernance> addGovernances(String governanceDirectory, PropertyList properties) throws Exception {
                Map<String, OfficeGovernance> governances = new HashMap<>();
                composeArchitect.addCompositions(new GovernanceComposeSource(), governanceDirectory, properties, GovernConfiguration.class, governances::put);
                return governances;
            }
        };
    }

    protected static class GovernanceComposeSource implements ComposeSource<OfficeGovernance, GovernConfiguration> {

        @Override
        public OfficeGovernance source(ComposeContext<GovernConfiguration> context) throws Exception {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            OfficeSourceContext officeContext = context.getOfficeSourceContext();

            // Obtain name of governance
            String governanceName = context.getItemName();

            // Configure the governance
            OfficeGovernance governance;
            GovernanceType<?, ?> governanceType;
            GovernConfiguration governConfiguration = context.getConfiguration();
            GovernanceConfiguration configuration = governConfiguration.getGovernance();

            // Determine if class
            String className = configuration.getClassName();
            if (className != null) {

                // Load the class based governance
                governance = officeArchitect.addOfficeGovernance(governanceName, ClassGovernanceSource.class.getName());
                governance.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME, className);

                // Load governance type
                PropertyList propertyList = officeContext.createPropertyList();
                propertyList.addProperty(ClassGovernanceSource.CLASS_NAME_PROPERTY_NAME).setValue(className);
                governanceType = officeContext.loadGovernanceType(governanceName, ClassGovernanceSource.class.getName(), propertyList);

            } else {

                // Load the source based governance
                String source = configuration.getSource();
                governance = officeArchitect.addOfficeGovernance(governanceName, source);
                Map<String, String> properties = configuration.getConfig();
                PropertyList propertyList = officeContext.createPropertyList();
                if (properties != null) {
                    properties.forEach((name, value) -> {
                        governance.addProperty(name, value);
                        propertyList.addProperty(name).setValue(value);
                    });
                }

                // Load the governance type
                governanceType = officeContext.loadGovernanceType(governanceName, source, propertyList);
            }

            // Load the composition handling
            context.linkFlows(configuration.getOutputs(), governanceType.getFlowTypes(), new ComposeLinkHandler<GovernanceFlowType<?>>() {
                @Override
                public String getFlowName(GovernanceFlowType<?> flowType) {
                    return flowType.getFlowName();
                }

                @Override
                public void link(GovernanceFlowType<?> flowType, OfficeSectionInput handler) {
                    // TODO support mapping flows
                }

                @Override
                public void handleNonConfiguredFlow(GovernanceFlowType<?> flowType) {
                    officeArchitect.addIssue("Must configure handler for " + Governance.class.getSimpleName() + " output " + flowType.getFlowName());
                }

                @Override
                public void handleExtraConfiguredFlow(String flowName, String handlerName) {
                    officeArchitect.addIssue(Governance.class.getSimpleName() + " does not define flow " + flowName);
                }
            });
            context.linkEscalations(configuration.getEscalations(), governanceType.getEscalationTypes(), new ComposeLinkHandler<GovernanceEscalationType>() {
                @Override
                public String getFlowName(GovernanceEscalationType flowType) {
                    return flowType.getEscalationType().getName();
                }

                @Override
                public void link(GovernanceEscalationType flowType, OfficeSectionInput handler) {
                    // TODO support mapping escalations
                }

                @Override
                public void handleNonConfiguredFlow(GovernanceEscalationType escalationType) {
                    officeArchitect.addIssue("Must configure handler for " + Governance.class.getSimpleName() + " escalation " + escalationType.getEscalationType().getName());
                }

                @Override
                public void handleExtraConfiguredFlow(String flowName, String handlerName) {
                    officeArchitect.addIssue(Governance.class.getSimpleName() + " does not define escalation " + flowName);
                }
            });

            // Always auto wire extensions
            governance.enableAutoWireExtensions();

            // Return the governance
            return governance;
        }
    }

}
