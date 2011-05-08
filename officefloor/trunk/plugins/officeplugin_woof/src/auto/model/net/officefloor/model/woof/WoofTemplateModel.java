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
public class WoofTemplateModel extends AbstractModel implements ItemModel<WoofTemplateModel> {

    public static enum WoofTemplateEvent {
     CHANGE_WOOF_TEMPLATE_NAME, CHANGE_URI, CHANGE_TEMPLATE_PATH, CHANGE_TEMPLATE_CLASS_NAME, ADD_OUTPUT, REMOVE_OUTPUT, ADD_WOOF_SECTION_OUTPUT, REMOVE_WOOF_SECTION_OUTPUT, ADD_WOOF_TEMPLATE_OUTPUT, REMOVE_WOOF_TEMPLATE_OUTPUT, ADD_WOOF_EXCEPTION, REMOVE_WOOF_EXCEPTION
    }

    /**
     * Default constructor.
     */
    public WoofTemplateModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofTemplateModel(
      String woofTemplateName
    , String uri
    , String templatePath
    , String templateClassName
    ) {
        this.woofTemplateName = woofTemplateName;
        this.uri = uri;
        this.templatePath = templatePath;
        this.templateClassName = templateClassName;
    }

    /**
     * Convenience constructor.
     */
    public WoofTemplateModel(
      String woofTemplateName
    , String uri
    , String templatePath
    , String templateClassName
    , WoofTemplateOutputModel[] output
    , WoofSectionOutputToWoofTemplateModel[] woofSectionOutput
    , WoofTemplateOutputToWoofTemplateModel[] woofTemplateOutput
    , WoofExceptionToWoofTemplateModel[] woofException
    ) {
        this.woofTemplateName = woofTemplateName;
        this.uri = uri;
        this.templatePath = templatePath;
        this.templateClassName = templateClassName;
        if (output != null) {
            for (WoofTemplateOutputModel model : output) {
                this.output.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofTemplateModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofTemplateModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofTemplateModel model : woofException) {
                this.woofException.add(model);
            }
        }
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofTemplateModel(
      String woofTemplateName
    , String uri
    , String templatePath
    , String templateClassName
    , WoofTemplateOutputModel[] output
    , WoofSectionOutputToWoofTemplateModel[] woofSectionOutput
    , WoofTemplateOutputToWoofTemplateModel[] woofTemplateOutput
    , WoofExceptionToWoofTemplateModel[] woofException
    , int x
    , int y
    ) {
        this.woofTemplateName = woofTemplateName;
        this.uri = uri;
        this.templatePath = templatePath;
        this.templateClassName = templateClassName;
        if (output != null) {
            for (WoofTemplateOutputModel model : output) {
                this.output.add(model);
            }
        }
        if (woofSectionOutput != null) {
            for (WoofSectionOutputToWoofTemplateModel model : woofSectionOutput) {
                this.woofSectionOutput.add(model);
            }
        }
        if (woofTemplateOutput != null) {
            for (WoofTemplateOutputToWoofTemplateModel model : woofTemplateOutput) {
                this.woofTemplateOutput.add(model);
            }
        }
        if (woofException != null) {
            for (WoofExceptionToWoofTemplateModel model : woofException) {
                this.woofException.add(model);
            }
        }
        this.setX(x);
        this.setY(y);
    }

    /**
     * Woof template name.
     */
    private String woofTemplateName;

    public String getWoofTemplateName() {
        return this.woofTemplateName;
    }

    public void setWoofTemplateName(String woofTemplateName) {
        String oldValue = this.woofTemplateName;
        this.woofTemplateName = woofTemplateName;
        this.changeField(oldValue, this.woofTemplateName, WoofTemplateEvent.CHANGE_WOOF_TEMPLATE_NAME);
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
        this.changeField(oldValue, this.uri, WoofTemplateEvent.CHANGE_URI);
    }

    /**
     * Template path.
     */
    private String templatePath;

    public String getTemplatePath() {
        return this.templatePath;
    }

    public void setTemplatePath(String templatePath) {
        String oldValue = this.templatePath;
        this.templatePath = templatePath;
        this.changeField(oldValue, this.templatePath, WoofTemplateEvent.CHANGE_TEMPLATE_PATH);
    }

    /**
     * Template class name.
     */
    private String templateClassName;

    public String getTemplateClassName() {
        return this.templateClassName;
    }

    public void setTemplateClassName(String templateClassName) {
        String oldValue = this.templateClassName;
        this.templateClassName = templateClassName;
        this.changeField(oldValue, this.templateClassName, WoofTemplateEvent.CHANGE_TEMPLATE_CLASS_NAME);
    }


    /**
     * Output.
     */
    private List<WoofTemplateOutputModel> output = new LinkedList<WoofTemplateOutputModel>();

    public List<WoofTemplateOutputModel> getOutputs() {
        return this.output;
    }

    public void addOutput(WoofTemplateOutputModel output) {
        this.addItemToList(output, this.output, WoofTemplateEvent.ADD_OUTPUT);
    }

    public void removeOutput(WoofTemplateOutputModel output) {
        this.removeItemFromList(output, this.output, WoofTemplateEvent.REMOVE_OUTPUT);
    }

    /**
     * Woof section output.
     */
    private List<WoofSectionOutputToWoofTemplateModel> woofSectionOutput = new LinkedList<WoofSectionOutputToWoofTemplateModel>();

    public List<WoofSectionOutputToWoofTemplateModel> getWoofSectionOutputs() {
        return this.woofSectionOutput;
    }

    public void addWoofSectionOutput(WoofSectionOutputToWoofTemplateModel woofSectionOutput) {
        this.addItemToList(woofSectionOutput, this.woofSectionOutput, WoofTemplateEvent.ADD_WOOF_SECTION_OUTPUT);
    }

    public void removeWoofSectionOutput(WoofSectionOutputToWoofTemplateModel woofSectionOutput) {
        this.removeItemFromList(woofSectionOutput, this.woofSectionOutput, WoofTemplateEvent.REMOVE_WOOF_SECTION_OUTPUT);
    }

    /**
     * Woof template output.
     */
    private List<WoofTemplateOutputToWoofTemplateModel> woofTemplateOutput = new LinkedList<WoofTemplateOutputToWoofTemplateModel>();

    public List<WoofTemplateOutputToWoofTemplateModel> getWoofTemplateOutputs() {
        return this.woofTemplateOutput;
    }

    public void addWoofTemplateOutput(WoofTemplateOutputToWoofTemplateModel woofTemplateOutput) {
        this.addItemToList(woofTemplateOutput, this.woofTemplateOutput, WoofTemplateEvent.ADD_WOOF_TEMPLATE_OUTPUT);
    }

    public void removeWoofTemplateOutput(WoofTemplateOutputToWoofTemplateModel woofTemplateOutput) {
        this.removeItemFromList(woofTemplateOutput, this.woofTemplateOutput, WoofTemplateEvent.REMOVE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof exception.
     */
    private List<WoofExceptionToWoofTemplateModel> woofException = new LinkedList<WoofExceptionToWoofTemplateModel>();

    public List<WoofExceptionToWoofTemplateModel> getWoofExceptions() {
        return this.woofException;
    }

    public void addWoofException(WoofExceptionToWoofTemplateModel woofException) {
        this.addItemToList(woofException, this.woofException, WoofTemplateEvent.ADD_WOOF_EXCEPTION);
    }

    public void removeWoofException(WoofExceptionToWoofTemplateModel woofException) {
        this.removeItemFromList(woofException, this.woofException, WoofTemplateEvent.REMOVE_WOOF_EXCEPTION);
    }


    /**
     * Remove Connections.
     */
    public RemoveConnectionsAction<WoofTemplateModel> removeConnections() {
        RemoveConnectionsAction<WoofTemplateModel> _action = new RemoveConnectionsAction<WoofTemplateModel>(this);
        _action.disconnect(this.woofSectionOutput);
        _action.disconnect(this.woofTemplateOutput);
        _action.disconnect(this.woofException);
        return _action;
    }
}
