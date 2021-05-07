package com.wayleynam.http;

import com.wayleynam.controller.MethodHandler;
import com.wayleynam.utils.HttpResponseStatus;
import com.wayleynam.utils.ServerConfig;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;


/***
 * 类似于SPringMVC的方案
 */
public final class ControllerMessageHandler extends HttpMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControllerMessageHandler.class);

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{(\\w+)\\}|\\*");

    private ClassPathXmlApplicationContext context;

    private Map<String, MethodHandler> mappingMethods = new HashMap<String, MethodHandler>();

    private Map<String, Template> templates = new HashMap<String, Template>();

    private List<MethodHandler> matcherMethods = new ArrayList<MethodHandler>();

    private ObjectMapper deafultObjectMapper;

    private ObjectMapper xssFilterObjectMapper;

    private Charset charset;

    private String jsonContentType;

    private GlobalInterceptor globalInterceptor;

    private ExceptionHandler exceptionHandler;


    @Override
    public void initialize(ServerConfig serverConfig) {
        String charset = serverConfig.getString("server.http.charset", "UTF-8");
        this.charset = Charset.forName(charset);
        this.jsonContentType = "text/json;chartset=" + charset;
        deafultObjectMapper = new ObjectMapper();
        xssFilterObjectMapper = new ObjectMapper();
        CustomSerializerFactory customSerializerFactory = new CustomSerializerFactory();
        xssFilterObjectMapper.setSerializerFactory(customSerializerFactory);
        context = new ClassPathXmlApplicationContext(new String[]{"classpath:application.xml"});
        try {
            this.globalInterceptor = context.getBean(GlobalInterceptor.class);
            this.exceptionHandler = context.getBean(ExceptionHandler.class);
        } catch (Exception e) {
            logger.error("can't get bean[GlobalInterceptor] " + e.getMessage());
        }
        Map<String, Object> beans = context.getBeansWithAnnotation(Controller.class);
        logger.info("beans " + beans.size());

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            loadObject(entry.getValue());
        }


    }


    @Override
    public void destroy() {
        context.destroy();
    }


    private void loadObject(Object object) {
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                String[] values = mapping.value();
                for (String value : values) {
                    loadMethod(object, clazz, method, value);
                }
            }

        }

    }


    private void loadMethod(Object object, Class<?> clazz, Method method, String mapping) {
        method.setAccessible(true);
        MethodHandler methodHandler = new MethodHandler(object, method);
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(mapping);
        String matched = null;
        List<String> pathVariables = new ArrayList<String>();
        while (matcher.find()) {
            String quot = matcher.group(0);
            if (quot.equals("*")) {
                matched = matcher.replaceFirst(".+");
            } else {
                String group = matcher.group(1);
                pathVariables.add(group);
                matched = matcher.replaceFirst("(\\\\w+)");

            }

            if (matched != null) {
                matched = "^" + matched + "$";
                methodHandler.setPathPattern(Pattern.compile(matched), pathVariables.toArray(new String[0]));
            }
            methodHandler.setObjectMapper(deafultObjectMapper);
            methodHandler.setParameterTypes(clazz, method);
            if (methodHandler.isMatcherHandler()) {
                matcherMethods.add(methodHandler);
            } else {
                mappingMethods.put(mapping, methodHandler);
            }

            if (methodHandler.isVelocityTemplate()) {
                String name = methodHandler.getTemplateName();
                Template template = Velocity.getTemplate(methodHandler.getTemplateName());
                templates.put(name, template);
            }

            logger.info("mapped method({},{}) " + mapping, clazz.getSimpleName(), method.getName());

        }


    }


    @Override
    public byte[] service(HttpRequest request, HttpResponse response) throws Throwable {
        try {
            String queryString = request.getQueryString();
            MethodHandler hanlder = mappingMethods.get(queryString);
            if (hanlder == null) {
                for (MethodHandler mm : matcherMethods) {
                    Matcher m = mm.getPattern().matcher(queryString);
                    if (m.find()) {
                        int index = 1;
                        DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) request;
                        for (String key : mm.getKeys()) {
                            String value = m.group(index);
                            defaultHttpRequest.addParameter(key, value);
                            index++;
                        }
                        hanlder = mm;
                        break;
                    }
                }
            }

            Object o = hanlder != null ? hanlder.getObject() : null;
            if (globalInterceptor != null && globalInterceptor.beforeHandler(request, response, o) == false) {
                return response.getContent();
            }

            if (hanlder == null) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND);
            }

            Object result = hanlder.invoke(request, response);
            byte[] bytes = null;
            if (hanlder.isVelocityTemplate()) {
                Map<?, ?> params = null;
                if (result instanceof Map) {
                    params = (Map<?, ?>) result;
                }

                VelocityContext context = new VelocityContext(params);
                Template template = templates.get(hanlder.getTemplateName());
                if (template == null) {
                    throw new RuntimeException("can not find the velocity template:" + hanlder.getTemplateName());
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
                OutputStream stream = os;
                String acceptEncoding = request.getHeader("Accept-Encoding");
                String[] arr = StringUtils.split(acceptEncoding, ',');
                String encoding = null;
                int type = 0;
                for (String a : arr) {
                    if ("gzip".equals(a) && encoding == null) {
                        type = 1;
                    } else if ("deflate".equals(a)) {
                        type = 2;
                        break;
                    }
                }

                if (type == 2) {
                    stream = new DeflaterOutputStream(stream);
                    response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, "defalte");
                } else if (type == 1) {
                    stream = new GZIPOutputStream(stream);
                    response.addHeader(HttpHeaders.Names.CONTENT_ENCODING, "gzip");
                }

                Writer writer = new PrintWriter(stream);
                template.merge(context, writer);
                writer.flush();
                writer.close();
                bytes = os.toByteArray();
                stream.close();
            } else if (result != null) {
                if (hanlder.isResponseBody()) {
                    response.setHeader(HttpHeaders.Names.CONTENT_TYPE, this.jsonContentType);
                    if (hanlder.isXssFilter()) {
                        bytes = xssFilterObjectMapper.writeValueAsBytes(result);
                    } else {
                        bytes = deafultObjectMapper.writeValueAsBytes(result);
                    }

                }
            } else {
                if (result instanceof byte[]) {
                    bytes = (byte[]) result;
                } else {
                    String str = result.toString();
                    if (hanlder.isXssFilter()) {
                        bytes = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(str).getBytes(charset);
                    } else {
                        bytes = str.getBytes(charset);
                    }
                }
            }

            if (globalInterceptor != null) {
                globalInterceptor.afterHandler(request, response, hanlder);
            }
            return bytes;
        } catch (Throwable t) {
            if (this.exceptionHandler != null) {
                return this.exceptionHandler.resolveException(request, response, t);
            } else {
                throw t;
            }

        }
    }
}
