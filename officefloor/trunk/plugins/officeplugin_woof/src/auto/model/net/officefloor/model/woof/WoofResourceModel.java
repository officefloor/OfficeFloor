/*
 * 
 */
package net.officefloor.model.woof;

import java.util.List;
import java.util.LinkedList;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ItemModel;
import net.officefloor.model.RemoveConnectionsAction;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofResourceModel extends AbstractModel implements ItemModel<WoofResourceModel> {

    public static enum WoofResourceEvent {
     CHANGE_WOOF_RESOURCE_NAME, CHANGE_RESOURCE_PATH, ADD_WOOF_TEMPLATE_OUTPUT, REMOVE_WOOF_TEMPLATE_OUTPUT, ADD_WOOF_SECTION_OUTPUT, REMOVE_WOOF_SECTION_OUTPUT, ADD_WOOF_EXCEPTION, REMOVE_WOOF_EXCEPTION
    }

    /**
     * Default constructor.
     */
    public WoofResourceModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofResourceModel(
      String woofResourceName
    , String resourcePath
    ) {
        this.woofResourceName = woofResourceName;
        this.resourcePath = resourcePath;
    }

    /**
     * Convenience constructor.
     */
    public WoofResourceModel(
      String woofResourceName
    , String resourcePath
    , WoofTemplateOutputToWoofResourceModel[] woofTemplateOutput
    , WoofSectionOutputToWoofResourceModel[] woofSectionOutput
    , WoofExceptionToWoofResourceModel[] woofException
    ) {
        this.woofResourceName = woofResourceName;
        this.resourcePath = resourcePath;
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofResourceModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofResourceModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofResourceModel model : woofException) {
                this.woofException.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofResourceModel(
      String woofResourceName
    , String resourcePath
    , WoofTemplateOutputToWoofResourceModel[] woofTemplateOutput
    , WoofSectionOutputToWoofResourceModel[] woofSectionOutput
    , WoofExceptionToWoofResourceModel[] woofException
    , int x
    , int y
    ) {
        this.woofResourceName = woofResourceName;
        this.resourcePath = resourcePath;
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofResourceModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofResourceModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofResourceModel model : woofException) {
                this.woofException.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof resource name.
     */
    private String woofResourceName;

    public String getWoofResourceName() {
        return this.woofResourceName;
    }

    public void setWoofResourceName(String woofResourceName) {
        String oldValue = this.woofResourceName;
        this.woofResourceName = woofResourceName;
        this.changeField(oldValue, this.woofResourceName, WoofResourceEvent.CHANGE_WOOF_RESOURCE_NAME);
    }

    /**
     * Resource path.
     */
    private String resourcePath;

    public String getResourcePath() {
        return this.resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        String oldValue = this.resourcePath;
        this.resourcePath = resourcePath;
        this.changeField(oldValue, this.resourcePath, WoofResourceEvent.CHANGE_RESOURCE_PATH);
    }


    /**
     * Woof template output.
     */
    private List<WoofTemplateOutputToWoofResourceModel> woofTemplateOutput = new LinkedList<WoofTemplateOutputToWoofResourceModel>();

    public List<WoofTemplateOutputToWoofResourceModel> getWoofTemplateOutputs() {
        return this.woofTemplateOutput;
    }

    public void addWoofTemplateOutput(WoofTemplateOutputToWoofResourceModel woofTemplateOutput) {
        this.addItemToList(woofTemplateOutput, this.woofTemplateOutput, WoofResourceEvent.ADD_WOOF_TEMPLATE_OUTPUT);
    }

    public void removeWoofTemplateOutput(WoofTemplateOutputToWoofResourceModel woofTemplateOutput) {
        this.removeItemFromList(woofTemplateOutput, this.woofTemplateOutput, WoofResourceEvent.REMOVE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof section output.
     */
    private List<WoofSectionOutputToWoofResourceModel> woofSectionOutput = new LinkedList<WoofSectionOutputToWoofResourceModel>();

    public List<WoofSectionOutputToWoofResourceModel> getWoofSectionOutputs() {
        return this.woofSectionOutput;
    }

    public void addWoofSectionOutput(WoofSectionOutputToWoofResourceModel woofSectionOutput) {
        this.addItemToList(woofSectionOutput, this.woofSectionOutput, WoofResourceEvent.ADD_WOOF_SECTION_OUTPUT);
    }

    public void removeWoofSectionOutput(WoofSectionOutputToWoofResourceModel woofSectionOutput) {
        this.removeItemFromList(woofSectionOutput, this.woofSectionOutput, WoofResourceEvent.REMOVE_WOOF_SECTION_OUTPUT);
    }

    /**
     * Woof exception.
     */
    private List<WoofExceptionToWoofResourceModel> woofException = new LinkedList<WoofExceptionToWoofResourceModel>();

    public List<WoofExceptionToWoofResourceModel> getWoofExceptions() {
        return this.woofException;
    }

    public void addWoofException(WoofExceptionToWoofResourceModel woofException) {
        this.addItemToList(woofException, this.woofException, WoofResourceEvent.ADD_WOOF_EXCEPTION);
    }

    public void removeWoofException(WoofExceptionToWoofResourceModel woofException) {
        this.removeItemFromList(woofException, this.woofException, WoofResourceEvent.REMOVE_WOOF_EXCEPTION);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofResourceModel> removeConnections() {
        RemoveConnectionsAction<WoofResourceModel> _action = new RemoveConnectionsAction<WoofResourceModel>(this);
        _action.disconnect(this.woofTemplateOutput);
        _action.disconnect(this.woofSectionOutput);
        _action.disconnect(this.woofException);
        return _action;
    }
}
