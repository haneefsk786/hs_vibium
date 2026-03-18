package com.vibium.errors;

/**
 * Thrown when a selector matches no elements.
 */
public class ElementNotFoundException extends VibiumException {
    public ElementNotFoundException(String message) {
        super(message);
    }
}
