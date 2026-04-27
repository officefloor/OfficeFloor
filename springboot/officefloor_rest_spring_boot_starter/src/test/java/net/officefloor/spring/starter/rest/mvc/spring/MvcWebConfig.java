package net.officefloor.spring.starter.rest.mvc.spring;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

@Configuration
public class MvcWebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<MvcFilter> mvcFilter() {
        FilterRegistrationBean<MvcFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MvcFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

}
