package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import me.libraryaddict.disguise.disguisetypes.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MurderParty extends JavaPlugin implements Listener {
    Configuration _configuration;
    boolean _running;
    Map _map;
    Map _lobby;
    List<MurderPartyPlayer> _players = new ArrayList<MurderPartyPlayer>();
    List<Entity> _bodies = new ArrayList<Entity>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.load();
        _configuration.debug("Murder Party enabled.");
    }

    @Override
    public void onDisable() {
        _configuration.debug("Murder Party disabled.");
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
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        e.setCancelled(true);
        MurderPartyPlayer damager = null;
        MurderPartyPlayer victim = null;
        for (MurderPartyPlayer player : _players) {
            if (player.getPlayer().getEntityId() == e.getDamager().getEntityId()) {
                damager = player;
            } else if (player.getPlayer().getEntityId() == e.getEntity().getEntityId()) {
                victim = player;
            }
        }
        Weapon weapon = _configuration.getWeapon(damager.getWeaponMaterial());
        _configuration.debug("material: " + damager.getWeaponMaterial().getKey().getKey());
        if (weapon != null) {
            if (!damager.getTeam().canKillTeammates() && damager.getTeam() == victim.getTeam()) {
                this.killPlayer(damager.getPlayer());
                this.killPlayer(victim.getPlayer());
            }
        }
    }

    @EventHandler
    public void onTeleportEvent(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            e.setCancelled(true);
        }
    }

    public void load() {
        reloadConfig();
        _configuration = new Configuration(this);
        _lobby = new Map(null, null, null, _configuration.getLobbyRegion(), _configuration.getWorld());
    }

    public void startGame() {
        this.startGame(null);
    }

    public void startGame(Map mapIn) {
        _running = true;
        _map = this.chooseMap(mapIn);
        _configuration.debug(_map.getName());
        List<Player> players = _configuration.getPlayers(_configuration.getLobbyRegion());
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++){
            Player player = players.get(i);
            Team team = this.chooseTeam(players, i);
            Role role = this.chooseRole(team);
            _players.add(this.createMpPlayer(player, role));
        }
        this.teleportPlayers(_players, _map);
    }

    public void endGame() {
        List<Player> players = _configuration.getPlayers(_map.getRegion());
        List<MurderPartyPlayer> mpPlayers = new ArrayList<MurderPartyPlayer>();
        for (Player player : players) {
            for (MurderPartyPlayer mpPlayer : _players) {
                if (mpPlayer.getPlayer().getEntityId() == player.getEntityId()) {
                    mpPlayers.add(mpPlayer);
                    player.getInventory().clear();
                    break;
                }
            }
        }
        this.teleportPlayers(mpPlayers, _lobby);
        for (Entity entity : _bodies) {
            entity.remove();
        }
        _players = new ArrayList<MurderPartyPlayer>();
        _map = null;
        _running = false;
    }

    public Map chooseMap(Map mapIn) {
        _configuration.debug("no map chosen");
        List<Map> maps = _configuration.getMaps();
        // Randomly select a map
        int total = 0;
        int random = (int)Math.floor(Math.random() * 100);
        _configuration.debug("map random: " + random);
        for (Map map : maps) {
            total += map.getProbability();
            _configuration.debug("map total: " + total);
            if (total >= random) {
                return map;
            }
        }
        return mapIn != null ? mapIn : null;
    }

    public Team chooseTeam(List<Player> players, int index) {
        for (Team team : _configuration.getTeams()) {
            _configuration.debug("iterated team: " + team.getName());
            // If the minimum player count before spawn is reached, continue
            int playersBefore = team.getPlayersBeforeSpawn();
            _configuration.debug("players before spawn: " + team.getPlayersBeforeSpawn());
            int playersPer = team.getPlayersPerSpawn();
            if (playersBefore - 1 <= index) {
                _configuration.debug("players per spawn: " + playersPer);
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
                    _configuration.debug("team probability: " + teamProbability);
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
            _configuration.debug("iterated role: " + teamRole.getName());
            _configuration.debug(currentCount + " < " + teamRole.getMaxCount());
            if (teamRole.getMaxCount() == null || currentCount < teamRole.getMaxCount()) {
                roleTotal += teamRole.getProbability();
                _configuration.debug("role total: " + teamRole.getProbability());
                if (roleTotal >= roleRandom) {
                    return teamRole;
                }
            }
        }
        return null;
    }

    public MurderPartyPlayer createMpPlayer(Player player, Role role) {
        _configuration.debug(player.getName());
        MurderPartyPlayer mpPlayer = new MurderPartyPlayer(player, role);
        return mpPlayer;
    }

    public boolean teleportPlayers(List<MurderPartyPlayer> players, Map map) {
        List<Location> spawnLocations = map.getSpawnLocations();
        boolean spawned = false;
        for (MurderPartyPlayer player : players) {
            Location spawnLocation = spawnLocations.get((int)Math.floor(spawnLocations.size() * Math.random()));
            spawnLocations.remove(spawnLocation);
            Location aboveLocation = new Location(_configuration.getWorld(), spawnLocation.getBlockX() + 0.5, spawnLocation.getBlockY() + 2, spawnLocation.getBlockZ() + 0.5);
            player.getPlayer().teleport(aboveLocation);
            spawned = true;
        }
        return spawned;
    }

    public void killPlayer(Player player) {
        Disguise disguise = new PlayerDisguise(player);
        Entity entity = player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        FlagWatcher flagWatcher = disguise.getWatcher();
        flagWatcher.setSleeping(true);
        disguise.setEntity(entity);
        disguise.startDisguise();
        player.setGameMode(GameMode.SPECTATOR);
        _bodies.add(entity);
    }
}
