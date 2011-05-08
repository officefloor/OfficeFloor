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
public class WoofSectionInputModel extends AbstractModel implements ItemModel<WoofSectionInputModel> {

    public static enum WoofSectionInputEvent {
     CHANGE_WOOF_SECTION_INPUT_NAME, CHANGE_PARAMETER_TYPE, CHANGE_URI, ADD_WOOF_TEMPLATE_OUTPUT, REMOVE_WOOF_TEMPLATE_OUTPUT, ADD_WOOF_SECTION_OUTPUT, REMOVE_WOOF_SECTION_OUTPUT, ADD_WOOF_EXCEPTION, REMOVE_WOOF_EXCEPTION
    }

    /**
     * Default constructor.
     */
    public WoofSectionInputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionInputModel(
      String woofSectionInputName
    , String parameterType
    , String uri
    ) {
        this.woofSectionInputName = woofSectionInputName;
        this.parameterType = parameterType;
        this.uri = uri;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionInputModel(
      String woofSectionInputName
    , String parameterType
    , String uri
    , WoofTemplateOutputToWoofSectionInputModel[] woofTemplateOutput
    , WoofSectionOutputToWoofSectionInputModel[] woofSectionOutput
    , WoofExceptionToWoofSectionInputModel[] woofException
    ) {
        this.woofSectionInputName = woofSectionInputName;
        this.parameterType = parameterType;
        this.uri = uri;
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofSectionInputModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofSectionInputModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofSectionInputModel model : woofException) {
                this.woofException.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionInputModel(
      String woofSectionInputName
    , String parameterType
    , String uri
    , WoofTemplateOutputToWoofSectionInputModel[] woofTemplateOutput
    , WoofSectionOutputToWoofSectionInputModel[] woofSectionOutput
    , WoofExceptionToWoofSectionInputModel[] woofException
    , int x
    , int y
    ) {
        this.woofSectionInputName = woofSectionInputName;
        this.parameterType = parameterType;
        this.uri = uri;
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofSectionInputModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofSectionInputModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofSectionInputModel model : woofException) {
                this.woofException.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof section input name.
     */
    private String woofSectionInputName;

    public String getWoofSectionInputName() {
        return this.woofSectionInputName;
    }

    public void setWoofSectionInputName(String woofSectionInputName) {
        String oldValue = this.woofSectionInputName;
        this.woofSectionInputName = woofSectionInputName;
        this.changeField(oldValue, this.woofSectionInputName, WoofSectionInputEvent.CHANGE_WOOF_SECTION_INPUT_NAME);
    }

    /**
     * Parameter type.
     */
    private String parameterType;

    public String getParameterType() {
        return this.parameterType;
    }

    public void setParameterType(String parameterType) {
        String oldValue = this.parameterType;
        this.parameterType = parameterType;
        this.changeField(oldValue, this.parameterType, WoofSectionInputEvent.CHANGE_PARAMETER_TYPE);
    }

    /**
     * Uri.
     */
    private String uri;

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        String oldValue = this.uri;
        this.uri = uri;
        this.changeField(oldValue, this.uri, WoofSectionInputEvent.CHANGE_URI);
    }


    /**
     * Woof template output.
     */
    private List<WoofTemplateOutputToWoofSectionInputModel> woofTemplateOutput = new LinkedList<WoofTemplateOutputToWoofSectionInputModel>();

    public List<WoofTemplateOutputToWoofSectionInputModel> getWoofTemplateOutputs() {
        return this.woofTemplateOutput;
    }

    public void addWoofTemplateOutput(WoofTemplateOutputToWoofSectionInputModel woofTemplateOutput) {
        this.addItemToList(woofTemplateOutput, this.woofTemplateOutput, WoofSectionInputEvent.ADD_WOOF_TEMPLATE_OUTPUT);
    }

    public void removeWoofTemplateOutput(WoofTemplateOutputToWoofSectionInputModel woofTemplateOutput) {
        this.removeItemFromList(woofTemplateOutput, this.woofTemplateOutput, WoofSectionInputEvent.REMOVE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof section output.
     */
    private List<WoofSectionOutputToWoofSectionInputModel> woofSectionOutput = new LinkedList<WoofSectionOutputToWoofSectionInputModel>();

    public List<WoofSectionOutputToWoofSectionInputModel> getWoofSectionOutputs() {
        return this.woofSectionOutput;
    }

    public void addWoofSectionOutput(WoofSectionOutputToWoofSectionInputModel woofSectionOutput) {
        this.addItemToList(woofSectionOutput, this.woofSectionOutput, WoofSectionInputEvent.ADD_WOOF_SECTION_OUTPUT);
    }

    public void removeWoofSectionOutput(WoofSectionOutputToWoofSectionInputModel woofSectionOutput) {
        this.removeItemFromList(woofSectionOutput, this.woofSectionOutput, WoofSectionInputEvent.REMOVE_WOOF_SECTION_OUTPUT);
    }

    /**
     * Woof exception.
     */
    private List<WoofExceptionToWoofSectionInputModel> woofException = new LinkedList<WoofExceptionToWoofSectionInputModel>();

    public List<WoofExceptionToWoofSectionInputModel> getWoofExceptions() {
        return this.woofException;
    }

    public void addWoofException(WoofExceptionToWoofSectionInputModel woofException) {
        this.addItemToList(woofException, this.woofException, WoofSectionInputEvent.ADD_WOOF_EXCEPTION);
    }

    public void removeWoofException(WoofExceptionToWoofSectionInputModel woofException) {
        this.removeItemFromList(woofException, this.woofException, WoofSectionInputEvent.REMOVE_WOOF_EXCEPTION);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofSectionInputModel> removeConnections() {
        RemoveConnectionsAction<WoofSectionInputModel> _action = new RemoveConnectionsAction<WoofSectionInputModel>(this);
        _action.disconnect(this.woofTemplateOutput);
        _action.disconnect(this.woofSectionOutput);
        _action.disconnect(this.woofException);
        return _action;
    }
}
