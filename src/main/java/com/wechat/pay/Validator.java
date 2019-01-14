package com.wechat.pay;

public interface Validator {
    String getField();

    boolean isRequired();
}
