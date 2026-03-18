package com.vibium.types;

/**
 * Parameters for setting a cookie.
 */
public class SetCookieParam {
    private String name;
    private String value;
    private String domain;
    private String url;
    private String path;
    private Boolean httpOnly;
    private Boolean secure;
    private String sameSite;
    private Long expiry;

    public SetCookieParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SetCookieParam domain(String domain) { this.domain = domain; return this; }
    public SetCookieParam url(String url) { this.url = url; return this; }
    public SetCookieParam path(String path) { this.path = path; return this; }
    public SetCookieParam httpOnly(boolean httpOnly) { this.httpOnly = httpOnly; return this; }
    public SetCookieParam secure(boolean secure) { this.secure = secure; return this; }
    public SetCookieParam sameSite(String sameSite) { this.sameSite = sameSite; return this; }
    public SetCookieParam expiry(long expiry) { this.expiry = expiry; return this; }
}
