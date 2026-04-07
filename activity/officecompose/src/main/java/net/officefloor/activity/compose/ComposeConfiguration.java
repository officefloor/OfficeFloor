package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ComposeConfiguration {

    @JsonIgnore
    private String start;

    private CompositionConfiguration composition;

    @JsonIgnore
    private Map<String, FunctionConfiguration> functions = new HashMap<>();

    @JsonAnySetter
    public void setFunction(String functionName, FunctionConfiguration functionConfig) {

        // Capture the first function as starting function
        if (this.start == null) {
            this.start = functionName;
        }

        // Include the function
        this.functions.put(functionName, functionConfig);
    }
}
