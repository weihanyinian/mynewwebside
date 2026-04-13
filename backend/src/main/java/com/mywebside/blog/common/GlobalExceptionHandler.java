package com.mywebside.blog.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
    return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgNotValid(MethodArgumentNotValidException e) {
    var msg = e.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
        .orElse("参数错误");
    return ResponseEntity.ok(ApiResponse.error(400, msg));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
    return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(500, "服务器错误"));
  }
}
