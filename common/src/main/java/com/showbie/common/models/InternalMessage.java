package com.showbie.common.models;

/**
 * Model class for serializing messages
 */
public class InternalMessage {

    private String text;

    public InternalMessage() {}

    public InternalMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
