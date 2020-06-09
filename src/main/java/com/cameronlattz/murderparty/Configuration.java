package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class Configuration {
    public FileConfiguration _configuration;
    RegionManager _regionManager;
    World _world;
    List<Map> _maps = new ArrayList<Map>();
    List<Team> _teams = new ArrayList<Team>();
    List<Weapon> _weapons = new ArrayList<Weapon>();
    List<Role> _roles = new ArrayList<Role>();
    ProtectedRegion _lobbyRegion;
    boolean _debug;

    public Configuration(MurderParty murderParty) {
        murderParty.saveDefaultConfig();
        _configuration = murderParty.getConfig();
        _debug = this.getBoolean(false,"debug");
        if (_debug) {
            this.debug("DEBUGGING ENABLED.");
        }
        this.loadWorld();
        this.loadWorldGuard();
        this.loadMaps();
        this.loadTeams();
        this.loadWeapons();
        this.loadRoles();
        String lobbyRegionName = this.getString("lobby");
        _lobbyRegion = this.getRegion(lobbyRegionName);
    }

    public World getWorld() { return _world; }

    public ObjectInterface getObject(String type, String name) {
        if (type.equals("map")) {
            return this.getMap(name);
        } else if (type.equals("role")) {
            return this.getRole(name);
        } else if (type.equals("team")) {
            return this.getTeam(name);
        } else if (type.equals("weapon")) {
            return this.getWeapon(name);
        }
        return null;
    }

    public List<String> getInfo(String type, String name) {
        ObjectInterface obj = this.getObject(type, name);
        if (obj == null) {
            return null;
        }
        return obj.getInfo();
    }

    public Map getMap(String name) {
        for (Map map : _maps) {
            if (map.getName().equals(name)) {
                return map;
            }
        }
        return null;
    }

    public List<Map> getMaps() {
        return _maps;
    }

    public Role getRole(String name) {
        for (Role role : _roles) {
            if (role.getName().equals(name)) {
                return role;
            }
        }
        return null;
    }

    public List<Role> getRolesInTeam(Team team) {
        List<Role> roles = new ArrayList<Role>();
        for (Role role : _roles) {
            if (role.getTeam() == team) {
                roles.add(role);
            }
        }
        return roles;
    }

    public Team getTeam(String name) {
        for (Team team : _teams) {
            if (team.getName().equals(name)) {
                return team;
            }
        }
        return null;
    }

    public List<Team> getTeams() {
        return _teams;
    }

    public Weapon getWeapon(String name) {
        for (Weapon weapon : _weapons) {
            if (weapon.getName().equals(name)) {
                return weapon;
            }
        }
        return null;
    }

    public ProtectedRegion getLobbyRegion() { return _lobbyRegion; }

    public ProtectedRegion getRegion(String regionName) {
        return _regionManager.getRegion(regionName);
    }

    public List<Player> getPlayers(ProtectedRegion region) {
        List<Player> playersInWorld = _world.getPlayers();
        List<Player> playersInRegion = new ArrayList<Player>();
        if (region != null) {
            for (Player player : playersInWorld) {
                int x = player.getLocation().getBlockX();
                int y = player.getLocation().getBlockY();
                int z = player.getLocation().getBlockZ();
                if (region.contains(x, y, z)) {
                    playersInRegion.add(player);
                }
            }
        }
        return playersInRegion;
    }

    public List<String> getKeys(String path) {
        ConfigurationSection configurationSection =  _configuration.getConfigurationSection(path);
        List<String> keys = new ArrayList<String>();
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                keys.add(key);
            }
        }
        return keys;
    }

    public Object getValue(String... keys) {
        return _configuration.get(StringUtils.join(keys, "."));
    }

    public Integer getInt(String... keys) {
        Object value = this.getValue(keys);
        if (value == null) {
            return null;
        }
        return (Integer)value;
    }

    public String getString(String... keys) {
        return (String)getValue(keys);
    }

    public List<String> getStringList(String... keys) {
        Object value = getValue(keys);
        if (value != null) {
            return (List<String>)getValue(keys);
        }
        return new ArrayList<String>();
    }

    public Boolean getNullableBoolean(String... keys) {
        Object value = this.getValue(keys);
        if (value == null) {
            return null;
        }
        return (Boolean)value;
    }

    public boolean getBoolean(boolean defaultValue, String... keys) {
        Object value = this.getValue(keys);
        if (value == null) {
            return defaultValue;
        }
        return (Boolean)value;
    }

    public String get(String path, String type) {
        if (type.equals("integer")) {
            Integer integer = this.getInt(path);
            if (integer == null) {
                return null;
            }
            return Integer.toString(integer);
        } else if (type.equals("boolean")) {
            Boolean bool = this.getNullableBoolean(path);
            if (bool == null) {
                return null;
            }
            return Boolean.toString(bool);
        } else {
            return this.getString(path);
        }
    }

    public void set(MurderParty murderParty, String path, String value, String type) {
        if (type.equals("integer")) {
            _configuration.set(path, Integer.parseInt(value));
        } else if (type.equals("boolean")) {
            _configuration.set(path, Boolean.parseBoolean(value));
        } else {
            _configuration.set(path, value);
        }
        murderParty.saveConfig();
        murderParty.load();
    }

    public void setSection(MurderParty murderParty, String path, String value) {
        if (path == null) {
            _configuration.createSection(value);
        } else {
            ConfigurationSection configurationSection = _configuration.getConfigurationSection(path);
            if (configurationSection == null) {
                configurationSection = _configuration.createSection(path);
            }
            configurationSection.createSection(value);
        }
        murderParty.saveConfig();
        murderParty.load();
    }

    public boolean addSection(MurderParty murderParty, String path, String value) {
        ConfigurationSection configurationSection = _configuration.getConfigurationSection(path);
        if (configurationSection != null && configurationSection.isConfigurationSection(value)
            || path == null && _configuration.isConfigurationSection(value)) {
            return false;
        }
        this.setSection(murderParty, path, value);
        return true;
    }

    public boolean remove(MurderParty murderParty, String path) {
        if (getValue(path) == null) {
            return false;
        }
        set(murderParty, path, null, null);
        return true;
    }

    private void loadWeapons() {
        List<String> weaponNames = this.getKeys("weapons");
        for (String weaponName : weaponNames) {
            String displayName = this.getString("weapons", weaponName, "name");
            String materialName = this.getString("weapons", weaponName, "material");
            Material material = Material.matchMaterial(materialName);
            boolean drops = this.getBoolean(false, "weapons", weaponName, "drops");
            String loreListString = this.getString("weapons", weaponName, "lore");
            List<String> lore = new ArrayList<String>();
            if (loreListString != null) {
                lore = Arrays.asList(loreListString.split("\\\\n"));
            }
            String enchantmentListString = this.getString("weapons", weaponName, "enchantments");
            LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>();
            if (enchantmentListString != null) {
                List<String> enchantmentStrings = Arrays.asList(enchantmentListString.split(", "));
                for (String enchantmentString: enchantmentStrings) {
                    int spaceIndex = enchantmentString.indexOf(" ");
                    String enchantmentName = enchantmentString.substring(0, spaceIndex).toLowerCase();
                    NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentName);
                    Enchantment enchantment = Enchantment.getByKey(namespacedKey);
                    int level = Integer.parseInt(enchantmentString.substring(spaceIndex + 1));
                    enchantments.put(enchantment, level);
                }
            }
            _weapons.add(new Weapon(weaponName, displayName, material, drops, lore, enchantments));
        }
    }

    private void loadTeams() {
        List<String> teamNames = this.getKeys("teams");
        for (String teamName : teamNames) {
            String displayName = this.getString("teams", teamName, "name");
            String color = this.getString("teams", teamName, "color");
            Integer probability = this.getInt("teams", teamName, "probability");
            Integer playersBefore = this.getInt("teams", teamName, "playersBeforeSpawn");
            Integer playersPer = this.getInt("teams", teamName, "playersPerSpawn");
            boolean canKillTeammates = this.getBoolean(true, "teams", teamName, "canKillTeammates");
            if (displayName != null && color != null &&  probability != null && playersBefore != null && playersPer != null) {
                _teams.add(new Team(teamName, displayName, color, probability, playersBefore, playersPer, canKillTeammates));
            }
        }
    }

    public void loadRoles() {
        List<String> roleNames = this.getKeys("roles");
        for (String roleName : roleNames) {
            String displayName = this.getString("roles", roleName, "name");
            Integer probability = this.getInt("roles", roleName, "probability");
            Integer maxCount = this.getInt("roles", roleName, "maxCount");
            Weapon weapon = this.getWeapon(this.getString("roles", roleName, "weapon"));
            Team team = this.getTeam(this.getString("roles", roleName, "team"));
            if (displayName != null && team != null && probability != null) {
                _roles.add(new Role(roleName, displayName, team, probability, maxCount, weapon));
            }
        }
    }

    public void loadMaps() {
        for (String mapName : this.getKeys("maps")) {
            String displayName = this.getString("maps", mapName, "name");
            Integer probability = this.getInt("maps", mapName, "probability");
            String regionName = this.getString("maps", mapName, "region");
            if (regionName != null) {
                ProtectedRegion region = this.getRegion(regionName);
                if (displayName != null && probability != null && region != null) {
                    _maps.add(new Map(mapName, displayName, probability, region, _world));
                }
            }
        }
    }

    public void loadWorldGuard() {
        _regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(_world));
    }

    public void loadWorld() {
        _world = Bukkit.getServer().getWorld(this.getString("world"));
    }

    public void debug(String msg) {
        if (_debug) {
            Bukkit.getLogger().info("[MurderParty DEBUG] " + msg);
        }
    }
}
