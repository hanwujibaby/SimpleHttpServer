package com.wayleynam.http;

import com.wayleynam.utils.ServerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/***
 * 类似于SPringMVC的方案
 */
public final class ControllerMessageHandler extends HttpMessageHandler {

    private static final Log logger = LogFactory.getLog(ControllerMessageHandler.class);

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{(\\w+)\\}|\\*");

    private ClassPathXmlApplicationContext context;

    private Map<String, MethodHandler> mappingMethods = new HashMap<String, MethodHandler>();
    private Map<String, Template> templates = new HashMap<String, Template>();

    private List<MethodHandler> matcherLists = new ArrayList<MethodHandler>();

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
            methodHandler.setParameterTypes(clazz,method);
            if(methodHandler.)

        }


    }


    @Override
    public byte[] service(HttpRequest httpRequest, HttpResponse httpResponse) throws Throwable {
        return new byte[0];
    }
}
