/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofTemplateOutputModel extends AbstractModel implements ItemModel<WoofTemplateOutputModel> {

    public static enum WoofTemplateOutputEvent {
     CHANGE_WOOF_TEMPLATE_OUTPUT_NAME, CHANGE_ARGUMENT_TYPE, CHANGE_WOOF_SECTION_INPUT, CHANGE_WOOF_TEMPLATE, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofTemplateOutputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofTemplateOutputModel(
      String woofTemplateOutputName
    , String argumentType
    ) {
        this.woofTemplateOutputName = woofTemplateOutputName;
        this.argumentType = argumentType;
    }

    /**
     * Convenience constructor.
     */
    public WoofTemplateOutputModel(
      String woofTemplateOutputName
    , String argumentType
    , WoofTemplateOutputToWoofSectionInputModel woofSectionInput
    , WoofTemplateOutputToWoofTemplateModel woofTemplate
    , WoofTemplateOutputToWoofResourceModel woofResource
    ) {
        this.woofTemplateOutputName = woofTemplateOutputName;
        this.argumentType = argumentType;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofTemplateOutputModel(
      String woofTemplateOutputName
    , String argumentType
    , WoofTemplateOutputToWoofSectionInputModel woofSectionInput
    , WoofTemplateOutputToWoofTemplateModel woofTemplate
    , WoofTemplateOutputToWoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.woofTemplateOutputName = woofTemplateOutputName;
        this.argumentType = argumentType;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof template output name.
     */
    private String woofTemplateOutputName;

    public String getWoofTemplateOutputName() {
        return this.woofTemplateOutputName;
    }

    public void setWoofTemplateOutputName(String woofTemplateOutputName) {
        String oldValue = this.woofTemplateOutputName;
        this.woofTemplateOutputName = woofTemplateOutputName;
        this.changeField(oldValue, this.woofTemplateOutputName, WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE_OUTPUT_NAME);
    }

    /**
     * Argument type.
     */
    private String argumentType;

    public String getArgumentType() {
        return this.argumentType;
    }

    public void setArgumentType(String argumentType) {
        String oldValue = this.argumentType;
        this.argumentType = argumentType;
        this.changeField(oldValue, this.argumentType, WoofTemplateOutputEvent.CHANGE_ARGUMENT_TYPE);
    }

    /**
     * Woof section input.
     */
    private WoofTemplateOutputToWoofSectionInputModel woofSectionInput;

    public WoofTemplateOutputToWoofSectionInputModel getWoofSectionInput() {
        return this.woofSectionInput;
    }

    public void setWoofSectionInput(WoofTemplateOutputToWoofSectionInputModel woofSectionInput) {
        WoofTemplateOutputToWoofSectionInputModel oldValue = this.woofSectionInput;
        this.woofSectionInput = woofSectionInput;
        this.changeField(oldValue, this.woofSectionInput, WoofTemplateOutputEvent.CHANGE_WOOF_SECTION_INPUT);
    }

    /**
     * Woof template.
     */
    private WoofTemplateOutputToWoofTemplateModel woofTemplate;

    public WoofTemplateOutputToWoofTemplateModel getWoofTemplate() {
        return this.woofTemplate;
    }

    public void setWoofTemplate(WoofTemplateOutputToWoofTemplateModel woofTemplate) {
        WoofTemplateOutputToWoofTemplateModel oldValue = this.woofTemplate;
        this.woofTemplate = woofTemplate;
        this.changeField(oldValue, this.woofTemplate, WoofTemplateOutputEvent.CHANGE_WOOF_TEMPLATE);
    }

    /**
     * Woof resource.
     */
    private WoofTemplateOutputToWoofResourceModel woofResource;

    public WoofTemplateOutputToWoofResourceModel getWoofResource() {
        return this.woofResource;
    }

    public void setWoofResource(WoofTemplateOutputToWoofResourceModel woofResource) {
        WoofTemplateOutputToWoofResourceModel oldValue = this.woofResource;
        this.woofResource = woofResource;
        this.changeField(oldValue, this.woofResource, WoofTemplateOutputEvent.CHANGE_WOOF_RESOURCE);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofTemplateOutputModel> removeConnections() {
        RemoveConnectionsAction<WoofTemplateOutputModel> _action = new RemoveConnectionsAction<WoofTemplateOutputModel>(this);
        _action.disconnect(this.woofSectionInput);
        _action.disconnect(this.woofTemplate);
        _action.disconnect(this.woofResource);
        return _action;
    }
}
