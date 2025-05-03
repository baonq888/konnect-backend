package com.konnectnet.core.friend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class FriendExceptionHandler {
    @ExceptionHandler(FriendException.class)
    public ResponseEntity<String> handleFriendException(FriendException e) {
        log.error("FriendException caught: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Friend error: " + e.getMessage());
    }

}
