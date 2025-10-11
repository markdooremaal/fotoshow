package com.example;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class SlideshowResourceTest {

    @Test
    void showBindsDefaultsAndImagesIntoTemplate() {
        // Arrange: a dynamic-proxy stub for Template/TemplateInstance capturing data
        CapturingTemplateStub templateStub = new CapturingTemplateStub();
        Template templateProxy = templateStub.createTemplateProxy();
        SlideshowResource resource = new SlideshowResource(templateProxy);

        // Act
        TemplateInstance instance = resource.show();

        // Assert: inspect the captured data from the TemplateInstance proxy's handler
        Map<String, Object> data = CapturingTemplateStub.getCapturedDataFromInstance(instance);

        // Defaults when no env vars are provided
        assertEquals("#111111", data.get("backdropColor"));
        assertEquals(3000, data.get("refreshMs"));

        @SuppressWarnings("unchecked")
        List<String> images = (List<String>) data.get("images");
        assertNotNull(images);
        // By default we expect 5 images to be resolved from the classpath
        assertEquals(5, images.size());
        // And they should be data URIs
        assertTrue(images.get(0).startsWith("data:image/"));
    }

    /**
     * A generic dynamic-proxy based stub for Qute Template and TemplateInstance.
     * It captures data(key,value) calls into an internal map.
     */
    static class CapturingTemplateStub {
        Map<String, Object> captured = new HashMap<>();

        Template createTemplateProxy() {
            InvocationHandler templateHandler = (proxy, method, args) -> {
                String name = method.getName();
                if (name.equals("data") && args != null && args.length == 2) {
                    // calls Template.data -> return a TemplateInstance proxy with the first key/value stored
                    TemplateInstance inst = createTemplateInstanceProxy();
                    method.invoke(inst, args); // delegate to instance.data
                    return inst;
                }
                if (name.equals("instance")) {
                    return createTemplateInstanceProxy();
                }
                // default stubs for other methods
                return defaultReturn(method.getReturnType());
            };
            return (Template) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class[]{Template.class}, templateHandler);
        }

        TemplateInstance createTemplateInstanceProxy() {
            TemplateInstanceHandler instanceHandler = new TemplateInstanceHandler(captured);
            return (TemplateInstance) Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class[]{TemplateInstance.class}, instanceHandler);
        }

        static Map<String, Object> getCapturedDataFromInstance(TemplateInstance instance) {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return ((CapturingTemplateStub.TemplateInstanceHandler) handler).captured;
        }

        class TemplateInstanceHandler implements InvocationHandler {
            final Map<String, Object> captured;
            TemplateInstanceHandler(Map<String, Object> captured) { this.captured = captured; }
            @Override public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if ("data".equals(name)) { this.captured.put((String) args[0], args[1]); return proxy; }
                if ("render".equals(name)) return "";
                if ("renderAsync".equals(name)) return CompletableFuture.completedFuture("");
                if ("consume".equals(name)) return CompletableFuture.completedFuture(null);
                return defaultReturn(method.getReturnType());
            }
        }

        private Object defaultReturn(Class<?> returnType) {
            if (returnType == void.class) return null;
            if (returnType == boolean.class) return false;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0f;
            if (returnType == double.class) return 0d;
            return null;
        }
    }
}
