package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Configuration {
    public FileConfiguration _configuration;
    WorldGuardPlugin _worldGuard;
    World _world;
    List<Map> _maps = new ArrayList<Map>();
    List<Team> _teams = new ArrayList<Team>();
    List<Weapon> _weapons = new ArrayList<Weapon>();
    List<Role> _roles = new ArrayList<Role>();
    String _lobbyRegionName;

    public Configuration(MurderParty murderParty) {
        murderParty.saveDefaultConfig();
        _configuration = murderParty.getConfig();
        this.loadWorld();
        this.loadWorldGuard(murderParty.getServer());
        this.loadMaps();
        this.loadTeams();
        this.loadWeapons();
        this.loadRoles(_teams, _weapons);
        _lobbyRegionName = this.getString("lobby");
    }

    public World getWorld() { return _world; }

    public Map getMap(String name) {
        for (Map map : _maps) {
            if (map.getName() == name) {
                return map;
            }
        }
        return null;
    }

    public List<Map> getMaps() { return _maps; }

    public Role getRole(String name) {
        for (Role role : _roles) {
            if (role.getName() == name) {
                return role;
            }
        }
        return null;
    }

    public List<Role> getRoles() { return _roles; }

    public List<Team> getTeams() { return _teams; }

    public Weapon getWeapon(String name) {
        for (Weapon weapon : _weapons) {
            if (weapon.getName() == name) {
                return weapon;
            }
        }
        return null;
    }

    public List<Weapon> getWeapons() { return _weapons; }

    public RegionContainer getWorldGuardRegionContainer() {
        return WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    public String getLobbyRegionName() { return _lobbyRegionName; }

    public ProtectedRegion getRegion(String regionName) {
        return this.getWorldGuardRegionContainer().get(BukkitAdapter.adapt(_world)).getRegion(regionName);
    }

    public List<Player> getPlayers(ProtectedRegion region) {
        List<Player> playersInWorld = _world.getPlayers();
        List<Player> playersInRegion = new ArrayList<Player>();
        for (Player player : playersInWorld) {
            int x = player.getLocation().getBlockX();
            int y = player.getLocation().getBlockY();
            int z = player.getLocation().getBlockZ();
            if (region.contains(x, y, z)) {
                playersInRegion.add(player);
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
        return Integer.parseInt((String)value);
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

    public boolean getBoolean(String... keys) {
        Object value = this.getValue(keys);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean((String)value);
    }

    public void set(MurderParty murderParty, String path, String value) {
        _configuration.set(path, value);
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
        set(murderParty, path, null);
        return true;
    }

    private void loadWeapons() {
        List<String> weaponNames = this.getKeys("weapons");
        for (String weaponName : weaponNames) {
            String displayName = this.getString("weapons", weaponName, "display");
            Material material = Material.getMaterial(this.getString("weapons", weaponName, "material"));
            Boolean drops = this.getBoolean("weapons", weaponName, "drops");
            String lore = this.getString("weapons", weaponName, "lore");
            List<String> enchantmentStrings = this.getStringList("weapons", weaponName, "enchantments");
            LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>();
            for (String enchantmentString: enchantmentStrings) {
                int spaceIndex = enchantmentString.indexOf(" ");
                String enchantmentName = enchantmentString.substring(0, spaceIndex);
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));
                int level = Integer.parseInt(enchantmentString.substring(spaceIndex + 1));
                enchantments.put(enchantment, level);
            }
            _weapons.add(new Weapon(weaponName, displayName, material, drops, lore, enchantments));
        }
    }

    private void loadTeams() {
        List<String> teamNames = this.getKeys("teams");
        for (String teamName : teamNames) {
            String displayName = this.getString("teams", teamName, "display");
            int probability = this.getInt("teams", teamName, "probability");
            int playersBefore = this.getInt("teams", teamName, "playersBeforeSpawn");
            int playersPer = this.getInt("teams", teamName, "playersPerSpawn");
            _teams.add(new Team(teamName, displayName, probability, playersBefore, playersPer));
        }
    }

    public void loadRoles(List<Team> teams, List<Weapon> weapons) {
        List<String> roleNames = this.getKeys("roles");
        for (String roleName : roleNames) {
            String displayName = this.getString("roles", roleName, "display");
            Team team = Team.getByName(teams, this.getString("roles", roleName, "team"));
            //int probability = this.getInt("roles", roleName, "probability");
            int probability = 0;
            //int maxCount = this.getInt("roles", roleName, "maxCount");
            int maxCount = 0;
            String weaponName = this.getString("roles", roleName, "weapon");
            int weaponIndex = weapons.indexOf(weaponName);
            Weapon weapon = null;
            if (weaponIndex != -1) {
                weapon = weapons.get(weapons.indexOf(weaponName));
            }
            _roles.add(new Role(roleName, displayName, team, probability, maxCount, weapon));
        }
    }

    public void loadMaps() {
        List<String> mapNames = this.getKeys("maps");
        for (String mapName : mapNames) {
            String displayName = this.getString("maps", mapName, "display");
            Integer probability = this.getInt("maps", mapName, "probability");
            String regionName = this.getString("maps", mapName, "region");
            if (mapName != null && probability != null && regionName != null) {
                _maps.add(new Map(mapName, displayName, probability, regionName));
            }
        }
    }

    public void loadWorldGuard(Server server) {
        Plugin pl = server.getPluginManager().getPlugin("WorldGuard");
        if (pl != null && pl instanceof WorldGuardPlugin) {
            _worldGuard = (WorldGuardPlugin) pl;
        }
    }

    public void loadWorld() {
        _world = Bukkit.getServer().getWorld(this.getString("world"));
    }
}