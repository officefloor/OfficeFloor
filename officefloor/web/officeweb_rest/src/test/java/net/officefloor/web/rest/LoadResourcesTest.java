package net.officefloor.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.web.rest.config.EndpointConfig;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class LoadResourcesTest {

    @Test
    public void loadResources() throws Exception {

        // Obtain the resource
        InputStream stream = LoadResourcesTest.class.getResourceAsStream("/officefloor/rest/index.GET.yaml");

        // Load the YAML file
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        EndpointConfig config = mapper.readValue(stream, EndpointConfig.class);

        // TODO REMOVE
        System.out.println(config.toString());
    }

}
