package com.vibium.types;

/**
 * Options for scrolling the page.
 */
public class ScrollOptions {
    private String direction;
    private Integer amount;
    private String selector;

    public ScrollOptions direction(String direction) { this.direction = direction; return this; }
    public ScrollOptions amount(int amount) { this.amount = amount; return this; }
    public ScrollOptions selector(String selector) { this.selector = selector; return this; }

    public String direction() { return direction; }
    public Integer amount() { return amount; }
    public String selector() { return selector; }
}
