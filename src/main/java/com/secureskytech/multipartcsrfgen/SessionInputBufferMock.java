package com.secureskytech.multipartcsrfgen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.io.HttpTransportMetricsImpl;
import org.apache.http.impl.io.SessionInputBufferImpl;

/**
 * copied from HttpCore's test code : org.apache.http.impl.SessionInputBufferMock
 * 
 * @see https://hc.apache.org/httpcomponents-core-4.4.x/httpcore/xref-test/org/apache/http/impl/SessionInputBufferMock.html
 */
public class SessionInputBufferMock extends SessionInputBufferImpl {

    public static final int BUFFER_SIZE = 16;

    public SessionInputBufferMock(final String s, final Charset charset) {
        this(new ByteArrayInputStream(s.getBytes(charset)), MessageConstraints.DEFAULT, charset);
    }

    public SessionInputBufferMock(final byte[] bytes, final Charset charset) {
        this(new ByteArrayInputStream(bytes), null, charset);
    }

    public SessionInputBufferMock(final InputStream ins, final MessageConstraints constraints, final Charset charset) {
        super(
            new HttpTransportMetricsImpl(),
            BUFFER_SIZE,
            -1,
            constraints,
            Objects.nonNull(charset) ? charset.newDecoder() : null);
        bind(ins);
    }
}
