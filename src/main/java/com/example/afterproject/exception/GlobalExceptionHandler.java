package com.example.afterproject.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // Spring Security 예외
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 권한 없음 (403 Forbidden) - SecurityException (서비스 로직에서 발생)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", ex.getMessage()); // "이 강좌를 수정할 권한이 없습니다." 등의 메시지
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 2. 접근 거부 (403 Forbidden) - AccessDeniedException (Spring Security 필터에서 발생)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "접근 권한이 없습니다.");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 3. 데이터 찾을 수 없음 (404 Not Found) - EntityNotFoundException
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 4. 잘못된 요청 상태 (400 Bad Request) - IllegalStateException (정원 초과, 출석률 미달 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 5. 잘못된 인자 (400 Bad Request) - IllegalArgumentException (잘못된 입력값)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 기존: 리소스 없음 (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested URL was not found on this server.");
        errorResponse.put("path", ex.getResourcePath());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}