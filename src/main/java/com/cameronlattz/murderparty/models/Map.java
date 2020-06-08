package com.cameronlattz.murderparty.models;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Map implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Integer _probability;
    private ProtectedRegion _region;
    private List<Location> _spawnLocations = new ArrayList<Location>();

    public Map(String name, String displayName, Integer probability, ProtectedRegion region, World world) {
        _name = name;
        _displayName = displayName;
        _probability = probability;
        _region = region;
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
                        _spawnLocations.add(new Location(world, x, y, z));
                    }
                }
            }
        }
    }

    public String getName() { return _name; }

    public Integer getProbability() { return _probability; }

    public List<Location> getSpawnLocations() { return new ArrayList<Location>(_spawnLocations); }

    public ProtectedRegion getRegion() { return _region; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  probability: " + _probability);
        info.add("  region: " + _region.getId());
        info.add("  spawn locations:");
        for (Location s : _spawnLocations) {
            info.add("   -" + s.getBlockX() + ", " + s.getBlockY() + ", " + s.getBlockZ());
        }
        return info;
    }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("probability", "integer");
            put("region", "string");
        }};
    }
}
