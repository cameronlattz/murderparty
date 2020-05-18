package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class MurderParty extends JavaPlugin {
    Configuration _configuration;
    boolean _running;
    Map _map;

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

    public void load() {
        _configuration = new Configuration(this);
    }

    public void startGame() {
        this.startGame(null);
    }

    public void startGame(Map mapIn) {
        _running = true;
        if (mapIn != null) {
            _map = mapIn;
        }
        else {
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
        // Iterate through a list of shuffled players in spawn region
        List<Player> playersInWorld = _configuration.getWorld().getPlayers();
        ProtectedRegion lobbyRegion = _configuration.getRegion(_configuration.getLobbyRegionName());
        List<Player> players = _configuration.getPlayers(lobbyRegion);
        Collections.shuffle(players);
        List<Location> spawnLocations = _map.getSpawnLocations();
        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            // Iterate through the teams
            for (Team team : _configuration.getTeams()) {
                // If the minimum player count before spawn is reached, continue
                if (team.getPlayersBeforeSpawn() > i) {
                    int playersPer = team.getPlayersPerSpawn();
                    // If we are currently spawning this team
                    if (i % playersPer == 0) {
                        // The probability of spawning the team is 1 - the probability of not spawning it
                        // if every player has a chance after the minimum is reached
                        double notTeamProbability = (100 - team.getProbability())/100;
                        int exponent = players.size() - i;
                        if (exponent > playersPer) {
                            exponent = playersPer;
                        }
                        double teamProbability = 1 - Math.pow(notTeamProbability, exponent);
                        if (teamProbability >= Math.random()) {
                            List<Role> teamRoles = Role.getRolesInTeam(team, _configuration.getRoles());
                            int roleRandom = (int)Math.floor(Math.random() * 100);
                            int roleTotal = 0;
                            // Iterate through a list of roles in the team, so we can randomly choose one based
                            // on probabilities
                            for (Role teamRole : teamRoles) {
                                roleTotal += teamRole.getProbability();
                                if (roleTotal >= roleRandom) {
                                    MurderPartyPlayer murderPartyPlayer = new MurderPartyPlayer(players.get(i), teamRole);
                                    // Spawn the player and remove that spawn location from the list
                                    Location spawnLocation = spawnLocations.get((int)Math.floor(spawnLocations.size() * Math.random()));
                                    spawnLocations.remove(spawnLocation);
                                    murderPartyPlayer.getPlayer().teleport(spawnLocation);
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
        for (Player player : _configuration.getPlayers(_configuration.getRegion(_map.getRegionName()))) {
            ProtectedRegion lobbyRegion = _configuration.getRegion(_configuration.getLobbyRegionName());
            double x =  (lobbyRegion.getMaximumPoint().getX() + lobbyRegion.getMinimumPoint().getX())/2;
            double y =  (lobbyRegion.getMaximumPoint().getY() + lobbyRegion.getMinimumPoint().getY())/2;
            double z =  (lobbyRegion.getMaximumPoint().getZ() + lobbyRegion.getMinimumPoint().getZ())/2;
            Location center = new Location(_configuration.getWorld(), x + Math.random() * 5, y, z + Math.random() * 5);
            player.getPlayer().teleport(center);
        }
        _map = null;
        _running = false;
    }
}
