/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofTemplateOutputToWoofTemplateModel extends AbstractModel implements ConnectionModel {

    public static enum WoofTemplateOutputToWoofTemplateEvent {
     CHANGE_TEMPLATE_NAME, CHANGE_WOOF_TEMPLATE_OUTPUT, CHANGE_WOOF_TEMPLATE
    }

    /**
     * Default constructor.
     */
    public WoofTemplateOutputToWoofTemplateModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofTemplateOutputToWoofTemplateModel(
      String templateName
    ) {
        this.templateName = templateName;
    }

    /**
     * Convenience constructor.
     */
    public WoofTemplateOutputToWoofTemplateModel(
      String templateName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofTemplateModel woofTemplate
    ) {
        this.templateName = templateName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofTemplate = woofTemplate;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofTemplateOutputToWoofTemplateModel(
      String templateName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofTemplateModel woofTemplate
    , int x
    , int y
    ) {
        this.templateName = templateName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofTemplate = woofTemplate;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Template name.
     */
    private String templateName;

    public String getTemplateName() {
        return this.templateName;
    }

    public void setTemplateName(String templateName) {
        String oldValue = this.templateName;
        this.templateName = templateName;
        this.changeField(oldValue, this.templateName, WoofTemplateOutputToWoofTemplateEvent.CHANGE_TEMPLATE_NAME);
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
        this.changeField(oldValue, this.woofTemplateOutput, WoofTemplateOutputToWoofTemplateEvent.CHANGE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof template.
     */
    private WoofTemplateModel woofTemplate;

    public WoofTemplateModel getWoofTemplate() {
        return this.woofTemplate;
    }

    public void setWoofTemplate(WoofTemplateModel woofTemplate) {
        WoofTemplateModel oldValue = this.woofTemplate;
        this.woofTemplate = woofTemplate;
        this.changeField(oldValue, this.woofTemplate, WoofTemplateOutputToWoofTemplateEvent.CHANGE_WOOF_TEMPLATE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofTemplateOutput.setWoofTemplate(this);
        this.woofTemplate.addWoofTemplateOutput(this);
    }

    public void remove() {
        this.woofTemplateOutput.setWoofTemplate(null);
        this.woofTemplate.removeWoofTemplateOutput(this);
    }

}
