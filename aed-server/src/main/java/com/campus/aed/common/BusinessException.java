package com.campus.aed.common;

import lombok.Getter;

/**
 * 业务异常: 由全局异常处理器统一转换为 Result 返回
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 状态码, 默认 400(业务校验失败); 特殊场景(如越权)显式传入 403 */
    private final Integer code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
