package org.newdawn.spaceinvaders.system;

import org.newdawn.spaceinvaders.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all entities in the game.
 * Handles adding, removing, and providing access to the list of entities.
 */
public class EntityManager {
    private final List<Entity> entities = new ArrayList<>();
    private final List<Entity> removeList = new ArrayList<>();
    private final List<Entity> addList = new ArrayList<>();

    public void addEntity(Entity entity) {
        addList.add(entity);
    }

    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    public List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /**
     * Should be called once per game loop to process the add and remove lists.
     * This avoids concurrent modification issues.
     */
    public void updateLists() {
        entities.addAll(addList);
        addList.clear();
        entities.removeAll(removeList);
        removeList.clear();
    }

    public void clear() {
        entities.clear();
        addList.clear();
        removeList.clear();
    }
}
