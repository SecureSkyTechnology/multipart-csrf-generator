package com.secureskytech.multipartcsrfgen;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.secureskytech.multipartcsrfgen.HttpMultipartRequest.MultipartParameter;
import com.secureskytech.multipartcsrfgen.HttpMultipartRequest.UrlEncodedParameter;

public class TestHttpMultipartRequest {

    @Test
    public void testParseMultipart() throws Exception {
        final byte[] reqdata0 =
            StreamUtils
                .copyToByteArray((new ClassPathResource("static/sample-multipart-request-1.bin")).getInputStream());
        HttpMultipartRequest req0 = HttpMultipartRequest.parse(reqdata0, StandardCharsets.UTF_8);

        final RequestLine reqline = req0.requestLine;
        assertNotNull(reqline);
        assertEquals("POST", reqline.getMethod());
        assertEquals("/dummy", reqline.getUri());
        assertEquals(HttpVersion.HTTP_1_1, reqline.getProtocolVersion());
        final Header[] headers = req0.headers;
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

        assertEquals(0, req0.formParameters.size());
        assertEquals(6, req0.multipartParameters.size());

        MultipartParameter mp = req0.multipartParameters.get(0);
        assertEquals("<>%22&'日本語キー", mp.name);
        assertFalse(mp.isFile);
        assertEquals("<>\"&'日本語文字列1", mp.printableValue);
        mp = req0.multipartParameters.get(1);
        assertEquals("<>%22&'日本語キー", mp.name);
        assertFalse(mp.isFile);
        assertEquals("<>\"&'日本語文字列2", mp.printableValue);
        mp = req0.multipartParameters.get(2);
        assertEquals("fileA", mp.name);
        assertTrue(mp.isFile);
        assertEquals("256_0to255.bin", mp.fileName);
        assertEquals("application/octet-stream", mp.contentType);
        assertArrayEquals(
            StreamUtils.copyToByteArray((new ClassPathResource("static/256_0to255.bin")).getInputStream()),
            mp.fileBytes);
        mp = req0.multipartParameters.get(3);
        assertEquals("<>%22&'ファイル%0D%0AB%0d%0a", mp.name);
        assertTrue(mp.isFile);
        assertEquals("512_0to255.bin", mp.fileName);
        assertEquals("application/octet-stream", mp.contentType);
        assertArrayEquals(
            StreamUtils.copyToByteArray((new ClassPathResource("static/512_0to255.bin")).getInputStream()),
            mp.fileBytes);
        mp = req0.multipartParameters.get(4);
        assertEquals("ascii%0D%0Apname2[]%0d%0a", mp.name);
        assertFalse(mp.isFile);
        assertEquals("ascii\r\npvalue2[]\r\n%0d%0a", mp.printableValue);
        mp = req0.multipartParameters.get(5);
        assertEquals("ascii pname1%0d%0a", mp.name);
        assertFalse(mp.isFile);
        assertEquals("ascii pvalue1%0d%0a", mp.printableValue);
    }

    @Test
    public void testParseFormPost() throws Exception {
        final byte[] reqdata0 =
            StreamUtils
                .copyToByteArray((new ClassPathResource("static/sample-formpost-request-1.bin")).getInputStream());
        HttpMultipartRequest req0 = HttpMultipartRequest.parse(reqdata0, StandardCharsets.UTF_8);

        assertEquals(4, req0.formParameters.size());
        assertEquals(0, req0.multipartParameters.size());

        UrlEncodedParameter formp = req0.formParameters.get(0);
        assertEquals("ascii pname1%0d%0a", formp.name);
        assertEquals("ascii pvalue1%0d%0a", formp.value);
        formp = req0.formParameters.get(1);
        assertEquals("ascii\r\npname2[]%0d%0a", formp.name);
        assertEquals("ascii\r\npvalue2[]\r\n%0d%0a", formp.value);
        formp = req0.formParameters.get(2);
        assertEquals("<>\"&'日本語キー", formp.name);
        assertEquals("<>\"&'日本語文字列1", formp.value);
        formp = req0.formParameters.get(3);
        assertEquals("<>\"&'日本語キー", formp.name);
        assertEquals("<>\"&'日本語文字列2", formp.value);
    }

    @Test
    public void testEscapeToJavaScriptString() {
        assertEquals("", HttpMultipartRequest.escapeToJavaScriptString(null));
        assertEquals("", HttpMultipartRequest.escapeToJavaScriptString(""));
        assertEquals("abcこんにちは", HttpMultipartRequest.escapeToJavaScriptString("abcこんにちは"));
        assertEquals(
            "\\x08\\x09\\x0a\\x0d\\x0c\\x3c\\x3e\\x22\\x27\\\\\\/abcこんにちは",
            HttpMultipartRequest.escapeToJavaScriptString("\b\t\n\r\f<>\"'\\/abcこんにちは"));
    }

}
