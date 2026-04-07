package net.officefloor.activity.compose.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * Handles linking composition.
 */
public interface ComposeLinkHandler<T> {

    String getFlowName(T flowType);

    void link(T flowType, OfficeSectionInput handler);

    default void handleNonConfiguredFlow(T flowType) {
        // Do nothing
    }

    default void handleNoHandlingFunction(T flowType, String handlerName) {
        // Do nothing
    }

    default void handleExtraConfiguredFlow(String flowName, String handlerName) {
        // Do nothing
    }
}
