package com.cameronlattz.murderparty.models;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public enum Ability  {
    TRACKING("tracking"), SUICIDAL("suicidal");

    private String _name;
    private Location _compassTarget = null;

    private Ability(String name) {
        _name = name;
    }

    public String getName() { return _name; };

    public Location getCompassTarget() { return _compassTarget; }

    public void setCompassTarget(Player player, Location location) {
        if (location != null) {
            _compassTarget = location;
            player.setCompassTarget(location);
        }
    }

    public static Ability getByName(String name) {
        for (Ability ability : Ability.values()) {
            if (ability.getName().equals(name)) {
                return ability;
            }
        }
        return null;
    }
}
