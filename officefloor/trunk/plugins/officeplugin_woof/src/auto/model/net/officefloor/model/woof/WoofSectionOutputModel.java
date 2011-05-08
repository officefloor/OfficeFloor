/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofSectionOutputModel extends AbstractModel implements ItemModel<WoofSectionOutputModel> {

    public static enum WoofSectionOutputEvent {
     CHANGE_WOOF_SECTION_OUTPUT_NAME, CHANGE_ARGUMENT_TYPE, CHANGE_WOOF_SECTION_INPUT, CHANGE_WOOF_TEMPLATE, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofSectionOutputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionOutputModel(
      String woofSectionOutputName
    , String argumentType
    ) {
        this.woofSectionOutputName = woofSectionOutputName;
        this.argumentType = argumentType;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionOutputModel(
      String woofSectionOutputName
    , String argumentType
    , WoofSectionOutputToWoofSectionInputModel woofSectionInput
    , WoofSectionOutputToWoofTemplateModel woofTemplate
    , WoofSectionOutputToWoofResourceModel woofResource
    ) {
        this.woofSectionOutputName = woofSectionOutputName;
        this.argumentType = argumentType;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionOutputModel(
      String woofSectionOutputName
    , String argumentType
    , WoofSectionOutputToWoofSectionInputModel woofSectionInput
    , WoofSectionOutputToWoofTemplateModel woofTemplate
    , WoofSectionOutputToWoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.woofSectionOutputName = woofSectionOutputName;
        this.argumentType = argumentType;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof section output name.
     */
    private String woofSectionOutputName;

    public String getWoofSectionOutputName() {
        return this.woofSectionOutputName;
    }

    public void setWoofSectionOutputName(String woofSectionOutputName) {
        String oldValue = this.woofSectionOutputName;
        this.woofSectionOutputName = woofSectionOutputName;
        this.changeField(oldValue, this.woofSectionOutputName, WoofSectionOutputEvent.CHANGE_WOOF_SECTION_OUTPUT_NAME);
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
        this.changeField(oldValue, this.argumentType, WoofSectionOutputEvent.CHANGE_ARGUMENT_TYPE);
    }

    /**
     * Woof section input.
     */
    private WoofSectionOutputToWoofSectionInputModel woofSectionInput;

    public WoofSectionOutputToWoofSectionInputModel getWoofSectionInput() {
        return this.woofSectionInput;
    }

    public void setWoofSectionInput(WoofSectionOutputToWoofSectionInputModel woofSectionInput) {
        WoofSectionOutputToWoofSectionInputModel oldValue = this.woofSectionInput;
        this.woofSectionInput = woofSectionInput;
        this.changeField(oldValue, this.woofSectionInput, WoofSectionOutputEvent.CHANGE_WOOF_SECTION_INPUT);
    }

    /**
     * Woof template.
     */
    private WoofSectionOutputToWoofTemplateModel woofTemplate;

    public WoofSectionOutputToWoofTemplateModel getWoofTemplate() {
        return this.woofTemplate;
    }

    public void setWoofTemplate(WoofSectionOutputToWoofTemplateModel woofTemplate) {
        WoofSectionOutputToWoofTemplateModel oldValue = this.woofTemplate;
        this.woofTemplate = woofTemplate;
        this.changeField(oldValue, this.woofTemplate, WoofSectionOutputEvent.CHANGE_WOOF_TEMPLATE);
    }

    /**
     * Woof resource.
     */
    private WoofSectionOutputToWoofResourceModel woofResource;

    public WoofSectionOutputToWoofResourceModel getWoofResource() {
        return this.woofResource;
    }

    public void setWoofResource(WoofSectionOutputToWoofResourceModel woofResource) {
        WoofSectionOutputToWoofResourceModel oldValue = this.woofResource;
        this.woofResource = woofResource;
        this.changeField(oldValue, this.woofResource, WoofSectionOutputEvent.CHANGE_WOOF_RESOURCE);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofSectionOutputModel> removeConnections() {
        RemoveConnectionsAction<WoofSectionOutputModel> _action = new RemoveConnectionsAction<WoofSectionOutputModel>(this);
        _action.disconnect(this.woofSectionInput);
        _action.disconnect(this.woofTemplate);
        _action.disconnect(this.woofResource);
        return _action;
    }
}
