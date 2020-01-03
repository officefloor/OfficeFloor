package net.officefloor.script;

import javax.script.ScriptEngine;

import lombok.Data;

/**
 * Meta-data for a {@link ScriptEngine} function.
 * 
 * @author Daniel Sagenschneider
 */
@Data
public class ScriptParameterMetaData {
	private String name;
	private String qualifier;
	private String type;
	private String nature;
}