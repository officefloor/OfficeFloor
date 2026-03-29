package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ComposeConfig {

    @JsonIgnore
    private String start;

    private CompositionConfig composition;

    @JsonIgnore
    private Map<String, FunctionConfig> functions = new HashMap<>();

    @JsonAnySetter
    public void setFunction(String functionName, FunctionConfig functionConfig) {

        // Capture the first function as starting function
        if (this.start == null) {
            this.start = functionName;
        }

        // Include the function
        this.functions.put(functionName, functionConfig);
    }
}
