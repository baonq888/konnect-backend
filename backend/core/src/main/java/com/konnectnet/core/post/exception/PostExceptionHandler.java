package com.konnectnet.core.post.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
@Slf4j
public class PostExceptionHandler {

    @ExceptionHandler(PostException.class)
    public ResponseEntity<String> handlePostException(PostException e) {
        log.error("PostException caught: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post error: " + e.getMessage());
    }

}