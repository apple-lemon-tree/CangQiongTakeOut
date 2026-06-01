package com.abc.context;

/**
 * ThreadLocal并不是一个Thread,而是Thread的局部变量。ThreadLocal为每个线程提供单独一份存储空间，
 * 具有线程隔离效果，只要在线程内才能获取到对应的值，线程外则不能访问。
 * 客户端每次请求都是一个单独的线程
 */

public class CurrentHolder {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    //设置当前线程的线程局部变量值，这里局部变量是一个Integer的id
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }
    //返回当前线程对应的局部变量的值
    public static Long getCurrentId(){
        return threadLocal.get();
    }
    //移除当前线程的线程局部变量
    public static void remove(){
        threadLocal.remove();
    }

}
