package com.showbie.publicservice.models;

/**
 * Model class for serializing messages
 */
public class Message {

    private String publicText;
    private String privateText;

    public Message(String publicText) {
        this(publicText, null);
    }

    public Message(String publicText, String privateText) {
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
