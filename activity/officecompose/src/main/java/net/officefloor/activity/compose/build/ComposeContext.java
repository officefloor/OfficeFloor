package net.officefloor.activity.compose.build;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Context for the {@link ComposeArchitect}.
 */
public interface ComposeContext<C> {

    /**
     * Obtains the suggested name of the item.
     *
     * @return Suggested name of the item.
     */
    String getItemName();

    /**
     * Obtains the configuration for the item.
     *
     * @return Configuration for the item.
     */
    C getConfiguration();

    /**
     * Obtains the particular configuration from the <code>composition</code> meta-data content by name.
     *
     * @param contentName Name of content.
     * @param type        Content type.
     * @param <T>         Content type.
     * @return Content.
     */
    <T> T getConfiguration(String contentName, Class<T> type);

    /**
     * Obtains {@link OfficeArchitect} to configure the item.
     *
     * @return {@link OfficeArchitect}.
     */
    OfficeArchitect getOfficeArchitect();

    /**
     * Obtains the {@link OfficeSourceContext} to assist configuring the item.
     *
     * @return {@link OfficeSourceContext}.
     */
    OfficeSourceContext getOfficeSourceContext();

    /**
     * <p>
     * Obtains the starting function to composition.
     * <p>
     * May be <code>null</code> if no functions for composition, as particular configuration
     * of item requires no functions.
     *
     * @return Starting function or <code>null</code>.
     */
    OfficeSectionInput getStartFunction();

    /**
     * <p>
     * Obtains the {@link OfficeSectionInput} to a named function.
     * <p>
     * This allows the item to link to invoking the function.
     *
     * @param functionName        Name of the function.
     * @param handleNotConfigured {@link Consumer} to handle the function not configured.
     * @return {@link OfficeSectionInput} to the function.
     */
    OfficeSectionInput getFunction(String functionName, Consumer<String> handleNotConfigured);


    /**
     * Convenience method to link the flows to handling functions via configuration.
     *
     * @param configuration Configuration mapping the output to the handling function.
     * @param flowTypes     Flow types of the item.
     * @param linkHandler   {@link ComposeLinkHandler}.
     * @param <F>           Flow type.
     */
    <F> void linkFlows(Map<String, String> configuration, F[] flowTypes, ComposeLinkHandler<F> linkHandler);

    /**
     * <p>
     * Convenience method to link the escalations to handling functions via configuration.
     * <p>
     * This incorporates the {@link net.officefloor.activity.compose.CompositionConfiguration} escalation mapping.
     *
     * @param configuration   Configuration mapping the escalation to the handling function.
     * @param escalationTypes Escalation types of the item.
     * @param linkHandler     {@link ComposeLinkHandler}.
     * @param <E>             Escalation type.
     */
    <E> void linkEscalations(Map<String, String> configuration, E[] escalationTypes, ComposeLinkHandler<E> linkHandler);

    /**
     * Obtains the {@link OfficeSection} containing the composition.
     *
     * @return {@link OfficeSection} containing the composition.
     */
    OfficeSection getCompositionSection();

}
