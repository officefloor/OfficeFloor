/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofExceptionModel extends AbstractModel implements ItemModel<WoofExceptionModel> {

    public static enum WoofExceptionEvent {
     CHANGE_CLASS_NAME, CHANGE_WOOF_SECTION_INPUT, CHANGE_WOOF_TEMPLATE, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofExceptionModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofExceptionModel(
      String className
    ) {
        this.className = className;
    }

    /**
     * Convenience constructor.
     */
    public WoofExceptionModel(
      String className
    , WoofExceptionToWoofSectionInputModel woofSectionInput
    , WoofExceptionToWoofTemplateModel woofTemplate
    , WoofExceptionToWoofResourceModel woofResource
    ) {
        this.className = className;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofExceptionModel(
      String className
    , WoofExceptionToWoofSectionInputModel woofSectionInput
    , WoofExceptionToWoofTemplateModel woofTemplate
    , WoofExceptionToWoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.className = className;
        this.woofSectionInput = woofSectionInput;
        this.woofTemplate = woofTemplate;
        this.woofResource = woofResource;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Class name.
     */
    private String className;

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        String oldValue = this.className;
        this.className = className;
        this.changeField(oldValue, this.className, WoofExceptionEvent.CHANGE_CLASS_NAME);
    }

    /**
     * Woof section input.
     */
    private WoofExceptionToWoofSectionInputModel woofSectionInput;

    public WoofExceptionToWoofSectionInputModel getWoofSectionInput() {
        return this.woofSectionInput;
    }

    public void setWoofSectionInput(WoofExceptionToWoofSectionInputModel woofSectionInput) {
        WoofExceptionToWoofSectionInputModel oldValue = this.woofSectionInput;
        this.woofSectionInput = woofSectionInput;
        this.changeField(oldValue, this.woofSectionInput, WoofExceptionEvent.CHANGE_WOOF_SECTION_INPUT);
    }

    /**
     * Woof template.
     */
    private WoofExceptionToWoofTemplateModel woofTemplate;

    public WoofExceptionToWoofTemplateModel getWoofTemplate() {
        return this.woofTemplate;
    }

    public void setWoofTemplate(WoofExceptionToWoofTemplateModel woofTemplate) {
        WoofExceptionToWoofTemplateModel oldValue = this.woofTemplate;
        this.woofTemplate = woofTemplate;
        this.changeField(oldValue, this.woofTemplate, WoofExceptionEvent.CHANGE_WOOF_TEMPLATE);
    }

    /**
     * Woof resource.
     */
    private WoofExceptionToWoofResourceModel woofResource;

    public WoofExceptionToWoofResourceModel getWoofResource() {
        return this.woofResource;
    }

    public void setWoofResource(WoofExceptionToWoofResourceModel woofResource) {
        WoofExceptionToWoofResourceModel oldValue = this.woofResource;
        this.woofResource = woofResource;
        this.changeField(oldValue, this.woofResource, WoofExceptionEvent.CHANGE_WOOF_RESOURCE);
    }



    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofExceptionModel> removeConnections() {
        RemoveConnectionsAction<WoofExceptionModel> _action = new RemoveConnectionsAction<WoofExceptionModel>(this);
        _action.disconnect(this.woofSectionInput);
        _action.disconnect(this.woofTemplate);
        _action.disconnect(this.woofResource);
        return _action;
    }
}
