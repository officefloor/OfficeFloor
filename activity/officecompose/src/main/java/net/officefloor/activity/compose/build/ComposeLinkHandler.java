package net.officefloor.activity.compose.build;

import net.officefloor.compile.spi.office.OfficeSectionInput;

/**
 * Handles linking composition.
 */
public interface ComposeLinkHandler<T> {

    /**
     * Obtains the flow name for the given flow type.
     *
     * @param flowType Flow type.
     * @return Flow name.
     */
    String getFlowName(T flowType);

    /**
     * Links the flow type to the {@link OfficeSectionInput}.
     *
     * @param flowType Flow type.
     * @param handler  {@link OfficeSectionInput} handler.
     */
    void link(T flowType, OfficeSectionInput handler);

    /**
     * Handles a non-configured flow.
     *
     * @param flowType Flow type.
     */
    default void handleNonConfiguredFlow(T flowType) {
        // Do nothing
    }

    /**
     * Handles the case where there is no handling function.
     *
     * @param flowType    Flow type.
     * @param handlerName Handler name.
     */
    default void handleNoHandlingFunction(T flowType, String handlerName) {
        // Do nothing
    }

    /**
     * Handles an extra configured flow.
     *
     * @param flowName    Flow name.
     * @param handlerName Handler name.
     */
    default void handleExtraConfiguredFlow(String flowName, String handlerName) {
        // Do nothing
    }
}
