package com.sky.Aop;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
// 【关键修改点1】必须导入 MethodSignature 这个具体的类
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
//作用是自动填充，无需手动设置了，自动设置了创建时间、修改时间、创建人、修改人
@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
public void PointcutFill() {

    }

    //前置通知
    @Before("PointcutFill()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
log.info("自动填充功能开启了");
//首先获取被拦截的方法的参数列表，自己写代码注意把要自动填充的放在参数列表的第一位
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        Object arg = args[0];

        //在获取被拦截的方法是什么
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        OperationType type = signature.getMethod().getAnnotation(AutoFill.class).value();//获取注解上的值，知道是insert还是update


        //准备要赋值的数据,需要时间和登录人的id
        LocalDateTime now=LocalDateTime.now();
        Long id= BaseContext.getCurrentId();

        //根据方法修改值
if(type == OperationType.INSERT) {
//    private LocalDateTime createTime;
//    private LocalDateTime updateTime;
//    private Long createUser;
//    private Long updateUser;

        arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(arg, now);
  arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(arg, now);
        arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(arg, id);
        arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(arg, id);

}
if(type == OperationType.UPDATE) {
    arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(arg, now);
    arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(arg, id);

    }
}

}
