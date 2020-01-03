package net.officefloor.spring;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Load Bean.
 * 
 * @author Daniel Sagenschneider
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoadBean {

	public static final AtomicInteger loadCount = new AtomicInteger(0);

	public LoadBean() {
		loadCount.incrementAndGet();
	}
}
