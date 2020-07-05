package com.cameronlattz.murderparty.models;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Role implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Team _team;
    private Integer _probability;
    private Integer _maxCount;
    private List<Weapon> _weapons = new ArrayList<Weapon>();
    private List<Ability> _abilities;

    public Role(String name, String displayName, Team team, Integer probability, Integer maxCount, List<Weapon> weapons, List<Ability> abilities) {
        _name = name;
        _displayName = displayName;
        _team = team;
        _probability = probability;
        _maxCount = maxCount;
        _weapons = weapons;
        _abilities = abilities;
    }

    public String getName() { return _name; }

    public String getDisplayName() { return _displayName; }

    public Integer getProbability() {
        return _probability;
    }

    public Team getTeam() {
        return _team;
    }

    public List<Weapon> getWeapons() { return _weapons; }

    public List<Ability> getAbilities() { return _abilities; }

    public Integer getMaxCount() { return _maxCount; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  team: " + _team.getName());
        info.add("  probability: " + _probability);
        info.add("  max count: " + _maxCount);
        for (Weapon weapon : _weapons) {
            info.add("  weapon: " + weapon.getName());
        }
        info.add("  abilities: " + StringUtils.join(_abilities.toArray(), ", "));
        return info;
    }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("team", "string");
            put("probability", "integer");
            put("maxCount", "integer");
            put("weapons", "string");
            put("abilities", "string");
        }};
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
