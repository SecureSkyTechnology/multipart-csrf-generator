package com.secureskytech.multipartcsrfgen;

import java.util.Objects;

public class HexDumper {

    String prefix = "";

    public void setPrefix(String s) {
        this.prefix = s;
    }

    boolean toUpperCase = false;

    public void setToUpperCase(boolean b) {
        this.toUpperCase = b;
    }

    String separator = "";

    public void setSeparator(String s) {
        this.separator = s;
    }

    public String dump(final byte[] srcbytes) {
        if (Objects.isNull(srcbytes)) {
            return "";
        }
        if (srcbytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(srcbytes.length * 4);
        final int len = srcbytes.length;
        final int seplimit = len - 1;
        for (int i = 0; i < len; i++) {
            byte b = srcbytes[i];
            sb.append(prefix);
            if (toUpperCase) {
                sb.append(String.format("%02X", b));
            } else {
                sb.append(String.format("%02x", b));
            }
            if (i < seplimit) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}