package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 切面类,用于实现功能字段自动填充的功能
 * 1.定义切面类,并使用@Aspect注解标识
 * 2.定义切点,指定哪些方法需要进行功能字段自动填充处理
 * 3.定义通知,在方法执行前或执行后进行功能字段自动填充处理
 * 4.在通知中获取当前用户id,并根据数据库操作类型进行不同的自动填充处理
 * 5.将切面类注册到Spring容器中,使其生效
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知,在方法执行前进行功能字段自动填充处理
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("执行了功能字段自动填充的切面逻辑");

        //1.获取当前被拦截的方法上的数据库操作类型，是insert还是update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //获取方法签名
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);//获取方法上的注解对象
        OperationType operationType = annotation.value(); //获取数据库操作类型

        //2.获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if (args==null ||args.length ==0){
            return;
        }
         Object entity = args[0]; //获取到实体对象

        //3.准备赋值的数据
            Long currentId = BaseContext.getCurrentId(); //获取当前用户id
            LocalDateTime now = LocalDateTime.now(); //获取当前时间

        //4.根据数据库操作类型进行不同的自动填充处理，通过反射给实体对象的属性赋值
        if (operationType == OperationType.INSERT){
            //为四个公共字段赋值：createTime、createUser、updateTime、updateUser
            try {
                Method setCreateTime  = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser  = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime  = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser  = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射给实体对象的属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if (operationType == OperationType.UPDATE){
            //为两个公共字段赋值：updateTime、updateUser
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射给实体对象的属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
