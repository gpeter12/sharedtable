package com.sharedtable.model.network;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class EncodingUtils {

    private static final Charset utf8charset = Charset.forName("UTF-8");
    private static final Charset iso88591charset = Charset.forName("ISO-8859-1");

    public static String toISO8859_1(String text) {
        try {
            ByteBuffer inputBuffer = ByteBuffer.wrap(text.getBytes(utf8charset));
            CharBuffer data = utf8charset.decode(inputBuffer);
            ByteBuffer outputBuffer = iso88591charset.encode(data);
            byte[] outputData = outputBuffer.array();
            return new String(outputData, iso88591charset);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String toUTF8(String text) {
        try {
            ByteBuffer inputBuffer = ByteBuffer.wrap(text.getBytes(iso88591charset));
            CharBuffer data = iso88591charset.decode(inputBuffer);
            ByteBuffer outputBuffer = utf8charset.encode(data);
            byte[] outputData = outputBuffer.array();
            return new String(outputData, utf8charset);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}