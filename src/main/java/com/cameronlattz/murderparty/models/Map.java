package com.cameronlattz.murderparty.models;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import scala.Int;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Map implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Integer _probability;
    private ProtectedRegion _region;
    private List<Location> _spawnLocations = new ArrayList<Location>();
    private List<Location> _dropLocations = new ArrayList<Location>();
    private int _ticksPerAmmoDrop = 0;
    private int _ticksPerWeaponDrop = 0;

    public Map(String name, String displayName, Integer probability, ProtectedRegion region, World world, Integer ticksPerAmmoDrop, Integer ticksPerWeaponDrop) {
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
                    if (!block.isEmpty() && !block.isLiquid() && !block.isPassable()) {
                        Block above1 = block.getRelative(0, 1, 0);
                        Block above2 = block.getRelative(0, 2, 0);
                        if ((above1.isEmpty() || above1.isPassable()) && (above2.isEmpty() || above2.isPassable())) {
                            _dropLocations.add(new Location(world, x, y, z));
                        }
                    }
                }
            }
        }
        _ticksPerAmmoDrop = ticksPerAmmoDrop != null ? ticksPerAmmoDrop : _ticksPerAmmoDrop;
        _ticksPerWeaponDrop = ticksPerWeaponDrop != null ? ticksPerWeaponDrop : _ticksPerWeaponDrop;
    }

    public String getName() { return _name; }

    public String getDisplayName() { return _displayName; }

    public Integer getProbability() { return _probability; }

    public List<Location> getSpawnLocations() { return new ArrayList<Location>(_spawnLocations); }

    public List<Location> getDropLocations() { return new ArrayList<Location>(_dropLocations); }

    public ProtectedRegion getRegion() { return _region; }

    public int getTicksPerAmmoDrop() { return _ticksPerAmmoDrop; }

    public int getTicksPerWeaponDrop() { return _ticksPerWeaponDrop; }

    public boolean containsLocation(Location location) {
        return _region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  probability: " + _probability);
        info.add("  region: " + _region.getId());
        info.add("  ticks per ammo drop: " + _ticksPerAmmoDrop);
        info.add("  ticks per weapon drop: " + _ticksPerWeaponDrop);
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
            put("ticksPerAmmoDrop", "integer");
            put("ticksPerWeaponDrop", "integer");
        }};
    }
}
