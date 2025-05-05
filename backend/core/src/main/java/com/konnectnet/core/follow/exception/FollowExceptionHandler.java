package com.konnectnet.core.follow.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class FollowExceptionHandler {
    @ExceptionHandler(FollowException.class)
    public ResponseEntity<String> handleFollowException(FollowException e) {
        log.error("FollowException caught: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Follow error: " + e.getMessage());
    }
}
