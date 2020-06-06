package com.cameronlattz.murderparty.models;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Map {
    private String _name;
    private String _displayName;
    private Integer _probability;
    private String _regionName;
    private List<Location> _spawnLocations;

    public Map(String name, String displayName, Integer probability, ProtectedRegion region, World world) {
        _name = name;
        _displayName = displayName;
        _probability = probability;
        _regionName = region.getId();
        int minX = region.getMinimumPoint().getBlockX();
        int minY = region.getMinimumPoint().getBlockY();
        int minZ = region.getMinimumPoint().getBlockZ();
        int maxX = region.getMaximumPoint().getBlockX();
        int maxY = region.getMaximumPoint().getBlockY();
        int maxZ = region.getMaximumPoint().getBlockZ();
        for(int x = minX; x < maxX; x++) {
            for(int y = minY; y < maxY; y++) {
                for(int z = minZ; z < maxZ; z++) {
                    Block block =  world.getBlockAt(x,y,z);
                    if (block.getType() == Material.JIGSAW) {
                        Location w = new Location(world, x, y, z);
                        //_spawnLocations.add(new Location(world, x, y, z));
                    }
                }
            }
        }
    }

    public String getName() { return _name; }

    public Integer getProbability() { return _probability; }

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

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("probability", "integer");
            put("region", "string");
        }};
    }
}
