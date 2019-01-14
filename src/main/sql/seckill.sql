-- 秒杀执行储存过程
DELIMITER  $$  -- console ; 转化为  $$
-- 定义储存过程
-- 参数  in 输入参数    out 输出参数
-- row_count():返回上一条修改类型sql（delete,insert,update）的影响行数
-- row_count();0 未修改数据  >0 表示已修改数据
  CREATE  PROCEDURE `seckill`.`execute_seckill`
      -- 定义变量  in 输入类型
      (in v_seckill_id BIGINT,in v_phone BIGINT, in v_kill_time TIMESTAMP,
            out r_result int )
      -- 开始
    BEGIN
          -- 定义变量
        DECLARE  insert_count int DEFAULT  0;
          -- 开启一个事物
              START TRANSACTION ;
                    INSERT  ignore INTO  success_killed
                          (seckill_id,user_phone,create_time)
                          VALUES (v_seckill_id,v_phone,v_kill_time);
                    SELECT  row_count() INTO insert_count;
                          IF (insert_count = 0)THEN
                                ROLLBACK ;
                                set r_result = -1;
                          ELSEIF (insert_count < 0) THEN
                                ROLLBACK ;
                                set r_result = -2;
                          ELSE
                                UPDATE seckill
                                      SET  number = number - 1
                                WHERE  seckill_id = v_seckill_id
                                      AND end_time > v_kill_time
                                      AND start_time < v_kill_time
                                      AND  number > 0;
                                SELECT  row_count() INTO  insert_count;
                                      IF (insert_count = 0) THEN
                                            ROLLBACK ;
                                            SET  r_result =  -1;
                                      ELSEIF (insert_count < 0) THEN
                                            ROLLBACK ;
                                            SET r_result = -2;
                                      ELSE
                                            COMMIT ;
                                            SET r_result = 1;
                                      END IF ;
                          END IF ;
    END ;
$$
-- 储存过程定义来结束

DELIMITER ;

set @r_result=-3;

-- 执行储存过程
call execute_seckill(1002,18802138826,now(),@r_result);

-- 获取结果
select@r_result;

-- 储存过程
-- 1. 存储过程优化 事物行级锁持有时间
-- 2.不要过度依赖储存过程
-- 3.简单的逻辑可以应用储存过程
-- 4.QPS：一个商品（秒杀单）6000/qbs