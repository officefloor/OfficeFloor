/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofTemplateOutputToWoofResourceModel extends AbstractModel implements ConnectionModel {

    public static enum WoofTemplateOutputToWoofResourceEvent {
     CHANGE_RESOURCE_NAME, CHANGE_WOOF_TEMPLATE_OUTPUT, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofTemplateOutputToWoofResourceModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofTemplateOutputToWoofResourceModel(
      String resourceName
    ) {
        this.resourceName = resourceName;
    }

    /**
     * Convenience constructor.
     */
    public WoofTemplateOutputToWoofResourceModel(
      String resourceName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofResourceModel woofResource
    ) {
        this.resourceName = resourceName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofTemplateOutputToWoofResourceModel(
      String resourceName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.resourceName = resourceName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofResource = woofResource;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Resource name.
     */
    private String resourceName;

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        String oldValue = this.resourceName;
        this.resourceName = resourceName;
        this.changeField(oldValue, this.resourceName, WoofTemplateOutputToWoofResourceEvent.CHANGE_RESOURCE_NAME);
    }

    /**
     * Woof template output.
     */
    private WoofTemplateOutputModel woofTemplateOutput;

    public WoofTemplateOutputModel getWoofTemplateOutput() {
        return this.woofTemplateOutput;
    }

    public void setWoofTemplateOutput(WoofTemplateOutputModel woofTemplateOutput) {
        WoofTemplateOutputModel oldValue = this.woofTemplateOutput;
        this.woofTemplateOutput = woofTemplateOutput;
        this.changeField(oldValue, this.woofTemplateOutput, WoofTemplateOutputToWoofResourceEvent.CHANGE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof resource.
     */
    private WoofResourceModel woofResource;

    public WoofResourceModel getWoofResource() {
        return this.woofResource;
    }

    public void setWoofResource(WoofResourceModel woofResource) {
        WoofResourceModel oldValue = this.woofResource;
        this.woofResource = woofResource;
        this.changeField(oldValue, this.woofResource, WoofTemplateOutputToWoofResourceEvent.CHANGE_WOOF_RESOURCE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofTemplateOutput.setWoofResource(this);
        this.woofResource.addWoofTemplateOutput(this);
    }

    public void remove() {
        this.woofTemplateOutput.setWoofResource(null);
        this.woofResource.removeWoofTemplateOutput(this);
    }

}
