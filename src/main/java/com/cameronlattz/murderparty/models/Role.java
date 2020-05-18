package com.cameronlattz.murderparty.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Role {
    private String _name;
    private String _displayName;
    private Team _team;
    private int _probability;
    private int _maxCount;
    private Weapon _weapon;

    public Role(String name, String displayName, Team team, int probability, int maxCount, Weapon weapon) {
        _name = name;
        _displayName = displayName;
        _team = team;
        _probability = probability;
        _maxCount = maxCount;
        _weapon = weapon;
    }

    public String getName() { return _name; }

    public int getProbability() {
        return _probability;
    }

    public Team getTeam() {
        return _team;
    }

    public Weapon getWeapon() { return _weapon; }

    public String[] getInfo() {
        String[] info = new String[4];
        info[0] = "Name: " + _name;
        info[1] = "Team: " + _team;
        info[2] = "Probability: " + _probability;
        info[3] = "Weapon: " + _weapon.getName();
        return info;
    }

    public static List<String> getOptions() {
        return Arrays.asList("Name", "Team", "Probability", "Weapon");
    }

    public static List<Role> getRolesInTeam(Team team, List<Role> roles) {
        List<Role> teamRoles = new ArrayList<Role>();
        for (Role role : roles) {
            if (role.getTeam() == team) {
                teamRoles.add(role);
            }
        }
        return teamRoles;
    }
}
