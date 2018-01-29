package com.secureskytech.multipartcsrfgen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.io.SessionInputBuffer;

public class AppSpecUtils {

    public static byte[] fromSessionInputBuffer(SessionInputBuffer inbuf) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        try {
            while (true) {
                final int i = inbuf.read();
                if (i == -1) {
                    break;
                }
                os.write(i);
            }
        } catch (IOException ignore) {
        }
        return os.toByteArray();
    }
}
