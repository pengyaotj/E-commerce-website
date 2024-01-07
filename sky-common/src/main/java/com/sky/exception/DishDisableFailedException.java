package com.sky.exception;

/**
 * 菜品停售异常
 */
public class DishDisableFailedException extends BaseException{
    public DishDisableFailedException(){}

    public DishDisableFailedException(String msg){
        super(msg);
    }
}
