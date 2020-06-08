package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MurderParty extends JavaPlugin {
    Configuration _configuration;
    boolean _running;
    Map _map;
    List<MurderPartyPlayer> _players = new ArrayList<MurderPartyPlayer>();

    @Override
    public void onEnable() {
        this.load();
        getLogger().info("Murder Party enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Murder Party disabled.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return CommandHelper.onTabComplete(_configuration, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return CommandHelper.onCommand(this, _configuration, sender, args);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        MurderPartyPlayer damager = null;
        MurderPartyPlayer victim = null;
        for (MurderPartyPlayer player : _players) {
            if (player.getPlayer().getEntityId() == e.getDamager().getEntityId()) {
                damager = player;
            } else if (player.getPlayer().getEntityId() == e.getEntity().getEntityId()) {
                victim = player;
            }
        }
        if (!damager.getTeam().canKillTeammates() && damager.getTeam() == victim.getTeam()) {
            e.setCancelled(true);
            damager.getPlayer().setHealth(0);
        }
    }

    public void load() {
        _configuration = new Configuration(this);
    }

    public void startGame() {
        this.startGame(null);
    }

    public void startGame(Map mapIn) {
        _running = true;
        _map = this.chooseMap(mapIn);
        // Iterate through a list of shuffled players in spawn region
        List<Player> players = _configuration.getPlayers(_configuration.getLobbyRegion());
        Collections.shuffle(players);
        List<Location> spawnLocations = _map.getSpawnLocations();
        List<Location> usedSpawnLocations = new ArrayList<Location>();
        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            // Iterate through the teams
            /*Team teamT = this.chooseTeam(players, i);
            Role roleR = this.chooseRole(teamT);
            MurderPartyPlayer mpPlayer = this.addPlayer(player, roleR);
            this.spawnPlayers(); // outside loop*/
            teams:
            for (Team team : _configuration.getTeams()) {
                getLogger().info("iterated team: " + team.getName());
                // If the minimum player count before spawn is reached, continue
                int playersBefore = team.getPlayersBeforeSpawn();
                getLogger().info("players before spawn: " + team.getPlayersBeforeSpawn());
                int playersPer = team.getPlayersPerSpawn();
                if (playersBefore - 1 <= i) {
                    getLogger().info("players per spawn: " + playersPer);
                    // If we are currently spawning this team
                    if (i == playersBefore - 1 || i % playersPer == 0) {
                        // The probability of spawning the team is 1 - the probability of not spawning it
                        // if every player has a chance after the minimum is reached
                        double notTeamProbability = (100 - team.getProbability())/100;
                        int exponent = players.size() - i;
                        if (exponent > playersPer) {
                            exponent = playersPer;
                        }
                        double teamProbability = 1 - Math.pow(notTeamProbability, exponent);
                        getLogger().info("team probability: " + teamProbability);
                        if (teamProbability >= Math.random()) {
                            int roleRandom = (int)Math.floor(Math.random() * 100);
                            getLogger().info("role random: " + roleRandom);
                            int roleTotal = 0;
                            // Iterate through a list of roles in the team, so we can randomly choose one based
                            // on probabilities
                            for (Role teamRole : _configuration.getRolesInTeam(team)) {
                                int currentCount = 0;
                                for (MurderPartyPlayer mpPlayer : _players) {
                                    if (mpPlayer.getRole() == teamRole) {
                                        currentCount++;
                                    }
                                }
                                getLogger().info("iterated role: " + teamRole.getName());
                                getLogger().info(currentCount + " < " + teamRole.getMaxCount());
                                if (teamRole.getMaxCount() == null || currentCount < teamRole.getMaxCount()) {
                                    roleTotal += teamRole.getProbability();
                                    getLogger().info("role total: " + teamRole.getProbability());
                                    if (roleTotal >= roleRandom) {
                                        _players.add(new MurderPartyPlayer(player, teamRole));
                                        getLogger().info("player: " + player.getDisplayName());
                                        getLogger().info("mp player: " + _players.get(_players.size() - 1).getPlayer().getDisplayName());
                                        // Spawn the player and remove that spawn location from the list
                                        List<Location> availableLocations = spawnLocations;
                                        availableLocations.removeAll(usedSpawnLocations);
                                        Location spawnLocation = availableLocations.get((int)Math.floor(availableLocations.size() * Math.random()));
                                        usedSpawnLocations.add(spawnLocation);
                                        Location aboveLocation = new Location(_configuration.getWorld(), spawnLocation.getBlockX() + 0.5, spawnLocation.getBlockY() + 2, spawnLocation.getBlockZ() + 0.5);
                                        player.teleport(aboveLocation);
                                        break teams;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void endGame() {
        // Teleport players back to center of the lobby
        for (Player player : _configuration.getPlayers(_map.getRegion())) {
            ProtectedRegion lobbyRegion = _configuration.getLobbyRegion();
            double x =  (lobbyRegion.getMaximumPoint().getX() + lobbyRegion.getMinimumPoint().getX())/2;
            double y =  (lobbyRegion.getMaximumPoint().getY() + lobbyRegion.getMinimumPoint().getY())/2;
            double z =  (lobbyRegion.getMaximumPoint().getZ() + lobbyRegion.getMinimumPoint().getZ())/2;
            Location center = new Location(_configuration.getWorld(), x + Math.random() * 10 - 5, y, z + Math.random() * 10 - 5);
            player.getInventory().clear();
            player.teleport(center);
        }
        _players = new ArrayList<MurderPartyPlayer>();
        _map = null;
        _running = false;
    }

    public Map chooseMap(Map mapIn) {
        Map selectedMap = null;
        if (mapIn != null) {
            selectedMap = mapIn;
        } else {
            List<Map> maps = _configuration.getMaps();
            // Randomly select a map
            int total = 0;
            int random = (int)Math.floor(Math.random() * 100);
            for (Map map : maps) {
                total += map.getProbability();
                if (total >= random) {
                    _map = map;
                }
            }
        }
        return selectedMap;
    }

    public Team chooseTeam(List<Player> players, int index) {
        for (Team team : _configuration.getTeams()) {
            getLogger().info("iterated team: " + team.getName());
            // If the minimum player count before spawn is reached, continue
            int playersBefore = team.getPlayersBeforeSpawn();
            getLogger().info("players before spawn: " + team.getPlayersBeforeSpawn());
            int playersPer = team.getPlayersPerSpawn();
            if (playersBefore - 1 <= index) {
                getLogger().info("players per spawn: " + playersPer);
                // If we are currently spawning this team
                if (index == playersBefore - 1 || index % playersPer == 0) {
                    // The probability of spawning the team is 1 - the probability of not spawning it
                    // if every player has a chance after the minimum is reached
                    double notTeamProbability = (100 - team.getProbability())/100;
                    int exponent = players.size() - index;
                    if (exponent > playersPer) {
                        exponent = playersPer;
                    }
                    double teamProbability = 1 - Math.pow(notTeamProbability, exponent);
                    getLogger().info("team probability: " + teamProbability);
                    if (teamProbability >= Math.random()) {
                        return team;
                    }
                }
            }
        }
        return null;
    }

    public Role chooseRole(Team team) {
        int roleRandom = (int)Math.floor(Math.random() * 100);
        int roleTotal = 0;
        for (Role teamRole : _configuration.getRolesInTeam(team)) {
            int currentCount = 0;
            for (MurderPartyPlayer mpPlayer : _players) {
                if (mpPlayer.getRole() == teamRole) {
                    currentCount++;
                }
            }
            getLogger().info("iterated role: " + teamRole.getName());
            getLogger().info(currentCount + " < " + teamRole.getMaxCount());
            if (teamRole.getMaxCount() == null || currentCount < teamRole.getMaxCount()) {
                roleTotal += teamRole.getProbability();
                getLogger().info("role total: " + teamRole.getProbability());
                if (roleTotal >= roleRandom) {
                    return teamRole;
                }
            }
        }
        return null;
    }

    public MurderPartyPlayer addPlayer(Player player, Role role) {
        MurderPartyPlayer mpPlayer = new MurderPartyPlayer(player, role);
        _players.add(mpPlayer);
        return mpPlayer;
    }

    public boolean spawnPlayers() {
        List<Location> spawnLocations = _map.getSpawnLocations();
        for (MurderPartyPlayer mpPlayer : _players) {
            Location spawnLocation = spawnLocations.get((int)Math.floor(spawnLocations.size() * Math.random()));
            spawnLocations.remove(spawnLocation);
            Location aboveLocation = new Location(_configuration.getWorld(), spawnLocation.getBlockX() + 0.5, spawnLocation.getBlockY() + 2, spawnLocation.getBlockZ() + 0.5);
            mpPlayer.getPlayer().teleport(aboveLocation);
        }
    }
}
