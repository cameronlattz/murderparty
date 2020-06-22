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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import scala.Int;

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
    List<Ammo> _ammos = new ArrayList<Ammo>();
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
        this.loadAmmos();
        this.loadWeapons();
        _weapons.add(new Weapon("nothing", null, Material.AIR, false, 0, null, null, null, null));
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

    public Ammo getAmmo(String ammoName) {
        for (Ammo ammoItem : _ammos) {
            if (ammoItem.getName().equals(ammoName)) {
                return ammoItem;
            }
        }
        return null;
    }

    public List<Ammo> getAmmos() { return _ammos; }

    public Weapon getWeapon(String name) {
        for (Weapon weapon : _weapons) {
            if (weapon.getName().equals(name)) {
                return weapon;
            }
        }
        return null;
    }

    public Weapon getWeapon(ItemStack itemStack) {
        for (Weapon weapon : _weapons) {
            if (weapon.getItemStack().isSimilar(itemStack)) {
                return weapon;
            }
        }
        return null;
    }

    public List<Weapon> getWeapons() {
        return _weapons;
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

    public List<String> getKeys(String... path) {
        ConfigurationSection configurationSection =  _configuration.getConfigurationSection(StringUtils.join(path, "."));
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
        if ((configurationSection != null && configurationSection.isConfigurationSection(value)) || (path == null && _configuration.isConfigurationSection(value))) {
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

    private void loadAmmos() {
        String key = "ammos";
        List<String> ammoNames = this.getKeys(key);
        for (String ammoName : ammoNames) {
            String materialName = this.getString(key, ammoName, "material");
            if (materialName != null) {
                Material material = Material.matchMaterial(materialName);
                Integer count = this.getInt(key, ammoName, "count");
                ItemStack itemStack = new ItemStack(material, count != null ? count : 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                String type = this.getString(key, ammoName, "type");
                if (type != null) {
                    if (material == Material.SPLASH_POTION) {
                        PotionMeta potionMeta = (PotionMeta)itemStack.getItemMeta();
                        String[] typeSplit = type.split(" ");
                        if (typeSplit.length == 3) {
                            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(typeSplit[0].toLowerCase()), Integer.parseInt(typeSplit[1]), Integer.parseInt(typeSplit[2]));
                            potionMeta.addCustomEffect(potionEffect, true);
                            itemStack.setItemMeta(potionMeta);
                        }
                    }
                }
                Integer max = this.getInt(key, ammoName, "max");
                Integer cooldown = this.getInt(key, ammoName, "cooldown");
                Integer dropProbability = this.getInt(key, ammoName, "dropProbability");
                _ammos.add(new Ammo(ammoName, itemStack, max, cooldown, dropProbability));
            }
        }
    }

    private void loadWeapons() {
        String key = "weapons";
        List<String> weaponNames = this.getKeys(key);
        for (String weaponName : weaponNames) {
            String displayName = this.getString(key, weaponName, "name");
            if (displayName != null && displayName.length() != 0) {
                String materialName = this.getString(key, weaponName, "material");
                Material material = Material.matchMaterial(materialName);
                boolean canDamage = this.getBoolean(true, key, weaponName, "canDamage");
                Integer dropProbability = this.getInt(key, weaponName, "dropProbability");
                String loreListString = this.getString(key, weaponName, "lore");
                List<String> lore = new ArrayList<String>();
                if (loreListString != null) {
                    lore = Arrays.asList(loreListString.split("\\\\n"));
                }
                String enchantmentListString = this.getString(key, weaponName, "enchantments");
                LinkedHashMap<Enchantment, Integer> enchantments = new LinkedHashMap<Enchantment, Integer>();
                if (enchantmentListString != null) {
                    List<String> enchantmentStrings = Arrays.asList(enchantmentListString.split(", "));
                    for (String enchantmentString: enchantmentStrings) {
                        int spaceIndex = enchantmentString.indexOf(" ");
                        String enchantmentName = enchantmentString.substring(0, spaceIndex).toLowerCase();
                        NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantmentName);
                        Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
                        int level = Integer.parseInt(enchantmentString.substring(spaceIndex + 1));
                        enchantments.put(enchantment, level);
                    }
                }
                String ammoNames = this.getString(key, weaponName, "ammo");
                List<Ammo> ammos = new ArrayList<Ammo>();
                if (ammoNames != null) {
                    for (String ammoName : ammoNames.split(" ,")) {
                        Ammo ammo = this.getAmmo(ammoName);
                        if (ammo != null) {
                            ammos.add(ammo);
                        }
                    }
                }
                List<Ability> abilities = new ArrayList<Ability>();
                String abilityNamesString = this.getString(key, weaponName, "abilities");
                if (abilityNamesString != null) {
                    for (String abilityName : abilityNamesString.split(", ")) {
                        Ability ability = Ability.getByName(abilityName);
                        if (ability != null) {
                            abilities.add(ability);
                        }
                    }
                }
                _weapons.add(new Weapon(weaponName, displayName, material, canDamage, dropProbability, lore, enchantments, ammos, abilities));
            }
        }
    }

    private void loadTeams() {
        String key = "teams";
        List<String> teamNames = this.getKeys(key);
        for (String teamName : teamNames) {
            String displayName = this.getString(key, teamName, "name");
            String color = this.getString(key, teamName, "color");
            Integer probability = this.getInt(key, teamName, "probability");
            Integer playersBefore = this.getInt(key, teamName, "playersBeforeSpawn");
            Integer playersPer = this.getInt(key, teamName, "playersPerSpawn");
            boolean canKillTeammates = this.getBoolean(true, key, teamName, "canKillTeammates");
            boolean canSeeTeammates = this.getBoolean(false, key, teamName, "canSeeTeammates");
            if (displayName != null && color != null &&  probability != null && playersBefore != null && playersPer != null) {
                _teams.add(new Team(teamName, displayName, color, probability, playersBefore, playersPer, canKillTeammates, canSeeTeammates));
            }
        }
    }

    public void loadRoles() {
        String key = "roles";
        List<String> roleNames = this.getKeys(key);
        for (String roleName : roleNames) {
            String displayName = this.getString(key, roleName, "name");
            Integer probability = this.getInt(key, roleName, "probability");
            Integer maxCount = this.getInt(key, roleName, "max");
            String weaponListString = this.getString(key, roleName, "weapons");
            List<Weapon> weapons = new ArrayList<Weapon>();
            if (weaponListString != null) {
                for (String weaponName : weaponListString.split(", ")) {
                    Weapon weapon = this.getWeapon(weaponName);
                    if (weapon != null) {
                        weapons.add(weapon);
                    }
                }
            }
            Team team = this.getTeam(this.getString(key, roleName, "team"));
            List<Ability> abilities = new ArrayList<Ability>();
            String abilityNamesString = this.getString(key, roleName, "abilities");
            if (abilityNamesString != null) {
                for (String abilityName : abilityNamesString.split(", ")) {
                    Ability ability = Ability.getByName(abilityName);
                    if (ability != null) {
                        abilities.add(ability);
                    }
                }
            }
            if (displayName != null && team != null && probability != null) {
                _roles.add(new Role(roleName, displayName, team, probability, maxCount, weapons, abilities));
            }
        }
    }

    public void loadMaps() {
        String key = "maps";
        for (String mapName : this.getKeys(key)) {
            String displayName = this.getString(key, mapName, "name");
            Integer probability = this.getInt(key, mapName, "probability");
            Integer ticksPerAmmoDrop = this.getInt(key, mapName, "ticksPerAmmoDrop");
            Integer ticksPerWeaponDrop = this.getInt(key, mapName, "ticksPerWeaponDrop");
            String regionName = this.getString(key, mapName, "region");
            if (regionName != null) {
                ProtectedRegion region = this.getRegion(regionName);
                if (displayName != null && probability != null && region != null) {
                    _maps.add(new Map(mapName, displayName, probability, region, _world, ticksPerAmmoDrop, ticksPerWeaponDrop));
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
