package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectParserFactory;
import net.officefloor.web.json.JacksonHttpObjectParserServiceFactory;
import net.officefloor.web.json.JacksonHttpObjectResponderFactory;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestEndpointContext;
import net.officefloor.web.rest.build.RestEndpointListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMapMethodArgumentResolver;

import java.util.List;
import java.util.Map;

public class SpringBootOfficeSource extends AbstractOfficeSource {

    private final Logger logger;

    private final ObjectMapper objectMapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    private final ConfigurableApplicationContext applicationContext;

    public SpringBootOfficeSource(Logger logger,
                                  ObjectMapper objectMapper,
                                  List<OfficeFloorRestEndpoint> restEndpoints,
                                  ConfigurableApplicationContext applicationContext) {
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.restEndpoints = restEndpoints;
        this.applicationContext = applicationContext;
    }

    /*
     * ======================= OfficeSource ========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) throws Exception {

        // Employ the architects
        WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(officeArchitect, officeSourceContext);
        ComposeArchitect<OfficeSection> composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
        RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

        // Configure object requests
        webArchitect.addHttpObjectParser(new JacksonHttpObjectParserFactory(this.objectMapper));

        // Configure object response
        webArchitect.addHttpObjectResponder(new JacksonHttpObjectResponderFactory(this.objectMapper));

        // Add the rest servicing
        this.logger.info("Loading REST endpoints:");
        PropertyList propertyList = officeSourceContext.createPropertyList();
        for (String propertyName : officeSourceContext.getPropertyNames()) {
            propertyList.addProperty(propertyName).setValue(officeSourceContext.getProperty(propertyName));
        }
        restArchitect.addRestServices(false, "officefloor/rest", propertyList, new RestEndpointListener() {
            @Override
            public void initialise(RestEndpointContext restEndpointContext) {
                logger.info("  " + restEndpointContext.getHttpMethod().getName() + " /" + restEndpointContext.getPath());
            }

            @Override
            public void endpoint(RestEndpoint restEndpoint) {

                // Register the end point
                HttpMethod httpMethod = restEndpoint.getHttpMethod();
                String path =  restEndpoint.getPath();
                ExternalServiceInput externalServiceInput = restEndpoint.getHttpInput().getDirect().addExternalServiceInput(ServerHttpConnection.class, ProcessAwareServerHttpConnectionManagedObject.class);
                SpringBootOfficeSource.this.restEndpoints.add(new OfficeFloorRestEndpoint(httpMethod, path, externalServiceInput));
            }
        });

        // Register all the Spring components for use
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

            // Obtain the bean type
            Class<?> beanType = beanFactory.getType(beanName);

            // Ensure type can be loaded (avoid lambdas)
            String beanTypeName = beanType.getName();
            if (officeSourceContext.loadOptionalClass(beanTypeName) != null) {

                // Obtain the bean qualifier
                String qualifier = null;
                if (beanDefinition instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                    AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();

                    // Obtain the qualifier
                    Map<String, Object> attributes = metadata.getAnnotationAttributes(Qualifier.class.getName());
                    qualifier = (attributes != null) ? (String) attributes.get("value") : null;
                }

                // Register bean
                String objectName = "SPRING_" + beanName;
                OfficeManagedObject beanMo = officeArchitect.addOfficeManagedObjectSource(
                                objectName,
                                new SpringBeanManagedObjectSource(beanName, beanType, beanFactory))
                        .addOfficeManagedObject(objectName, ManagedObjectScope.THREAD);
                if (qualifier != null) {
                    beanMo.addTypeQualification(qualifier, beanTypeName);
                }

            }
        }

        // Configure Office
        webArchitect.informOfficeArchitect();
    }

}
