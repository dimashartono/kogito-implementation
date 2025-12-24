package com.ecommerce.processor.exception;

public class OrderPersistenceException extends RuntimeException {

    public OrderPersistenceException(String message) {
        super(message);
    }

    public OrderPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderPersistenceException(Throwable cause) {
        super(cause);
    }
}
