package com.wayleynam.http;

import com.sun.deploy.security.ValidationState;
import com.wayleynam.constants.RequestParamType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class MethodHandler {
    private final static Log logger = LogFactory.getLog(MethodHandler.class);
    private final Object object;
    private final Method method;
    private final boolean isResponseBody;
    private final boolean xssFilter;
    private RequestParamType[] requestParamType;
    private int parameterLength;
    private ObjectMapper objectMapper;
    private boolean matcherHandler;
    private Pattern pattern;
    private String[] keys;
    private boolean isVelocityTemplate;
    private String templateName;

    public MethodHandler(Object object, Method method) {
        this.object = object;
        this.method = method;
        this.isVelocityTemplate = method.isAnnotationPresent(VelocityTemplate.class);
        this.isResponseBody = method.isAnnotationPresent(ResponseBody.class);

        if (this.isResponseBody && this.isResponseBody) {
            throw new RuntimeException(object.getClass().getName() + " method " + method.getName() + " can not be annotation present both VelocityTemplate and ResponseBody")
        }
        XssFilter filter = method.getAnnotation(XssFilter.class);
        this.xssFilter = filter == null ? true : filter.value();
    }


    public void setPathPattern(Pattern pathPattern, String[] keys) {
        this.pattern = pathPattern;
        this.keys = keys;
        this.matcherHandler = true;
    }

    public void setParameterTypes(Class<?> clazz, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        this.parameterLength = parameterTypes.length;
        this.requestParamType = new RequestParamType[this.parameterLength];
        for (int i = 0; i < parameterLength; i++) {
            Class<?> classType = parameterTypes[i];
            RequestParamType paramType = new RequestParamType();
            RequestParamType.Type type;

            if (classType == HttpRequest.class) {
                type = RequestParamType.Type.HTTP_REQUEST;
            } else if (classType == HttpResponse.class) {
                type = RequestParamType.Type.HTTP_RESPONSE;
            } else {
                if (classType == String.class) {
                    type = RequestParamType.Type.STRING;
                } else if (classType.isAssignableFrom(List.class)) {
                    type = RequestParamType.Type.LIST;
                } else if (classType.isAssignableFrom(Set.class)) {
                    type = RequestParamType.Type.SET;
                } else if (classType.isAssignableFrom(Map.class)) {
                    type = RequestParamType.Type.MAP;
                } else if (classType.isArray()) {
                    type = RequestParamType.Type.ARRARY;
                } else if (classType == Boolean.class || classType == boolean.class) {
                    type = RequestParamType.Type.BOOLEAN;
                } else if (classType == Short.class || classType == short.class) {
                    type = RequestParamType.Type.SHORT;
                } else if (classType == Integer.class || classType == int.class) {
                    type = RequestParamType.Type.INTEGER;
                } else if (classType == Long.class || classType == long.class) {
                    type = RequestParamType.Type.LONG;
                } else if (classType == Float.class || classType == float.class) {
                    type = RequestParamType.Type.FLOAT;
                } else if (classType == Double.class || classType == double.class) {
                    type = RequestParamType.Type.DOUBLE;
                } else if (classType == Character.class || classType == char.class) {
                    type = RequestParamType.Type.CHAR;
                } else if (classType == Byte.class || classType == byte.class) {
                    type = RequestParamType.Type.BYTE;
                }else {
                    throw new RuntimeException(clazz.getSimpleName()+"."+method.getName()+" param["+i+"] is not support method ioc");
                }
            }
        }

    }


    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String[] getKeys() {
        return keys;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public boolean isVelocityTemplate() {
        return isVelocityTemplate;
    }

    public void setVelocityTemplate(boolean velocityTemplate) {
        isVelocityTemplate = velocityTemplate;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
