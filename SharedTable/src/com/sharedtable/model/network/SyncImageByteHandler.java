package com.sharedtable.model.network;

public class SyncImageByteHandler {

    public SyncImageByteHandler(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getNextNBytes(int byteNumber) {
        byte[] res = new byte[byteNumber];
        if(byteNumber+actByteCount>bytes.length)
            throw new RuntimeException("getNextNBytes, index out of range");
        for(int i = actByteCount; i<byteNumber+actByteCount; i++) {
            res[i-actByteCount] = bytes[i];
            //System.out.println("res "+i+".: "+res[i-actByteCount]);
        }
        actByteCount += byteNumber;
        return res;
    }

    private byte[] bytes;
    private int actByteCount = 0;
}
