package com.cameronlattz.murderparty.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Map {
    private String _name;
    private String _displayName;
    private int _probability;
    private String _regionName;
    private List<Location> _spawnLocations;

    public Map(String name, String displayName, int probability, String regionName) {
        _name = name;
        _displayName = displayName;
        _probability = probability;
        _regionName = regionName;
        // set spawn locations
    }

    public String getName() { return _name; }

    public int getProbability() { return _probability; }

    public List<Location> getSpawnLocations() { return _spawnLocations; }

    public String getRegionName() { return _regionName; }

    public String[] getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("Name: " + _name);
        info.add("Probability: " + _probability);
        info.add("Region: " + _regionName);
        info.add("Spawn locations:");
        for (Location s : _spawnLocations) {
            info.add("   -" + s.getBlockX() + ", " + s.getBlockY() + ", " + s.getBlockZ());
        }
        return info.toArray(new String[0]);
    }

    public static List<String> getOptions() {
        return Arrays.asList("Name", "Probability", "Region");
    }
}
