package com.cameronlattz.murderparty.models;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Team implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Integer _probability;
    private Integer _playersBeforeSpawn;
    private Integer _playersPerSpawn;
    private ChatColor _color;
    private boolean _canKillTeammates;

    public Team(String name, String displayName, String color, Integer probability, Integer playersBeforeSpawn, Integer playersPerSpawn, boolean canKillTeammates) {
        _name = name;
        _displayName = displayName;
        _probability = probability;
        _playersBeforeSpawn = playersBeforeSpawn;
        _playersPerSpawn = playersPerSpawn;
        _color = ChatColor.valueOf(color.toUpperCase());
        _canKillTeammates = canKillTeammates;
    }

    public boolean canKillTeammates() { return _canKillTeammates; }

    public String getName() { return _name; }

    public int getPlayersBeforeSpawn() { return _playersBeforeSpawn; }

    public int getPlayersPerSpawn() { return _playersPerSpawn; }

    public int getProbability() { return _probability; }

    public ChatColor getColor() { return _color; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  color: " + _color.name());
        info.add("  probability: " + _probability);
        info.add("  players before spawn: " + _playersBeforeSpawn);
        info.add("  players per spawn: " + _playersPerSpawn);
        return info;
    }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("color", "string");
            put("probability", "integer");
            put("playersBeforeSpawn", "integer");
            put("playersPerSpawn", "integer");
        }};
    }

    public static Team getByName(List<Team> teams, String name) {
        for (Team team : teams) {
            if (team.getName() == name) {
                return team;
            }
        }
        return null;
    }
}
