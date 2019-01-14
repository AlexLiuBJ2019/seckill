package com.wx.pay;

import com.alibaba.fastjson.JSONObject;
import com.wx.entity.PaymentOrder;
import com.wx.util.WXAuthUtil;
import com.wx.util.WXPayUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayMentOrderAction {
//    @Resource(name = "paymentOrderService")
//    private PaymentOrderService paymentImpl;

    /**
     * 点击微信支付弹出支付页面并获取支付参数
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/getPaymentOrder")
    @ResponseBody
    public String getPaymentOrder(HttpServletRequest request, int ordetaileid, String sum) throws Exception {
        Map<String, String> wxUserMap = (Map<String, String>) request.getSession().getAttribute("wxUserMap");
        String openId = wxUserMap.get("openid");
//        PaymentOrder order = paymentImpl.getPaymentOrderOrdetaileid(ordetaileid);
        Map<String, String> payMap = null;
        //String openId="oW-bM0tabXXrrNzfjeq4jNlahGJQ";
        //double sum2=mul(Double.parseDouble(sum),100);//金额转分
        //System.out.println(sum2);
//        if (order == null || order.getStatus() == 3) {//该订单不存在或失效
            Map<String, String> paraMap = new HashMap<String, String>();
            paraMap.put("appid", WXAuthUtil.APPID);
            paraMap.put("body", "测试购买支付");
            paraMap.put("mch_id", WXAuthUtil.MCH_ID);//商户id
            paraMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paraMap.put("notify_url", "---------/getPaymentResult");// 此路径是微信服务器调用支付结果通知路径
            paraMap.put("openid", openId);
            paraMap.put("out_trade_no", ordetaileid + "");
            //paraMap.put("spbill_create_ip", "123.12.12.123");
            paraMap.put("spbill_create_ip", "192.168.1.123");
            paraMap.put("total_fee", "1"); //金额
            paraMap.put("trade_type", "JSAPI"); //交易类型（公众号支付）
            System.out.println(paraMap.toString());
            String sign = WXPayUtil.generateSignature(paraMap, WXAuthUtil.KEY);
            paraMap.put("sign", sign);
            // 统一下单 https://api.mch.weixin.qq.com/pay/unifiedorder
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            String xml = WXPayUtil.GetMapToXML(paraMap);
            String xmlStr = HttpKit.post(url, xml);
            System.out.println("xml=" + xmlStr);
            // 微信预付订单id
            String prepay_id = "";
            Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);
            if ("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))) {
                //Map<String, String> map = doXMLParse(xmlStr);
                prepay_id = map.get("prepay_id");
            } else {
                System.out.println("调用微信支付出错，返回状态码：" + map.get("return_code") + "，返回信息：" + map.get("return_msg"));
                return JSONObject.toJSONString(2);//调用微信支付出错
            }
            //保险系统订单入库
            PaymentOrder payOrder = WXPayUtil.getPaymentOrders(paraMap);
            payOrder.setPrepay_id(prepay_id);
            payOrder.setTime_start(getTimesMiao());
            payOrder.setStatus(1);
//            if (order == null) paymentImpl.addPaymentOrder(payOrder);
//            else if (order.getStatus() == 3) paymentImpl.updatePaymentOrder(payOrder);
//            //int pid=payOrder.getPid();//商户预付订单id
            //
            String timeStamp = WXPayUtil.getCurrentTimestamp() + "";//获取当前时间戳（时分秒）
            String nonceStr = WXPayUtil.generateNonceStr();
            payMap = new HashMap<String, String>();
            payMap.put("appId", paraMap.get("appid"));
            payMap.put("timeStamp", timeStamp);
            payMap.put("nonceStr", nonceStr);
            payMap.put("signType", "MD5");
            payMap.put("package", "prepay_id=" + prepay_id);
            String paySign = WXPayUtil.generateSignature(payMap, WXAuthUtil.KEY);
            System.out.println(paySign);
            payMap.put("paySign", paySign);
            //添加商户系统订单传到前台的数据appid，timestamp,noncestr,signType,package
            payOrder.setNonce_str(nonceStr);
            payOrder.setTimestamp(timeStamp);
            payOrder.setSingType("MD5");
            payOrder.setPackages(payMap.get("package"));
            payOrder.setPaySign(paySign);
//            paymentImpl.updatePaymentOrder(payOrder);
//        } else if (order.getStatus() == 1) {//订单未支付,
//            payMap = WXPayUtil.getMaptoOrder(order);
//        } else {
            //该订单已支付，但由于各种原因未跳转至指定页面
            return JSONObject.toJSONString(1);//订单已支付
        }
//        String str = JSONObject.toJSONString(payMap);
     //   System.out.println(str);
//        return str;
//    }

    private String getTimesMiao() {

        return null;//TODO
    }

    /**
     * 支付结果通知页面
     *
     * @throws Exception
     */
    @RequestMapping("/getPaymentResult")
    @ResponseBody
    public String getPaymentResult(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String out_trade_no = null;
        String return_code = null;
        String result_code = null;
        Map<String, Object> resultMap = null;
        try {
            InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            String resultStr = new String(outSteam.toByteArray(), "utf-8");
            System.out.println(resultStr);
            //logger.info("支付成功的回调："+resultStr);
            resultMap = WXPayUtil.parseXmlToList(resultStr);
            request.setAttribute("out_trade_no", out_trade_no);
            //通知微信.异步确认成功.必写.不然微信会一直通知后台.八次之后就认为交易失败了.
            //response.getWriter().write(RequestHandler.setXML("SUCCESS", ""));
        } catch (Exception e) {
            //logger.error("微信回调接口出现错误：",e);
            try {
                //response.getWriter().write(RequestHandler.setXML("FAIL", "error"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        PaymentOrder paymentOrder = new PaymentOrder();
        Map<String, String> return_data = new HashMap<String, String>();
        return_code = (String) resultMap.get("return_code");
        out_trade_no = (String) resultMap.get("out_trade_no");
        if (return_code.equals("SUCCESS")) {
            //支付成功的业务逻辑,如果支付成功，则修改商户订单的状态，
//            paymentOrder = paymentImpl.getPaymentOrderOrdetaileid(Integer.parseInt(out_trade_no));
            if (paymentOrder == null) {
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "订单不存在");
                return WXPayUtil.mapToXml(return_data);
            } else {
                result_code = (String) resultMap.get("result_code");
                if (result_code.equals("SUCCESS")) {
                    if (paymentOrder.getStatus() == 2) {
                        return_data.put("return_code", "SUCCESS");
                        return_data.put("return_msg", "OK");
                        return WXPayUtil.mapToXml(return_data);
                    } else {
                        //String sign = resultMap.get("sign").toString();
                        String total_fee = resultMap.get("total_fee").toString();//订单金额
                        if (!(paymentOrder.getTotal_fee() + "").equals(total_fee)) {
                            return_data.put("return_code", "FAIL");
                            return_data.put("return_msg", "金额异常");
                            return WXPayUtil.mapToXml(return_data);
                        } else {
                            String time_end = resultMap.get("time_end").toString();
                            //String settlement_total_fee = resultMap.get("settlement_total_fee").toString();
                            paymentOrder.setStatus(2);
                            paymentOrder.setTime_expire(time_end);
                            paymentOrder.setOrdetaileid(Integer.parseInt(out_trade_no));
                            //paymentOrder.setTime_expire(System.currentTimeMillis()+"");
//                            int i = paymentImpl.updatePaymentOrder(paymentOrder);
//                            if (i <= 0) {
                                return_data.put("return_code", "FAIL");
                                return_data.put("return_msg", "更新订单失败");
                                return WXPayUtil.mapToXml(return_data);
//                            } else {
//                                return_data.put("return_code", "SUCCESS");
//                                return_data.put("return_msg", "OK");
//                                return WXPayUtil.mapToXml(return_data);
//                            }
                        }
                    }
                }
            }
        } else {
            //支付失败的业务逻辑
            if (paymentOrder != null) {
                paymentOrder.setStatus(1);
//                paymentImpl.updatePaymentOrder(paymentOrder);
            }
            return_data.put("return_code", "FAIL");
            return_data.put("return_msg", resultMap.get("return_msg").toString());
            return WXPayUtil.mapToXml(return_data);
        }
        String xml = WXPayUtil.mapToXml(return_data);
        System.out.println(xml);
        return xml;
    }

    /**
     * 查询订单接口
     *
     * @throws Exception
     */
    @RequestMapping("/findWXOrder")
    @ResponseBody
    public String findWXOrder(int ordetaileid) throws Exception {
        String appid = WXAuthUtil.APPID;
        String mch_id = WXAuthUtil.MCH_ID;
//        PaymentOrder paymentOrder = paymentImpl.getPaymentOrderOrdetaileid(ordetaileid);
        //String transaction_id="wx20180320140726da1319d3f80503302647";//微信订单号
        String nonce_str = WXPayUtil.generateNonceStr();
        String sign_type = "MD5";
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid", appid);
        paraMap.put("mch_id", mch_id);
        paraMap.put("out_trade_no", ordetaileid + "");
        paraMap.put("nonce_str", nonce_str);
        paraMap.put("sign_type", sign_type);
        String sign = WXPayUtil.generateSignature(paraMap, WXAuthUtil.KEY);
        paraMap.put("sign", sign);
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        String xml = WXPayUtil.GetMapToXML(paraMap);
        String xmlStr = HttpKit.post(url, xml);
        System.out.println(xmlStr);
        Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);
        if (map.get("return_code").equals("FAIL")) {
//            paymentOrder.setStatus(1);
//            paymentImpl.updatePaymentOrder(paymentOrder);
//            return JSONObject.toJSONString(1);//微信支付失败，修改商户订单状态为待支付
//        } else if (map.get("return_code").equals("SUCCESS")) {
//            if (map.get("result_code").equals("FAIL")) {
//                paymentOrder.setStatus(1);
//                paymentImpl.updatePaymentOrder(paymentOrder);
//                return JSONObject.toJSONString(1);//微信支付失败，修改商户订单状态为待支付
//            } else {
//                if (map.get("trade_state").equals("SUCCESS")) {
//                    return JSONObject.toJSONString(2);//微信支付成功,将信息传给前端并跳转至签约界面
//                } else if (map.get("trade_state").equals("CLOSED")) {
//                    paymentOrder.setStatus(3);
//                    paymentImpl.updatePaymentOrder(paymentOrder);
                    return JSONObject.toJSONString(1);//微信支付失败，修改商户订单状态为已失效
                } else {
//                    paymentOrder.setStatus(1);
//                    paymentImpl.updatePaymentOrder(paymentOrder);
                    return JSONObject.toJSONString(1);//微信支付失败，修改商户订单状态为待支付
                }
            }


//        }
//        return JSONObject.toJSONString(1);
//    }

    /**
     * 微信关闭订单
     *
     * @throws Exception
     */
    @RequestMapping("/wxCloseorder")
    @ResponseBody
    public String wxCloseorder(String out_trade_no, String nonce_str) throws Exception {
        String appid = WXAuthUtil.APPID;
        String mch_id = WXAuthUtil.MCH_ID;
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid", appid);
        paraMap.put("mch_id", mch_id);
        paraMap.put("out_trade_no", out_trade_no);
        paraMap.put("nonce_str", nonce_str);
        String sign = WXPayUtil.generateSignature(paraMap, WXAuthUtil.KEY);
        paraMap.put("sign", sign);
        String xml = WXPayUtil.GetMapToXML(paraMap);
        String xmlStr = HttpKit.post(url, xml);
        System.out.println(xmlStr);
        //Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);
        return "";
    }
}
