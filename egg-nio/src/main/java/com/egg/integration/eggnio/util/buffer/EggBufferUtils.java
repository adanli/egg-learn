package com.egg.integration.eggnio.util.buffer;

public class EggBufferUtils {
    public static final String PACKAGE_HEADER = "LENGTH: ";

    public static String packageMsg(String msg) {
        StringBuffer sb = new StringBuffer(PACKAGE_HEADER);
        return sb.append(msg.length())
                .append("\r\n")
                .append(msg)
                .append("\r\n")
                .toString();
    }
}
