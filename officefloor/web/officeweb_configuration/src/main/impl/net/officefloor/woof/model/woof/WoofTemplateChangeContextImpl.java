package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link WoofTemplateChangeContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateChangeContextImpl extends SourceContextImpl implements WoofTemplateChangeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType        Indicates if loading type.
	 * @param sourceContext        {@link SourceContext}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
	 */
	public WoofTemplateChangeContextImpl(boolean isLoadingType, SourceContext sourceContext,
			ConfigurationContext configurationContext, WoofChangeIssues issues) {
		super(sourceContext.getLogger().getName(), isLoadingType, sourceContext, new SourcePropertiesImpl());
		this.configurationContext = configurationContext;
		this.issues = issues;
	}

	/*
	 * =============== WoofTemplateChangeContext =======================
	 */

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public WoofChangeIssues getWoofChangeIssues() {
		return this.issues;
	}

}