package com.wechat.pay;
public enum PayQueryField implements Validator {

        APP_ID("appid", true),
        MCH_ID("mch_id", true),
        NONCE_STR("nonce_str", true),
        SIGN("sign", true),
        OUT_TRADE_NO("out_trade_no", false),
        TRANSACTION_ID("transaction_id", false),;

private String field;
private boolean required;

        PayQueryField(String field, boolean required) {
        this.field = field;
        this.required = required;
        }

public String getField() {
        return field;
        }

public boolean isRequired() {
        return required;
        }
}
