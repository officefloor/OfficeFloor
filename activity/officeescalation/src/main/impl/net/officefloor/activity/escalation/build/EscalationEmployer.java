/*-
 * #%L
 * Escalation
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.activity.escalation.build;

import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

/**
 * Employs the {@link EscalationArchitect}.
 */
public class EscalationEmployer {

    /**
     * Employs an {@link EscalationArchitect}.
     *
     * @param officeArchitect  {@link OfficeArchitect}.
     * @param composeArchitect {@link ComposeArchitect}.
     * @param officeContext    {@link OfficeSourceContext}.
     * @return {@link EscalationArchitect}.
     */
    public static EscalationArchitect employEscalationArchitect(OfficeArchitect officeArchitect,
                                                                ComposeArchitect composeArchitect,
                                                                OfficeSourceContext officeContext) {
        return new EscalationArchitect() {

            @Override
            public OfficeEscalation addEscalation(String escalationTypeName, String resourceLocation,
                                                  PropertyList properties) throws Exception {
                return composeArchitect.addComposition(escalationTypeName, new EscalationComposeSource(),
                        resourceLocation, properties, ComposeConfiguration.class);
            }

            @Override
            public void addEscalations(String escalationDirectory, PropertyList properties) throws Exception {
                composeArchitect.addCompositions(OfficeEscalation.class.getSimpleName(),
                        (composeContext, listener) -> {
                            String exceptionTypeName = composeContext.getItemName();
                            OfficeEscalation escalation = composeContext.addComposition(
                                    exceptionTypeName, new EscalationComposeSource(), ComposeConfiguration.class);
                            listener.composition(exceptionTypeName, escalation);
                        }, escalationDirectory, properties, (name, escalation) -> {});
            }
        };
    }

    protected static class EscalationComposeSource implements ComposeSource<OfficeEscalation, ComposeConfiguration> {

        @Override
        public OfficeEscalation source(ComposeContext<ComposeConfiguration> context) throws Exception {
            OfficeArchitect officeArchitect = context.getOfficeArchitect();
            String escalationTypeName = context.getItemName();

            // Register the office-level escalation
            OfficeEscalation escalation = officeArchitect.addOfficeEscalation(escalationTypeName);

            // Link the escalation to the start of the composition
            OfficeSectionInput startFunction = context.getStartFunction();
            if (startFunction == null) {
                officeArchitect.addIssue("No handler function defined in escalation composition for " + escalationTypeName);
            } else {
                officeArchitect.link(escalation, startFunction);
            }

            return escalation;
        }
    }

}
