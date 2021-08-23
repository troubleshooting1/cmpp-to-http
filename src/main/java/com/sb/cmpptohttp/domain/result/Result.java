package com.sb.cmpptohttp.domain.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

import static com.sb.cmpptohttp.domain.result.Code.ERROR_500;


/**
 * 通用返回结果
 *
 * @author qiangchen
 */
@Data
public class Result<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  public Result() {
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;
  private Integer code;
  private Long timestamp;
  private String msg;

  public static <T> Result<T> buildSucc(T data) {
    return build(data, Code.SUCCESS.code(), Code.SUCCESS.msg());
  }

  public static <T> Result<T> buildFaild() {
    return buildFaild(ERROR_500.code(), null);
  }

  public static <T> Result<T> buildFaild(String msg) {
    return buildFaild(ERROR_500.code(), msg);
  }

  public static <T> Result<T> buildFaild(CodeSupport code, String msg) {
    return buildFaild(code.code(), msg);
  }

  public static <T> Result<T> buildFaild(Integer code, String msg) {
    return build(null, code, msg);
  }

  public static <T> Result<T> buildSucc() {
    return buildSucc(null);
  }

  public static <T> Result<T> build(T data, Integer code, String message) {
     Result<T> result = new Result<>();
     result.setData(data);
     result.setCode(code);
     result.setMsg(message);
     result.setTimestamp(System.currentTimeMillis());
     return result;
  }

  @JsonIgnore
  public boolean succ() {
    return this.code != null && this.code == Code.SUCCESS.code();
  }

  /**
   * 获取data. 当code != SUCCESS.code 的时候,会抛出 RuntimeException().
   */
  public T touch() {
    if (!this.succ()) {
      throw new RuntimeException(this.code + "," + this.getMsg());
    }
    return this.getData();
  }

  public boolean equalsByCode(CodeSupport code) {
    if (code == null) {
      return false;
    }

    return this.code == code.code();
  }

  @JsonIgnore
  public boolean isSuccess(){
    return this.code != null && this.code == Code.SUCCESS.code();
  }
}