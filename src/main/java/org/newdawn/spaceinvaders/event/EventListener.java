package org.newdawn.spaceinvaders.event;

/**
 * An interface for any class that wants to listen for events.
 */
public interface EventListener {
    /**
     * Called when an event this listener is subscribed to occurs.
     * @param event The event that occurred.
     */
    void onEvent(Event event);
}
