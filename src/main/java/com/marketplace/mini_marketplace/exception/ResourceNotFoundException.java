package com.marketplace.mini_marketplace.exception;

//This is a custom exception class — nothing more than a wrapper around RuntimeException
//It exists so that when something isn't found (a product, a user, an order),
//the code can throw a meaningful, named exception instead of a generic one.  see it being used in the services
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}