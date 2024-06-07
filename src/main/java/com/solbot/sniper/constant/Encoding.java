package com.solbot.sniper.constant;

public enum Encoding {
    base64("base64"),
    base58("base58"),
    jsonParsed("jsonParsed");

    private final String enc;

    private Encoding(String enc) {
        this.enc = enc;
    }

    public String getEncoding() {
        return this.enc;
    }
}
