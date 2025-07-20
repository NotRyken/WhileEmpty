package com.notryken.config;

public final class DelayedMessage {

    private final String message;
    private int delayTicks;

    public DelayedMessage(String message, int delayTicks) {
        this.message = message;
        this.delayTicks = delayTicks;
    }

    public String message() {
        return message;
    }

    public int delayTicks() {
        return delayTicks;
    }

    public boolean tick() {
        return --delayTicks <= 0;
    }
}
