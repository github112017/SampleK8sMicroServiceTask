package com.showbie.common.models;

/**
 * Model class for serializing messages, includes both a text and the origin.
 */
public class Message {

    private String text;
    private String origin;

    public Message() {
    }

    public Message(String text, String origin) {
        this.text = text;
        this.origin = origin;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String toString() {
        return (origin == null ? "<null>" : origin) + ": " + (text == null ? "<null>" : text);
    }
}
