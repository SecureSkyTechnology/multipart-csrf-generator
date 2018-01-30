package com.secureskytech.multipartcsrfgen;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.io.SessionInputBuffer;

import ext.org.apache.commons.fileupload.MockHttpServletRequest;
import lombok.Data;

@Data
public class HttpMultipartRequest {
    public final RequestLine requestLine;
    public final Header[] headers;
    private final Charset charset;
    public final List<MultipartParameter> multipartParameters;

    private HttpMultipartRequest(final RequestLine requestLine, final Header[] headers,
            final List<MultipartParameter> multipartParameters, final Charset charset) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.multipartParameters = multipartParameters;
        this.charset = charset;
    }

    public static HttpMultipartRequest parse(final byte[] httpRequest, final Charset charset) throws Exception {
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(httpRequest, StandardCharsets.ISO_8859_1);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        final Header[] headers = hreq0.getAllHeaders();
        String contentType = "";
        for (Header h : headers) {
            if (h.getName().equals("Content-Type")) {
                contentType = h.getValue();
            }
        }
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        HttpServletRequest servletRequest = new MockHttpServletRequest(remains, contentType, StandardCharsets.UTF_8);
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        Map<String, List<FileItem>> params = upload.parseParameterMap(servletRequest);
        final List<MultipartParameter> multipartParameters = new ArrayList<>();
        for (Entry<String, List<FileItem>> e : params.entrySet()) {
            List<FileItem> fis = e.getValue();
            for (int i = 0; i < fis.size(); i++) {
                FileItem fi = fis.get(i);
                final String name = fi.getFieldName();
                if (fi.isFormField()) {
                    final String pv = new String(fi.get(), charset);
                    multipartParameters.add(new MultipartParameter(name, pv));
                } else {
                    final String fileName = new String(fi.getName().getBytes(StandardCharsets.ISO_8859_1), charset);
                    multipartParameters.add(new MultipartParameter(name, fileName, fi.getContentType(), fi.get()));
                }
            }
        }
        return new HttpMultipartRequest(reqline, headers, multipartParameters, charset);
    }

    public static class MultipartParameter {
        public final String name;
        public final String printableValue;
        public final boolean isFile;
        public final String fileName;
        public final String contentType;
        public final byte[] fileBytes;

        public MultipartParameter(final String name, final String printableValue) {
            this.name = name;
            this.printableValue = printableValue;
            this.isFile = false;
            this.fileName = "";
            this.contentType = "";
            this.fileBytes = new byte[] {};
        }

        public MultipartParameter(final String name, final String fileName, final String contentType,
                final byte[] fileBytes) {
            this.name = name;
            this.printableValue = "";
            this.isFile = true;
            this.fileName = fileName;
            this.contentType = contentType;
            this.fileBytes = fileBytes;
        }
    }

    public static String jsescape(String s) {
        if (Objects.isNull(s)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\b':
                sb.append("\\x08");
                break;
            case '\t':
                sb.append("\\x09");
                break;
            case '\n':
                sb.append("\\x0a");
                break;
            case '\r':
                sb.append("\\x0d");
                break;
            case '\f':
                sb.append("\\x0c");
                break;
            case '<':
                sb.append("\\x3c");
                break;
            case '>':
                sb.append("\\x3e");
                break;
            case '"':
                sb.append("\\x22");
                break;
            case '\'':
                sb.append("\\x27");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '/':
                sb.append("\\/");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String createCSRFhtml(final String uri, final boolean enableAutoAccess, final boolean withCredentials,
            final boolean copyAuthorizationHeader) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html>\r\n");
        sb.append("<head><meta charset=\"" + charset.name() + "\"></head>\r\n");
        sb.append("<body><script>\r\n");
        sb.append("function docsrf() {\r\n");
        sb.append("var formData = new FormData();\r\n");
        for (MultipartParameter mp : this.multipartParameters) {
            final String pname = jsescape(mp.name);
            if (mp.isFile) {
                final HexDumper hd = new HexDumper();
                hd.setPrefix("0x");
                hd.setSeparator(",");
                hd.setToUpperCase(false);
                final String uint8arr = hd.dump(mp.fileBytes);
                sb.append(
                    "formData.append('"
                        + pname
                        + "', new File([new Blob([new Uint8Array(["
                        + uint8arr
                        + "])], {type: 'application/octet-stream'})], '"
                        + jsescape(mp.fileName)
                        + "', {type: '"
                        + mp.contentType
                        + "'}));\r\n");
            } else {
                sb.append("formData.append('" + pname + "', '" + jsescape(mp.printableValue) + "');\r\n");
            }
        }
        sb.append("var xhr = new XMLHttpRequest();\r\n");
        sb.append("xhr.open('" + this.requestLine.getMethod() + "', '" + jsescape(uri) + "');\r\n");
        for (Header h : this.headers) {
            final String n = h.getName();
            if (copyAuthorizationHeader && n.equals("Authorization")) {
                sb.append("xhr.setRequestHeader('" + h.getName() + "', '" + h.getValue() + "');\r\n");
            }
        }
        if (withCredentials) {
            sb.append("xhr.withCredentials = true;\r\n");
        }
        sb.append("xhr.send(formData);\r\n");
        sb.append("}\r\n");
        if (enableAutoAccess) {
            sb.append("docsrf();\r\n");
        }
        sb.append("</script>\r\n");
        sb.append("<input type=\"submit\" onclick=\"docsrf(); return false;\">\r\n");
        sb.append("</body></html>");
        return sb.toString();
    }
}
