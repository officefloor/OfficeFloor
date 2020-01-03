package net.officefloor.script;

import java.util.List;

import javax.script.ScriptEngine;

import lombok.Data;

/**
 * Meta-data for a {@link ScriptEngine} function.
 * 
 * @author Daniel Sagenschneider
 */
@Data
public class ScriptFunctionMetaData {
	private List<ScriptParameterMetaData> parameters;
	private String nextArgumentType;
	private String error;
}