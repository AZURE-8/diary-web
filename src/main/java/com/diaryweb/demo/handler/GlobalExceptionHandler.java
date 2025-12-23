package com.diaryweb.demo.handler;

import com.diaryweb.demo.common.ApiResponse;
import com.diaryweb.demo.common.BizException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//全局异常处理器：统一拦截 Controller 层抛出的异常，返回标准化的 ApiResponse JSON
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常（自己抛的）
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK) // 业务错误统一返回 200 + code
    public ApiResponse<Void> handleBizException(BizException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    // 参数校验失败（@Valid）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
        return ApiResponse.fail(4000, "参数校验失败: " + errors);
    }

    // 参数绑定失败（@RequestParam / @PathVariable 类型不对）
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> handleBind(BindException e) {
        return ApiResponse.fail(4000, "参数绑定失败: " + e.getMessage());
    }

    // JSON 解析失败（Body 不合法）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> handleJsonParse(HttpMessageNotReadableException e) {
        return ApiResponse.fail(4000, "JSON 格式错误或缺少字段");
    }

    // 兜底：未知异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleException(Exception e) {
        // 生产环境不要返回 e.getMessage()
        return ApiResponse.fail(5000, "服务器内部错误: " + e.getMessage());
    }
}
