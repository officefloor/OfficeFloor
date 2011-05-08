/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofExceptionToWoofResourceModel extends AbstractModel implements ConnectionModel {

    public static enum WoofExceptionToWoofResourceEvent {
     CHANGE_RESOURCE_NAME, CHANGE_WOOF_EXCEPTION, CHANGE_WOOF_RESOURCE
    }

    /**
     * Default constructor.
     */
    public WoofExceptionToWoofResourceModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofExceptionToWoofResourceModel(
      String resourceName
    ) {
        this.resourceName = resourceName;
    }

    /**
     * Convenience constructor.
     */
    public WoofExceptionToWoofResourceModel(
      String resourceName
    , WoofExceptionModel woofException
    , WoofResourceModel woofResource
    ) {
        this.resourceName = resourceName;
        this.woofException = woofException;
        this.woofResource = woofResource;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofExceptionToWoofResourceModel(
      String resourceName
    , WoofExceptionModel woofException
    , WoofResourceModel woofResource
    , int x
    , int y
    ) {
        this.resourceName = resourceName;
        this.woofException = woofException;
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
        this.changeField(oldValue, this.resourceName, WoofExceptionToWoofResourceEvent.CHANGE_RESOURCE_NAME);
    }

    /**
     * Woof exception.
     */
    private WoofExceptionModel woofException;

    public WoofExceptionModel getWoofException() {
        return this.woofException;
    }

    public void setWoofException(WoofExceptionModel woofException) {
        WoofExceptionModel oldValue = this.woofException;
        this.woofException = woofException;
        this.changeField(oldValue, this.woofException, WoofExceptionToWoofResourceEvent.CHANGE_WOOF_EXCEPTION);
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
        this.changeField(oldValue, this.woofResource, WoofExceptionToWoofResourceEvent.CHANGE_WOOF_RESOURCE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofException.setWoofResource(this);
        this.woofResource.addWoofException(this);
    }

    public void remove() {
        this.woofException.setWoofResource(null);
        this.woofResource.removeWoofException(this);
    }

}
