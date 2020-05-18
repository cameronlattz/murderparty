package com.cameronlattz.murderparty.models;

import java.util.Arrays;
import java.util.List;

public class Team {
    private String _name;
    private String _displayName;
    private int _probability;
    private int _playersBeforeSpawn;
    private int _playersPerSpawn;

    public Team(String name, String displayName, int probability, int playersBeforeSpawn, int playersPerSpawn) {
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

    public static List<String> getOptions() {
        return Arrays.asList("Name", "Probability");
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
