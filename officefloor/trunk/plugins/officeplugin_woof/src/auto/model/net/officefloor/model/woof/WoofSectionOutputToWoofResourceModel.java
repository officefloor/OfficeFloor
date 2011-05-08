/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofSectionOutputToWoofResourceModel extends AbstractModel implements ConnectionModel {

    public static enum WoofSectionOutputToWoofResourceEvent {
     CHANGE_RESOURCE_NAME, CHANGE_WOOF_SECTION_OUTPUT, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofSectionOutputToWoofResourceModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionOutputToWoofResourceModel(
      String resourceName
    ) {
        this.resourceName = resourceName;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionOutputToWoofResourceModel(
      String resourceName
    , WoofSectionOutputModel woofSectionOutput
    , WoofResourceModel woofResource
    ) {
        this.resourceName = resourceName;
        this.woofSectionOutput = woofSectionOutput;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionOutputToWoofResourceModel(
      String resourceName
    , WoofSectionOutputModel woofSectionOutput
    , WoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.resourceName = resourceName;
        this.woofSectionOutput = woofSectionOutput;
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
        this.changeField(oldValue, this.resourceName, WoofSectionOutputToWoofResourceEvent.CHANGE_RESOURCE_NAME);
    }

    /**
     * Woof section output.
     */
    private WoofSectionOutputModel woofSectionOutput;

    public WoofSectionOutputModel getWoofSectionOutput() {
        return this.woofSectionOutput;
    }

    public void setWoofSectionOutput(WoofSectionOutputModel woofSectionOutput) {
        WoofSectionOutputModel oldValue = this.woofSectionOutput;
        this.woofSectionOutput = woofSectionOutput;
        this.changeField(oldValue, this.woofSectionOutput, WoofSectionOutputToWoofResourceEvent.CHANGE_WOOF_SECTION_OUTPUT);
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
        this.changeField(oldValue, this.woofResource, WoofSectionOutputToWoofResourceEvent.CHANGE_WOOF_RESOURCE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofSectionOutput.setWoofResource(this);
        this.woofResource.addWoofSectionOutput(this);
    }

    public void remove() {
        this.woofSectionOutput.setWoofResource(null);
        this.woofResource.removeWoofSectionOutput(this);
    }

}
