package com.showbie.common.models;

/**
 * Model class for serializing messages, includes both a public and private text.
 */
public class ExternalMessage {

    private String publicText;
    private String privateText;

    public ExternalMessage() {
    }

    public ExternalMessage(String publicText) {
        this(publicText, null);
    }

    public ExternalMessage(String publicText, String privateText) {
        this.publicText = publicText;
        this.privateText = privateText;
    }

    public String getPublicText() {
        return publicText;
    }

    public void setPublicText(String publicText) {
        this.publicText = publicText;
    }

    public String getPrivateText() {
        return privateText;
    }

    public void setPrivateText(String privateText) {
        this.privateText = privateText;
    }

}
