package com.chaotic_loom.loom.core.events;

public enum EventResult {
    PASS, // Continue to the next listener
    CANCEL; // Stop the event propagation immediately
}