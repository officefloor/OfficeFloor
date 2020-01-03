/*-
 * #%L
 * OfficeXml
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.plugin.xml.unmarshall.flat;

import java.util.Date;

/**
 * Target object to have XML data loaded onto it.
 * 
 * @author Daniel Sagenschneider
 */
public class TargetObject {

    /**
     * String value.
     */
    protected String stringValue;

    public void setString(String value) {
        this.stringValue = value;
    }

    public String getString() {
        return this.stringValue;
    }

    /**
     * boolean value.
     */
    protected boolean booleanValue;

    public void setBoolean(boolean value) {
        this.booleanValue = value;
    }

    public boolean getBoolean() {
        return this.booleanValue;
    }

    /**
     * Boolean object.
     */
    protected Boolean booleanObject;

    public void setBooleanObject(Boolean value) {
        this.booleanObject = value;
    }

    public Boolean getBooleanObject() {
        return this.booleanObject;
    }

    /**
     * char value.
     */
    protected char charValue;

    public void setChar(char value) {
        this.charValue = value;
    }

    public char getChar() {
        return this.charValue;
    }

    /**
     * Charactor object.
     */
    protected Character charactor;

    public void setCharacter(Character value) {
        this.charactor = value;
    }

    public Character getCharacter() {
        return this.charactor;
    }

    /**
     * byte value.
     */
    protected byte byteValue;

    public void setByte(byte value) {
        this.byteValue = value;
    }

    public byte getByte() {
        return this.byteValue;
    }

    /**
     * Byte object.
     */
    protected Byte byteObject;

    public void setByteObject(Byte value) {
        this.byteObject = value;
    }

    public Byte getByteObject() {
        return this.byteObject;
    }

    /**
     * int value.
     */
    protected int intValue;

    public int getInt() {
        return this.intValue;
    }

    public void setInt(int value) {
        this.intValue = value;
    }

    /**
     * Integer object.
     */
    protected Integer integerObject;

    public Integer getInteger() {
        return this.integerObject;
    }

    public void setInteger(Integer value) {
        this.integerObject = value;
    }

    /**
     * long value.
     */
    protected long longValue;

    public long getLong() {
        return this.longValue;
    }

    public void setLong(long value) {
        this.longValue = value;
    }

    /**
     * Long object.
     */
    protected Long longObject;

    public Long getLongObject() {
        return this.longObject;
    }

    public void setLongObject(Long value) {
        this.longObject = value;
    }

    /**
     * float value.
     */
    protected float floatValue;

    public float getFloat() {
        return this.floatValue;
    }

    public void setFloat(float value) {
        this.floatValue = value;
    }

    /**
     * Float object.
     */
    protected Float floatObject;

    public Float getFloatObject() {
        return this.floatObject;
    }

    public void setFloatObject(Float value) {
        this.floatObject = value;
    }

    /**
     * double value.
     */
    protected double doubleValue;

    public double getDouble() {
        return this.doubleValue;
    }

    public void setDouble(double value) {
        this.doubleValue = value;
    }

    /**
     * Double object.
     */
    protected Double doubleObject;

    public Double getDoubleObject() {
        return this.doubleObject;
    }

    public void setDoubleObject(Double value) {
        this.doubleObject = value;
    }

    /**
     * Date object.
     */
    protected Date dateObject;

    public Date getDate() {
        return this.dateObject;
    }

    public void setDate(Date value) {
        this.dateObject = value;
    }
}
