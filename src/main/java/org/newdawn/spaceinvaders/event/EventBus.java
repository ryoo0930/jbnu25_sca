package org.newdawn.spaceinvaders.event;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple singleton Event Bus to manage event publishing and subscription.
 */
public class EventBus {
    private static final EventBus instance = new EventBus();
    private final List<EventListener> listeners = new ArrayList<>();

    private EventBus() {}

    public static EventBus getInstance() {
        return instance;
    }

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    public void publish(Event event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
