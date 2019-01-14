package com.wechat.pay;

public class weixinData {

    public  static String APPID = "wx225ad303292d69f1";

    public  static String MCHID="1273041701";

    public static String APPSECRET ="992a7fe9ac5c8196fa5d40c35412a345";

    public static final String KEY="i2lv0xing18guan09jia2xiu7gai12wc";

    public static final String CHANNEL = "JSAPI";

    public static final String NOTIFY_URL = "";//TODO
    /**微信回调url**/
    public static final String NOTIFY_URL_PAY = "http://client.ilxgj.com/getPayResult";//TODO
    /**微信订单查询**/
    public static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    /**微信统一下单**/
    public static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    /**微信退款申请**/
    public static final String REFUND_URL="https://api.mch.weixin.qq.com/secapi/pay/refund";
    /**微信关闭订单**/
    public static final String CLOSER_ORDER_URL="https://api.mch.weixin.qq.com/pay/closeorder";

}
