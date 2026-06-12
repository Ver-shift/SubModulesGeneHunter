package org.galaxy.beyond.api.system.teleport;

public enum BeyondTeleportType {
    HOME("home"),
    ACTIVE_BOUNDARY("activeBoundary"),
    ANY_NODE("any"),
    UNLOCKED_NODE("unlocked"),
    LOCKED_NODE("locked"),
    INSIDE_NODE("inside"),
    OUTSIDE_NODE("outside");

    private final String id;

    BeyondTeleportType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
