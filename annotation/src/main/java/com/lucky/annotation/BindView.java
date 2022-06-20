package com.lucky.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: Lucky Mo
 * @date: 2022/6/20 13:42
 */
@Retention(RetentionPolicy.CLASS)//编译时起效
@Target(ElementType.FIELD)//针对的是属性
public @interface BindView {
    int value(); //定义输入参数为整形，例如：@BindView(R.id.xxx)
}
