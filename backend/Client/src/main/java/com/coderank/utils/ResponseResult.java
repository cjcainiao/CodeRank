package com.coderank.utils;



import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果工具类
 * @param <T>
 */

@Data
public  class ResponseResult<T> implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    private ResponseResult(Integer code , String msg , T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseResult<T> success(T data){
        return new ResponseResult<>(200,"操作成功",data);
    }

    public static <T> ResponseResult<T> success(String msg){
        return new ResponseResult<>(200,msg,null);
    }

    public static <T> ResponseResult<T> success(String msg, T data){
        return new ResponseResult<>(200,msg,data);
    }

    public static <T> ResponseResult<T> error(Integer code , String msg){
        return new ResponseResult<>(code,msg,null);
    }

    public static <T> ResponseResult<T> error(String msg){
        return new ResponseResult<>(500,msg,null);
    }

    public static <T> ResponseResult<T> error(){
        return new ResponseResult<>(500,"操作失败",null);
    }
}
