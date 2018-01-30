package com.secureskytech.multipartcsrfgen;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.impl.io.DefaultHttpRequestParser;
import org.apache.http.io.SessionInputBuffer;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import ext.org.apache.commons.fileupload.MockHttpServletRequest;

public class TestHttpRequestParserDemo {

    @Test
    public void testParseGetRequest() throws Exception {
        final String rstr0 =
            "GET /こんにちは HTTP/1.1\r\nHost: localhost:8080\r\nUser-Agent: abcdefg\r\nCookie: c1=cv1;c2=cv2\r\n\r\nabc";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(rstr0, StandardCharsets.UTF_8);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        assertNotNull(reqline);
        assertEquals("GET", reqline.getMethod());
        assertEquals("/こんにちは", reqline.getUri());
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = hreq0.getAllHeaders();
        assertEquals(3, headers.length);
        assertEquals("Host", headers[0].getName());
        assertEquals("localhost:8080", headers[0].getValue());
        assertEquals("User-Agent", headers[1].getName());
        assertEquals("abcdefg", headers[1].getValue());
        assertEquals("Cookie", headers[2].getName());
        assertEquals("c1=cv1;c2=cv2", headers[2].getValue());
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        assertArrayEquals(new byte[] { 0x61, 0x62, 0x63 }, remains);
    }

    @Test
    public void testParsePostRequest() throws Exception {
        final String rstr0 =
            "POST /こんにちは HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "User-Agent: abcdefg\r\n"
                + "Content-Length: 5\r\n"
                + "\r\n"
                + "abcd";
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(rstr0, StandardCharsets.UTF_8);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        assertNotNull(reqline);
        assertEquals("POST", reqline.getMethod());
        assertEquals("/こんにちは", reqline.getUri());
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = hreq0.getAllHeaders();
        assertEquals(3, headers.length);
        assertEquals("Host", headers[0].getName());
        assertEquals("localhost:8080", headers[0].getValue());
        assertEquals("User-Agent", headers[1].getName());
        assertEquals("abcdefg", headers[1].getValue());
        assertEquals("Content-Length", headers[2].getName());
        assertEquals("5", headers[2].getValue());
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        assertArrayEquals(new byte[] { 0x61, 0x62, 0x63, 0x64 }, remains);
    }

    @Test
    public void testParsePostRequest2() throws Exception {
        final String rstr0 = "POST /こんにちは HTTP/1.1\r\nHost: localhost:8080\r\nUser-Agent: abcdefg\r\n\r\nあいうえお";
        final byte[] rbytes0 = rstr0.getBytes(StandardCharsets.UTF_8);
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(rbytes0, StandardCharsets.UTF_8);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        assertNotNull(reqline);
        assertEquals("POST", reqline.getMethod());
        assertEquals("/こんにちは", reqline.getUri());
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = hreq0.getAllHeaders();
        assertEquals(2, headers.length);
        assertEquals("Host", headers[0].getName());
        assertEquals("localhost:8080", headers[0].getValue());
        assertEquals("User-Agent", headers[1].getName());
        assertEquals("abcdefg", headers[1].getValue());
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        assertArrayEquals("あいうえお".getBytes(StandardCharsets.UTF_8), remains);
    }

    @Test
    public void testParsePostRequest3() throws Exception {
        final String rstr0 = "POST /こんにちは HTTP/1.1\r\nHost: localhost:8080\r\nUser-Agent: abcdefg\r\n\r\nあいうえお";
        final byte[] rbytes0 = rstr0.getBytes(StandardCharsets.UTF_8);
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(rbytes0, StandardCharsets.ISO_8859_1);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        assertNotNull(reqline);
        assertEquals("POST", reqline.getMethod());
        assertEquals(
            "/こんにちは",
            new String(reqline.getUri().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = hreq0.getAllHeaders();
        assertEquals(2, headers.length);
        assertEquals("Host", headers[0].getName());
        assertEquals("localhost:8080", headers[0].getValue());
        assertEquals("User-Agent", headers[1].getName());
        assertEquals("abcdefg", headers[1].getValue());
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        assertArrayEquals("あいうえお".getBytes(StandardCharsets.UTF_8), remains);
    }

    @Test
    public void testParseMultipartRequest() throws Exception {
        final byte[] req0 =
            StreamUtils
                .copyToByteArray((new ClassPathResource("static/sample-multipart-request-1.bin")).getInputStream());
        final SessionInputBuffer inbuffer = new SessionInputBufferMock(req0, StandardCharsets.ISO_8859_1);
        final DefaultHttpRequestParser parser = new DefaultHttpRequestParser(inbuffer);
        final HttpRequest hreq0 = parser.parse();

        final RequestLine reqline = hreq0.getRequestLine();
        assertNotNull(reqline);
        assertEquals("POST", reqline.getMethod());
        assertEquals("/dummy", reqline.getUri());
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = hreq0.getAllHeaders();
        assertEquals(13, headers.length);
        assertEquals("Host", headers[0].getName());
        assertEquals("localhost:9002", headers[0].getValue());
        assertEquals("Content-Length", headers[1].getName());
        assertEquals("1683", headers[1].getValue());
        assertEquals("Cache-Control", headers[2].getName());
        assertEquals("max-age=0", headers[2].getValue());
        assertEquals("Origin", headers[3].getName());
        assertEquals("http://localhost:9002", headers[3].getValue());
        assertEquals("Upgrade-Insecure-Requests", headers[4].getName());
        assertEquals("1", headers[4].getValue());
        assertEquals("Content-Type", headers[5].getName());
        assertEquals("multipart/form-data; boundary=----WebKitFormBoundaryQMdpaBCjQndwSnvO", headers[5].getValue());
        final String contentType = headers[5].getValue();
        assertEquals("User-Agent", headers[6].getName());
        assertEquals(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36",
            headers[6].getValue());
        assertEquals("Accept", headers[7].getName());
        assertEquals(
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            headers[7].getValue());
        assertEquals("Referer", headers[8].getName());
        assertEquals("http://localhost:9002/", headers[8].getValue());
        assertEquals("Accept-Encoding", headers[9].getName());
        assertEquals("gzip, deflate", headers[9].getValue());
        assertEquals("Accept-Language", headers[10].getName());
        assertEquals("ja,en-US;q=0.9,en;q=0.8", headers[10].getValue());
        assertEquals("Cookie", headers[11].getName());
        assertEquals(
            "_ga=GA1.1.394581352.1511848536; JSESSIONID=8B51DE5FD4C803610312C9720A23402C",
            headers[11].getValue());
        assertEquals("Connection", headers[12].getName());
        assertEquals("close", headers[12].getValue());

        final byte[] reqbody0 =
            StreamUtils.copyToByteArray(
                (new ClassPathResource("static/sample-multipart-request-1-body.bin")).getInputStream());
        final byte[] remains = AppSpecUtils.fromSessionInputBuffer(inbuffer);
        assertArrayEquals(reqbody0, remains);
        HttpServletRequest servletRequest = new MockHttpServletRequest(remains, contentType, StandardCharsets.UTF_8);
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        Map<String, List<FileItem>> params = upload.parseParameterMap(servletRequest);
        for (Entry<String, List<FileItem>> e : params.entrySet()) {
            System.out.println("debug: key=[" + e.getKey() + "]");
            List<FileItem> fis = e.getValue();
            for (int i = 0; i < fis.size(); i++) {
                FileItem fi = fis.get(i);
                System.out.println("[" + e.getKey() + "][" + i + "].isFormField()=" + fi.isFormField());
                System.out.println("[" + e.getKey() + "][" + i + "].getFieldName()=" + fi.getFieldName());
                System.out.println("[" + e.getKey() + "][" + i + "].getName()=" + fi.getName());
                System.out.println("[" + e.getKey() + "][" + i + "].getContentType()=" + fi.getContentType());
                System.out.println("[" + e.getKey() + "][" + i + "].getSize()=" + fi.getSize());
            }
        }
        List<FileItem> fis = params.get("<>%22&'日本語キー");
        assertEquals(2, fis.size());
        FileItem fi = fis.get(0);
        assertTrue(fi.isFormField());
        assertEquals("<>%22&'日本語キー", fi.getFieldName());
        assertEquals(24, fi.getSize());
        byte[] fiv = fi.get();
        assertEquals("<>\"&'日本語文字列1", new String(fiv, StandardCharsets.UTF_8));
        fi = fis.get(1);
        assertTrue(fi.isFormField());
        assertEquals("<>%22&'日本語キー", fi.getFieldName());
        assertEquals(24, fi.getSize());
        fiv = fi.get();
        assertEquals("<>\"&'日本語文字列2", new String(fiv, StandardCharsets.UTF_8));

        fis = params.get("fileA");
        assertEquals(1, fis.size());
        fi = fis.get(0);
        assertFalse(fi.isFormField());
        assertEquals("fileA", fi.getFieldName());
        assertEquals("256_0to255.bin", fi.getName());
        assertEquals("application/octet-stream", fi.getContentType());
        assertEquals(256, fi.getSize());
        final byte[] bin256 =
            StreamUtils.copyToByteArray((new ClassPathResource("static/256_0to255.bin")).getInputStream());
        assertArrayEquals(bin256, fi.get());

        fis = params.get("<>%22&'ファイル%0D%0AB%0d%0a");
        assertEquals(1, fis.size());
        fi = fis.get(0);
        assertFalse(fi.isFormField());
        assertEquals("<>%22&'ファイル%0D%0AB%0d%0a", fi.getFieldName());
        assertEquals("512_0to255.bin", fi.getName());
        assertEquals("application/octet-stream", fi.getContentType());
        assertEquals(512, fi.getSize());
        final byte[] bin512 =
            StreamUtils.copyToByteArray((new ClassPathResource("static/512_0to255.bin")).getInputStream());
        assertArrayEquals(bin512, fi.get());

        fis = params.get("ascii%0D%0Apname2[]%0d%0a");
        assertEquals(1, fis.size());
        fi = fis.get(0);
        assertTrue(fi.isFormField());
        assertEquals("ascii%0D%0Apname2[]%0d%0a", fi.getFieldName());
        assertEquals(24, fi.getSize());
        fiv = fi.get();
        assertEquals("ascii\r\npvalue2[]\r\n%0d%0a", new String(fiv, StandardCharsets.UTF_8));

        fis = params.get("ascii pname1%0d%0a");
        assertEquals(1, fis.size());
        fi = fis.get(0);
        assertTrue(fi.isFormField());
        assertEquals("ascii pname1%0d%0a", fi.getFieldName());
        assertEquals(19, fi.getSize());
        fiv = fi.get();
        assertEquals("ascii pvalue1%0d%0a", new String(fiv, StandardCharsets.UTF_8));
    }
}
