package com.sharedtable.controller;

public class BooleanFlag {
    public BooleanFlag(boolean b){
        flag = b;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    private boolean flag;
}
