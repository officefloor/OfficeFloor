package net.officefloor.plugin.managedfunction.method;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.section.clazz.Flow;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.test.OfficeFloorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Ensure can inject via {@link Flow} annotation.
 */
@ExtendWith(OfficeFloorExtension.class)
public class FlowInjectTest {

    @Test
    public void runnable() {
        Closure<Boolean> invoked = new Closure<>();
        RunnableFunction instance = new RunnableFunction();
        MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
            type.addFlow().setLabel("flow");
        }, (context) -> {
            context.setFlow(0, (parameter, callback) -> {
                assertNull(parameter, "Should not have parameter");
                assertNull(callback, "Should not have callback");
                invoked.value = true;
            });
        });
        assertTrue(invoked.value, "Flow should be invoked");
    }

    public static class RunnableFunction {
        public void method(@Flow("flow") Runnable flow) {
            flow.run();
        }
    }

    @Test
    public void consumer() {
        Closure<Object> argument = new Closure<>();
        ConsumerFunction instance = new ConsumerFunction();
        MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
            type.addFlow().setLabel("accept").setArgumentType(Object.class);
        }, (context) -> {
            context.setFlow(0, (parameter, callback) -> {
                argument.value = parameter;
            });
        });
        assertEquals("PARAMETER", argument.value, "Incorrect parameter in invoking flow");
    }

    public static class ConsumerFunction {
        public void method(@Flow("accept") Consumer<String> invoke) {
            invoke.accept("PARAMETER");
        }
    }

    @Test
    public void typedParameter() {
        Closure<String> argument = new Closure<>();
        TypedParameterFunction instance = new TypedParameterFunction();
        MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
            type.addFlow().setLabel("invoke").setArgumentType(String.class);
        }, (context) -> {
            context.setFlow(0, (parameter, callback) -> {
                argument.value = (String) parameter;
            });
        });
        assertEquals("PARAMETER", argument.value, "Incorrect parameter in invoking flow");
    }

    @FunctionalInterface
    public static interface Invoke {
        void invoke(String parameter);
    }

    public static class TypedParameterFunction {
        public void method(@Flow("invoke") Invoke invoke) {
            invoke.invoke("PARAMETER");
        }
    }

    @Test
    public void flowCallback() {
        Closure<String> argument = new Closure<>();
        FlowCallbackFunction instance = new FlowCallbackFunction();
        MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
            type.addObject(Closure.class).setLabel(Closure.class.getName());
            type.addFlow().setLabel("callback");
        }, (context) -> {
            context.setObject(0, argument);
            context.setFlow(0, (parameter, callback) -> {
                assertNull(parameter, "Should not have parameter");
                try {
                    callback.run(null);
                } catch (Throwable ex) {
                    fail(ex);
                }
            });
        });
        assertEquals("CALLBACK", argument.value, "Incorrect callback value");
    }

    @FunctionalInterface
    public static interface Callback {
        void invoke(FlowCallback callback);
    }

    public static class FlowCallbackFunction {
        public void method(Closure<String> parameter, @Flow("callback") Callback callback) {
            callback.invoke((ex) -> parameter.value = "CALLBACK");
        }
    }

    @Test
    public void parameterAndCallback() {
        Closure<String> argument = new Closure<>();
        ParameterCallbackFunction instance = new ParameterCallbackFunction();
        MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
            type.addObject(Closure.class).setLabel(Closure.class.getName());
            type.addFlow().setLabel("callback").setArgumentType(String.class);
        }, (context) -> {
            context.setObject(0, argument);
            context.setFlow(0, (parameter, callback) -> {
                assertEquals("PARAMETER", parameter, "Incorrect parameter in invoking flow");
                try {
                    callback.run(null);
                } catch (Throwable ex) {
                    fail(ex);
                }
            });
        });
        assertEquals("CALLBACK", argument.value, "Incorrect callback value");
    }

    @FunctionalInterface
    public static interface ParameterCallback {
        void invoke(String parameter, FlowCallback callback);
    }

    public static class ParameterCallbackFunction {
        public void method(Closure<String> parameter, @Flow("callback") ParameterCallback callback) {
            callback.invoke("PARAMETER", (ex) -> parameter.value = "CALLBACK");
        }
    }

}
