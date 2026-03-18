package com.vibium.types;

/**
 * A browser cookie.
 */
public class Cookie {
    private String name;
    private String value;
    private String domain;
    private String path;
    private int size;
    private boolean httpOnly;
    private boolean secure;
    private String sameSite;
    private Long expiry;

    public String name() { return name; }
    public String value() { return value; }
    public String domain() { return domain; }
    public String path() { return path; }
    public int size() { return size; }
    public boolean httpOnly() { return httpOnly; }
    public boolean secure() { return secure; }
    public String sameSite() { return sameSite; }
    public Long expiry() { return expiry; }

    @Override
    public String toString() {
        return "Cookie{name='" + name + "', value='" + value + "', domain='" + domain + "'}";
    }
}
