package com.wayleynam.controller;

import com.wayleynam.constants.RequestParamType;
import com.wayleynam.http.HttpException;
import com.wayleynam.http.HttpRequest;
import com.wayleynam.http.HttpResponse;
import com.wayleynam.utils.HttpResponseStatus;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class MethodHandler {
    private final static Logger logger = LoggerFactory.getLogger(MethodHandler.class);
    private final Object object;
    private final Method method;
    private final boolean isResponseBody;
    private final boolean xssFilter;
    private RequestParamType[] requestParamTypes;
    private int parameterLength;
    private ObjectMapper objectMapper;
    private Pattern pattern;
    private boolean matcherHandler = false;
    private String[] keys;
    private boolean isVelocityTemplate;
    private String templateName;

    public MethodHandler(Object object, Method method) {
        this.object = object;
        this.method = method;
        this.isVelocityTemplate = method.isAnnotationPresent(RequestParam.VelocityTemplate.class);
        this.isResponseBody = method.isAnnotationPresent(ResponseBody.class);

        if (this.isResponseBody && this.isResponseBody) {
            throw new RuntimeException(object.getClass().getName() + " method " + method.getName() + " can not be annotation present both VelocityTemplate and ResponseBody");
        }
        XssFilter filter = method.getAnnotation(XssFilter.class);
        this.xssFilter = filter == null ? true : filter.value();
    }


    public void setPathPattern(Pattern pathPattern, String[] keys) {
        this.pattern = pathPattern;
        this.keys = keys;
        this.matcherHandler = true;
    }


    public boolean isMatcherHandler() {
        return matcherHandler;

    }

    public void setParameterTypes(Class<?> clazz, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        this.parameterLength = parameterTypes.length;
        this.requestParamTypes = new RequestParamType[this.parameterLength];
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
                } else {
                    throw new RuntimeException(clazz.getSimpleName() + "." + method.getName() + " param[" + i + "] is not support method ioc");
                }


                Annotation[] annotations = parameterAnnotations[i];
                RequestParam requestParam = null;
                PathVariable pathVariable = null;
                for (Annotation annotation : annotations) {
                    if (annotation instanceof RequestParam) {
                        requestParam = (RequestParam) annotation;
                        paramType.setName(requestParam.defaultValue());
                        if (!requestParam.defaultValue().equals(ValueConstants.DEFAULT_NONE)) {
                            paramType.setDefaultValue(requestParam.defaultValue());
                            paramType.setRequired(true);
                        } else {
                            paramType.setRequired(requestParam.required());
                        }

                    } else if (annotation instanceof PathVariable) {
                        pathVariable = (PathVariable) annotation;
                        paramType.setName(pathVariable.value());
                        paramType.setRequired(true);

                    }

                    if (requestParam == null && pathVariable == null) {
                        throw new RuntimeException(clazz.getSimpleName() + "." + method.getName() + " param[" + i + "] must be annotation present RequsetParam or PathVariable");
                    }
                }
                paramType.setType(type);
                this.requestParamTypes[i] = paramType;
            }
        }

    }

    public Object invoke(HttpRequest request, HttpResponse response) throws Throwable {
        try {
            if (this.parameterLength > 0) {
                Object[] params = new Object[this.parameterLength];
                for (int i = 0; i < this.parameterLength; i++) {
                    RequestParamType requestParamType = requestParamTypes[i];
                    RequestParamType.Type type = requestParamType.getType();
                    if (type == RequestParamType.Type.HTTP_REQUEST) {
                        params[i] = request;
                    } else if (type == RequestParamType.Type.HTTP_RESPONSE) {
                        params[i] = response;
                    } else {
                        String name = requestParamType.getName();
                        String value = request.getParameter(name);
                        if (value == null) {
                            value = requestParamType.getDefaultValue();
                        }
                        if (requestParamType.isRequired() && value == null) {
                            logger.warn("bad request http param[{}]", name);
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST);
                        }

                        if (value == null) {
                            continue;
                        }

                        switch (type) {
                            case STRING:
                                params[i] = value;
                                break;
                            case LIST:
                                params[i] = objectMapper.readValue(value, List.class);
                                break;
                            case SET:
                                params[i] = objectMapper.readValue(value, Set.class);
                                break;
                            case MAP:
                                params[i] = objectMapper.readValue(value, Map.class);
                                break;
                            case ARRARY:
                                params[i] = objectMapper.readValue(value, List.class).toArray();
                                break;
                            case BOOLEAN:
                                params[i] = Boolean.parseBoolean(value);
                                break;
                            case SHORT:
                                params[i] = Short.parseShort(value);
                                break;
                            case INTEGER:
                                params[i] = Integer.parseInt(value);
                                break;
                            case LONG:
                                params[i] = Long.parseLong(value);
                                break;
                            case FLOAT:
                                params[i] = Float.parseFloat(value);
                                break;
                            case DOUBLE:
                                params[i] = Double.parseDouble(value);
                                break;
                            case CHAR:
                                params[i] = value.charAt(0);
                                break;
                            case BYTE:
                                params[i] = value.getBytes()[0];
                                break;
                            default: {

                            }
                        }
                    }

                }
                return method.invoke(object, params);

            } else {
                return method.invoke(object);
            }

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

    }

    public boolean isResponseBody() {
        return isResponseBody;
    }

    public boolean isXssFilter() {
        return xssFilter;
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

    public Object getObject() {
        return object;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ResponseBody {

    }
}
