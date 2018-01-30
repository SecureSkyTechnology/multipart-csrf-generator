package com.secureskytech.multipartcsrfgen;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public class CsrfHistory {
    
    @Data
    public static class CsrfItem {
        public final String token;
        public final String url;
        public final Charset charset;
        public final HttpMultipartRequest mpreq;
        public final String csrfHtml;
        public final long timestamp;

        public CsrfItem(final String url, final Charset charset, final HttpMultipartRequest mpreq,
                final String csrfHtml) throws NoSuchAlgorithmException {
            final MessageDigest md = MessageDigest.getInstance("SHA-1");
            final String tokensrc = url + Long.toHexString(System.nanoTime());
            final byte[] digest = md.digest(tokensrc.getBytes(StandardCharsets.UTF_8));
            final HexDumper hd = new HexDumper();
            hd.setPrefix("");
            hd.setSeparator("");
            hd.setToUpperCase(false);
            this.token = hd.dump(digest);
            this.url = url;
            this.charset = charset;
            this.mpreq = mpreq;
            this.csrfHtml = csrfHtml;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public final List<CsrfItem> items = new ArrayList<>();
}
