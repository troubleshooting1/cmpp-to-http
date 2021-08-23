package com.sb.cmpptohttp.domain.result;


public enum Code implements CodeSupport {

  /* 成功 */
  SUCCESS(0, "成功"),

  /* 默认失败 */
  COMMON_FAIL(999, "失败"),

  /* 参数错误：1000～1999 */
  PARAM_NOT_VALID(1001, "参数无效"),
  PARAM_IS_BLANK(1002, "参数为空"),
  PARAM_TYPE_ERROR(1003, "参数类型错误"),
  PARAM_NOT_COMPLETE(1004, "参数缺失"),

  /* 用户错误 */
  USER_NOT_LOGIN(2001, "用户未登录"),
  USER_ACCOUNT_EXPIRED(2002, "账号已过期"),
  USER_CREDENTIALS_ERROR(2003, "账号或密码错误"),
  USER_CREDENTIALS_EXPIRED(2004, "密码过期"),
  USER_ACCOUNT_DISABLE(2005, "账号不可用"),
  USER_ACCOUNT_LOCKED(2006, "账号被锁定"),
  USER_ACCOUNT_NOT_EXIST(2007, "账号不存在"),
  USER_ACCOUNT_ALREADY_EXIST(2008, "账号已存在"),
  USER_ACCOUNT_USE_BY_OTHERS(2009, "账号下线"),
  FILE_EMPTY(2010, "文件上传不能为空"),
  FILE_FORMAT(2011, "文件格式错误"),

  /* 业务错误 */
  NO_PERMISSION(3001, "没有权限"),

  UNKNOWN(-1, "Code is unknown"),

  ERROR_500(500, "Request is error:500"),
  ERROR_404(404, "Request is error:404"),
  ERROR_400(400, "Request is error:400"),
  ERROR(2, "Request is error"),

  ERROR_lOGIN(300,"登录过期，请登录"),

  FAIL_DAO(1003, "Request is fail:DAOException"),
  FAIL_TIMEOUT(1002, "Request is fail:TIMEOUT"),
  FAIL_NULL(1001, "Request is fail:NULL"),
  FAIL(1, "Request is fail"),
  BAD_ARGS(8001, "bad args:");

  private int code;
  private String msg;

  Code(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String msg() {
    return msg;
  }

  private Code getCode(int code) {
    for (Code tmpCode : Code.values()) {
      if (tmpCode.code() == code) {
        return tmpCode;
      }
    }
    return Code.UNKNOWN;
  }
}
