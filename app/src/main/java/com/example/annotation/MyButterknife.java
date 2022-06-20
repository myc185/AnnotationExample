package com.example.annotation;

import android.app.Activity;

public class MyButterknife {
    public static void bind(Activity activity) {
        String name = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<?> aClass = Class.forName(name);
            IBinder iBinder = (IBinder) aClass.newInstance(); //通过反射，生成模板类的实例
            iBinder.bind(activity); //调用这个方法，会取执行对应模板类的bind方法，实现findViewById的功能
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
