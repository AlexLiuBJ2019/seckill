package com.wx.login;


import com.alibaba.fastjson.JSONObject;
import com.wx.util.WXAuthUtil;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Login {

//    @Resource(name="userService")
//    private UserService userimpl;
    private static Logger logger=LoggerFactory.getLogger(Login.class);
    /**
     * 获取微信用户信息
     * @throws IOException
     * @throws ClientProtocolException
     */
    @RequestMapping("/getWXUserInformation")
    public String getWXUserInformation(HttpServletRequest request) throws ClientProtocolException, IOException {
        /*
         * start 获取微信用户基本信息
         */
        String code=request.getParameter("code");
        System.out.println(code);
        logger.info("code="+code);
        //第二步：通过code换取网页授权access_token
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+WXAuthUtil.APPID
                + "&secret="+ WXAuthUtil.APPSECRET
                + "&code="+code
                + "&grant_type=authorization_code";

        System.out.println("url:"+url);
        logger.info("url="+url);
        JSONObject jsonObject = WXAuthUtil.doGetJson(url);
        System.out.println(jsonObject.toString());
        logger.info("获取token返回信息为:"+jsonObject.toString());
	     /*
	      { "access_token":"ACCESS_TOKEN",
	         "expires_in":7200,
	         "refresh_token":"REFRESH_TOKEN",
	         "openid":"OPENID",
	         "scope":"SCOPE"
	        }
	      */
        String openid = jsonObject.getString("openid");
        String access_token = jsonObject.getString("access_token");
        String refresh_token = jsonObject.getString("refresh_token");
        //第五步验证access_token是否失效；暂时不需要
        String chickUrl="https://api.weixin.qq.com/sns/auth?access_token="+access_token+"&openid="+openid;

        JSONObject chickuserInfo = WXAuthUtil.doGetJson(chickUrl);
        System.out.println(chickuserInfo.toString());
        logger.info("返回验证信息："+chickuserInfo.toString());
        if(!"0".equals(chickuserInfo.getString("errcode"))){
            // 第三步：刷新access_token（如果需要）-----暂时没有使用,参考文档https://mp.weixin.qq.com/wiki，
            String refreshTokenUrl="https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="+openid+"&grant_type=refresh_token&refresh_token="+refresh_token;

            JSONObject refreshInfo = WXAuthUtil.doGetJson(chickUrl);
	         /*
	          * { "access_token":"ACCESS_TOKEN",
	             "expires_in":7200,
	             "refresh_token":"REFRESH_TOKEN",
	             "openid":"OPENID",
	             "scope":"SCOPE" }
	          */
            System.out.println(refreshInfo.toString());
            access_token=refreshInfo.getString("access_token");
        }

        // 第四步：拉取用户信息(需scope为 snsapi_userinfo)
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token="+access_token
                + "&openid="+openid
                + "&lang=zh_CN";
        System.out.println("infoUrl:"+infoUrl);
        JSONObject userInfo = WXAuthUtil.doGetJson(infoUrl);
        logger.info("用户信息为："+userInfo.toString());
	     /*
	      {    "openid":" OPENID",
	         " nickname": NICKNAME,
	         "sex":"1",
	         "province":"PROVINCE"
	         "city":"CITY",
	         "country":"COUNTRY",
	         "headimgurl":    "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46",
	         "privilege":[ "PRIVILEGE1" "PRIVILEGE2"     ],
	         "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
	         }
	      */
        System.out.println("JSON-----"+userInfo.toString());
        System.out.println("名字-----"+userInfo.getString("nickname"));
        System.out.println("头像-----"+userInfo.getString("headimgurl"));
        //request.getSession().setAttribute("openid", openid);
        Map<String, String> map=new HashMap<String,String>();
        map.put("openid", openid);
        map.put("nickname", userInfo.getString("nickname"));
        map.put("headimgurl", userInfo.getString("headimgurl"));
        request.getSession().setAttribute("wxUserMap", map);
        /*
         * end 获取微信用户基本信息
         */
        //获取到用户信息后就可以进行重定向，走自己的业务逻辑了。。。。。。
        //接来的逻辑就是你系统逻辑了，请自由发挥
        return "operatingVan";
    }

}
