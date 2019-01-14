package com.seckill.service;

public interface IResourceDicService {

    /**
     * 获取Excel放入数据库
     * @param rname
     * @param retailPrice
     * @param costPrice
     * @param intcode
     * @param number
     * @return  > 1  表示成功
     */
  int addExcelImport(String rname,double retailPrice,double costPrice,String intcode,int number);
}
