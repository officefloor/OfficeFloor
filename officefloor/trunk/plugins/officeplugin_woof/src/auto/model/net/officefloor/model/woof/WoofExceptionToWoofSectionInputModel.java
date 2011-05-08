/*
 * 
 */
package net.officefloor.model.woof;

import javax.annotation.Generated;

import net.officefloor.model.AbstractModel;
import net.officefloor.model.ConnectionModel;

@Generated("net.officefloor.model.generate.ModelGenerator")
public class WoofExceptionToWoofSectionInputModel extends AbstractModel implements ConnectionModel {

    public static enum WoofExceptionToWoofSectionInputEvent {
     CHANGE_SECTION_NAME, CHANGE_INPUT_NAME, CHANGE_WOOF_EXCEPTION, CHANGE_WOOF_SECTION_INPUT
    }

    /**
     * Default constructor.
     */
    public WoofExceptionToWoofSectionInputModel() {
    }

    /**
     * Convenience constructor for new non-linked instance.
     */
    public WoofExceptionToWoofSectionInputModel(
      String sectionName
    , String inputName
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
    }

    /**
     * Convenience constructor.
     */
    public WoofExceptionToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofExceptionModel woofException
    , WoofSectionInputModel woofSectionInput
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofException = woofException;
        this.woofSectionInput = woofSectionInput;
    }

    /**
     * Convenience constructor allowing XY initialising.
     */
    public WoofExceptionToWoofSectionInputModel(
      String sectionName
    , String inputName
    , WoofExceptionModel woofException
    , WoofSectionInputModel woofSectionInput
    , int x
    , int y
    ) {
        this.sectionName = sectionName;
        this.inputName = inputName;
        this.woofException = woofException;
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
        this.changeField(oldValue, this.sectionName, WoofExceptionToWoofSectionInputEvent.CHANGE_SECTION_NAME);
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
        this.changeField(oldValue, this.inputName, WoofExceptionToWoofSectionInputEvent.CHANGE_INPUT_NAME);
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
        this.changeField(oldValue, this.woofException, WoofExceptionToWoofSectionInputEvent.CHANGE_WOOF_EXCEPTION);
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
        this.changeField(oldValue, this.woofSectionInput, WoofExceptionToWoofSectionInputEvent.CHANGE_WOOF_SECTION_INPUT);
    }



    /*
     * ConnectionModel
     */
    public boolean isRemovable() {
        return true;
    }

    public void connect() {
        this.woofException.setWoofSectionInput(this);
        this.woofSectionInput.addWoofException(this);
    }

    public void remove() {
        this.woofException.setWoofSectionInput(null);
        this.woofSectionInput.removeWoofException(this);
    }

}
