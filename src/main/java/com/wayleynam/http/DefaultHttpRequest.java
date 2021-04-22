package com.wayleynam.http;

import com.wayleynam.constants.HttpMethod;
import com.wayleynam.constants.HttpVersion;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * HttpRequest实现类，用于保存Http请求栈上的数据
 */
public class DefaultHttpRequest extends DefaultHttpMessage implements HttpRequest {
    private final static Logger logger = LoggerFactory.getLogger(DefaultHttpMessage.class);
    private final HttpMethod method;
    private final String uri;
    private String queryString;
    private Map<String, String> parameters;

    private Map<String, FileItem> files;
    private byte[] content;
    private int contentLength;
    private int contentIndex;
    private InputStream inputStream;
    private boolean dynamic;
    private String clientIp;
    private String characterEncoding;
    private String contentType;
    private long start = 0;

    private int initialType = 0;

    private File bufferFile;
    private BufferedOutputStream bufferedFileOutputStream;

    void createContentBuffer(int contentLength, String contentType) throws IOException {
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentIndex = 0;
        if (contentType != null) {
            String lower = this.contentType.toLowerCase();
            if (lower.startsWith(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)) {
                initialType = 1;
                content = new byte[contentLength];
                return;
            } else if (lower.startsWith("mmultipart/")) {
                initialType = 2;
            }
        }

        bufferFile = File.createTempFile("request_", ".tmp");
        bufferFile.deleteOnExit();
        bufferedFileOutputStream = new BufferedOutputStream(new FileOutputStream(bufferFile));
    }


    boolean readContentBuffer(ByteBuffer buffer) throws Exception {
        int remain = buffer.remaining();
        boolean finished = false;
        if (initialType == 1) {
            for (; this.contentIndex < this.contentLength && remain > 0; this.contentIndex++, remain--) {
                //buffer.get() 相对读，读的时候将position+1，为下一次读做准备
                content[this.contentIndex] = buffer.get();
            }
        } else {
            for (; this.contentIndex < this.contentLength && remain > 0; this.contentIndex++, remain--) {
                this.bufferedFileOutputStream.write(buffer.get());
            }
        }
        finished = this.contentIndex == this.contentLength;
        return finished;

    }

    void initialize() throws Exception {
        if (initialType == 1) {
            //解析application/x-www-form-urlencoded 类似的数据
            decodeContentAsURL(new String(this.content), this.characterEncoding);
            this.content = null;
        } else {
            if (this.bufferedFileOutputStream != null) {
                this.bufferedFileOutputStream.close();
                this.bufferedFileOutputStream = null;
            }
            if (this.bufferFile != null) {
                this.inputStream = new FileInputStream(this.bufferFile);
            }
        }

        if (initialType == 2 && contentType != null && contentType.startsWith("multipart/")) {
            FileUpload fileUpload = new FileUpload(new DiskFileItemFactory());
            List<FileItem> list = fileUpload.parseRequest(new HttpRequestContent(this));
            for (FileItem item : list) {
                if (item.isFormField()) {
                    String value;
                    try {
                        value = item.getString(characterEncoding);
                    } catch (UnsupportedEncodingException e) {
                        logger.warn("connot decode multipart item:" + item.getFieldName() + " witch encoding " + characterEncoding + " :using platform default");
                        value = item.getString();
                    }
                    addParameter(item.getFieldName(), value);
                } else {
                    if (files == null) {
                        files = new HashMap<String, FileItem>();
                    }
                    files.put(item.getFieldName(), item);
                }

            }

        }

    }


    public void destroy() {
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            } catch (IOException e) {
            }
        }

        if (this.bufferFile != null) {
            this.bufferFile.delete();
        }

        this.parameters = null;
        if (files != null) {
            for (FileItem item : files.values()) {
                item.delete();
            }
            files = null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("http request process delay:{}ms", (System.currentTimeMillis() - start));
        }
    }


    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public DefaultHttpRequest(HttpVersion version, HttpMethod method, String uri) {
        super(version);
        this.method = method;
        this.uri = uri;
        if (logger.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
    }

    void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /***
     * 将post接口的APPLICATION_X_WWW_FORM_URLENCODED 的content-type post的数据将按照title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3 的方式向后端传递数据
     * @param params
     * @param charset
     */
    void decodeContentAsURL(String params, String charset) {
        int start = 0;
        int length = params.length();

        for (; start < length; start++) {
            if ('?' != params.charAt(start)) {
                break;
            }
        }

        int left = start;
        int middle = 0;
        for (; start < length; start++) {
            if ('=' == params.charAt(start)) {
                middle = start;
                for (; start < length; start++) {
                    char c = params.charAt(start);
                    if ('&' == c) {
                        String key = params.substring(left, middle);
                        String value = params.substring(left + 1, start);
                        try {
                            addParameter(URLDecoder.decode(key, charset), URLDecoder.decode(value, charset));
                        } catch (UnsupportedEncodingException e) {

                        }

                        for (; start < left; start++) {
                            if ('&' != params.charAt(start)) {
                                break;
                            }
                        }
                        left = start;
                        break;
                    }

                }
            }
        }


        if (middle > start) {
            String key = params.substring(left, middle);
            String value = params.substring(middle + 1);
            try {
                addParameter(URLDecoder.decode(key, charset), URLDecoder.decode(value, charset));
            } catch (UnsupportedEncodingException e) {

            }
        }

    }


    void addParameter(String key, String value) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.put(key, value);
    }


    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Map<String, String> getParametersMap() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public String getClientIp() {
        return null;
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getCharaterEncoding() {
        return null;
    }

    @Override
    public FileItem getFile(String name) {
        return null;
    }
}
