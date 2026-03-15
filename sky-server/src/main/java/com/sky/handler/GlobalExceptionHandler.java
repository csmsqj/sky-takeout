package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
//全局异常处理器，处理项目中抛出的业务异常，异常类型写在公共类中。
//若有异常，从下往上找，先找业务异常，再找未知异常
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    //SQLIntegrityConstraintViolationException是MySQL数据库抛出的异常，表示违反了数据库的完整性约束，例如插入重复的主键值、违反外键约束等
    // 无需专门定义一个异常类来处理这个异常，可以直接在全局异常处理器中捕获并处理它
    // 因为它是一个常见的数据库异常，处理方式也比较统一。
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        if(ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            return Result.error(s[2] + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }


}
