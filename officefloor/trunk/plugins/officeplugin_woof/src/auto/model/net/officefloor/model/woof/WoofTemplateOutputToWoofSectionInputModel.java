/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofTemplateOutputToWoofSectionInputModel extends AbstractModel implements ConnectionModel {

    public static enum WoofTemplateOutputToWoofSectionInputEvent {
     CHANGE_SECTION_NAME, CHANGE_INPUT_NAME, CHANGE_WOOF_TEMPLATE_OUTPUT, CHANGE_WOOF_SECTION_INPUT
    }

    /**
     * Default constructor.
     */
    public WoofTemplateOutputToWoofSectionInputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofTemplateOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
    }

    /**
     * Convenience constructor.
     */
    public WoofTemplateOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofSectionInputModel woofSectionInput
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofSectionInput = woofSectionInput;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofTemplateOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofTemplateOutputModel woofTemplateOutput
    , WoofSectionInputModel woofSectionInput
    , int x
    , int y
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofTemplateOutput = woofTemplateOutput;
        this.woofSectionInput = woofSectionInput;
        this.setX(x);
        this.setY(y);
    }

    /**
     * Section name.
     */
    private String sectionName;

    public String getSectionName() {
        return this.sectionName;
    }

    public void setSectionName(String sectionName) {
        String oldValue = this.sectionName;
        this.sectionName = sectionName;
        this.changeField(oldValue, this.sectionName, WoofTemplateOutputToWoofSectionInputEvent.CHANGE_SECTION_NAME);
    }

    /**
     * Input name.
     */
    private String inputName;

    public String getInputName() {
        return this.inputName;
    }

    public void setInputName(String inputName) {
        String oldValue = this.inputName;
        this.inputName = inputName;
        this.changeField(oldValue, this.inputName, WoofTemplateOutputToWoofSectionInputEvent.CHANGE_INPUT_NAME);
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
        this.changeField(oldValue, this.woofTemplateOutput, WoofTemplateOutputToWoofSectionInputEvent.CHANGE_WOOF_TEMPLATE_OUTPUT);
    }

    /**
     * Woof section input.
     */
    private WoofSectionInputModel woofSectionInput;

    public WoofSectionInputModel getWoofSectionInput() {
        return this.woofSectionInput;
    }

    public void setWoofSectionInput(WoofSectionInputModel woofSectionInput) {
        WoofSectionInputModel oldValue = this.woofSectionInput;
        this.woofSectionInput = woofSectionInput;
        this.changeField(oldValue, this.woofSectionInput, WoofTemplateOutputToWoofSectionInputEvent.CHANGE_WOOF_SECTION_INPUT);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofTemplateOutput.setWoofSectionInput(this);
        this.woofSectionInput.addWoofTemplateOutput(this);
    }

    public void remove() {
        this.woofTemplateOutput.setWoofSectionInput(null);
        this.woofSectionInput.removeWoofTemplateOutput(this);
    }

}
