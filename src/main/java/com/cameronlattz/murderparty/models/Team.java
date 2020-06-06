package com.cameronlattz.murderparty.models;

import java.util.LinkedHashMap;
import java.util.List;

public class Team {
    private String _name;
    private String _displayName;
    private Integer _probability;
    private Integer _playersBeforeSpawn;
    private Integer _playersPerSpawn;

    public Team(String name, String displayName, Integer probability, Integer playersBeforeSpawn, Integer playersPerSpawn) {
        _name = name;
        _displayName = displayName;
        _probability = probability;
        _playersBeforeSpawn = playersBeforeSpawn;
        _playersPerSpawn = playersPerSpawn;
    }

    public String getName() { return _name; }

    public int getPlayersBeforeSpawn() { return _playersBeforeSpawn; }

    public int getPlayersPerSpawn() { return _playersPerSpawn; }

    public int getProbability() { return _probability; }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("probability", "integer");
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
