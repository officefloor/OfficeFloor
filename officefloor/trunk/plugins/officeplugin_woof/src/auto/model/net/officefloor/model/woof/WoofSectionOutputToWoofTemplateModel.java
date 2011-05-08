/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofSectionOutputToWoofTemplateModel extends AbstractModel implements ConnectionModel {

    public static enum WoofSectionOutputToWoofTemplateEvent {
     CHANGE_TEMPLATE_NAME, CHANGE_WOOF_SECTION_OUTPUT, CHANGE_WOOF_TEMPLATE
    }

    /**
     * Default constructor.
     */
    public WoofSectionOutputToWoofTemplateModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionOutputToWoofTemplateModel(
      String templateName
    ) {
        this.templateName = templateName;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionOutputToWoofTemplateModel(
      String templateName
    , WoofSectionOutputModel woofSectionOutput
    , WoofTemplateModel woofTemplate
    ) {
        this.templateName = templateName;
        this.woofSectionOutput = woofSectionOutput;
        this.woofTemplate = woofTemplate;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionOutputToWoofTemplateModel(
      String templateName
    , WoofSectionOutputModel woofSectionOutput
    , WoofTemplateModel woofTemplate
    , int x
    , int y
    ) {
        this.templateName = templateName;
        this.woofSectionOutput = woofSectionOutput;
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
        this.changeField(oldValue, this.templateName, WoofSectionOutputToWoofTemplateEvent.CHANGE_TEMPLATE_NAME);
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
        this.changeField(oldValue, this.woofSectionOutput, WoofSectionOutputToWoofTemplateEvent.CHANGE_WOOF_SECTION_OUTPUT);
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
        this.changeField(oldValue, this.woofTemplate, WoofSectionOutputToWoofTemplateEvent.CHANGE_WOOF_TEMPLATE);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofSectionOutput.setWoofTemplate(this);
        this.woofTemplate.addWoofSectionOutput(this);
    }

    public void remove() {
        this.woofSectionOutput.setWoofTemplate(null);
        this.woofTemplate.removeWoofSectionOutput(this);
    }

}
