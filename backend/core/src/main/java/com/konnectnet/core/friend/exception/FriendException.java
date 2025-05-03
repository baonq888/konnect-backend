package com.konnectnet.core.friend.exception;

public class FriendException extends RuntimeException {
    public FriendException(String message) {
        super(message);
    }
    public FriendException(String message, Throwable cause) {
    super(message, cause);
  }

}
