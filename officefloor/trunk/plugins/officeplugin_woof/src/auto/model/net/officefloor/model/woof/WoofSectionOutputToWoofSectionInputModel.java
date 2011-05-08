/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofSectionOutputToWoofSectionInputModel extends AbstractModel implements ConnectionModel {

    public static enum WoofSectionOutputToWoofSectionInputEvent {
     CHANGE_SECTION_NAME, CHANGE_INPUT_NAME, CHANGE_WOOF_SECTION_OUTPUT, CHANGE_WOOF_SECTION_INPUT
    }

    /**
     * Default constructor.
     */
    public WoofSectionOutputToWoofSectionInputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofSectionOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
    }

    /**
     * Convenience constructor.
     */
    public WoofSectionOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofSectionOutputModel woofSectionOutput
    , WoofSectionInputModel woofSectionInput
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofSectionOutput = woofSectionOutput;
        this.woofSectionInput = woofSectionInput;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofSectionOutputToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofSectionOutputModel woofSectionOutput
    , WoofSectionInputModel woofSectionInput
    , int x
    , int y
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofSectionOutput = woofSectionOutput;
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
        this.changeField(oldValue, this.sectionName, WoofSectionOutputToWoofSectionInputEvent.CHANGE_SECTION_NAME);
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
        this.changeField(oldValue, this.inputName, WoofSectionOutputToWoofSectionInputEvent.CHANGE_INPUT_NAME);
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
        this.changeField(oldValue, this.woofSectionOutput, WoofSectionOutputToWoofSectionInputEvent.CHANGE_WOOF_SECTION_OUTPUT);
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
        this.changeField(oldValue, this.woofSectionInput, WoofSectionOutputToWoofSectionInputEvent.CHANGE_WOOF_SECTION_INPUT);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofSectionOutput.setWoofSectionInput(this);
        this.woofSectionInput.addWoofSectionOutput(this);
    }

    public void remove() {
        this.woofSectionOutput.setWoofSectionInput(null);
        this.woofSectionInput.removeWoofSectionOutput(this);
    }

}
