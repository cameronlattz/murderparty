package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import com.cameronlattz.murderparty.models.Map;
import me.libraryaddict.disguise.disguisetypes.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MurderParty extends JavaPlugin implements Listener {
    Configuration _configuration;
    Map _map;
    Map _lobby;
    List<MurderPartyPlayer> _players = new ArrayList<MurderPartyPlayer>();
    LinkedHashMap<Entity, MurderPartyPlayer> _bodies = new LinkedHashMap<Entity, MurderPartyPlayer>();
    BukkitTask _task;

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
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {{
        Entity entity = event.getRightClicked();
        if (entity.getType() == EntityType.ARMOR_STAND) {
            for (MurderPartyPlayer mpPlayer : _players) {
                if (mpPlayer.getPlayer() == event.getPlayer()) {
                    for (Ability ability : mpPlayer.getRole().getAbilities())
                        switch (ability) {
                            case TRACKING:
                                if (_bodies.containsValue(entity)) {
                                    ability.setCompassTarget(mpPlayer.getPlayer(), entity.getLocation());
                                }
                        }
                }
            }
        }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        MurderPartyPlayer damager = this.getPlayer(e.getDamager());
        MurderPartyPlayer victim = this.getPlayer(e.getEntity());
        if (damager != null && victim != null) {
            Weapon weapon = _configuration.getWeapon(damager.getPlayer().getInventory().getItemInMainHand());
            _configuration.debug("material: " + weapon.getItemStack().getType().getKey().getKey());
            if (weapon != null && weapon.canDamage()) {
                if (!damager.getRole().getTeam().canKillTeammates() && damager.getRole().getTeam() == victim.getRole().getTeam()) {
                    this.killPlayer(damager, damager);
                    this.killPlayer(victim, damager);
                }
                if(victim.getPlayer().getHealth() < 1) {
                    if (victim.getRole().getAbilities().contains(Ability.SUICIDAL)) {
                        for (MurderPartyPlayer player : _players) {
                            if (player.isAlive() && player != victim) {
                                this.killPlayer(player, victim);
                            }
                        }
                    } else {
                        this.killPlayer(victim, damager);
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        if(event.getEntity() instanceof Arrow){
            Arrow arrow = (Arrow)event.getEntity();
            arrow.remove();
        }
    }

    /*@EventHandler
    public void onTeleportEvent(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            boolean isMurderPartyPlayer = false;
            for (MurderPartyPlayer mpPlayer : _players) {
                Location playerLocation = mpPlayer.getPlayer().getLocation();
                Location teleportLocation = e.getTo();
                if (playerLocation.getBlock() == teleportLocation.getBlock()) {
                    isMurderPartyPlayer = true;
                    break;
                }
            }
            e.setCancelled(isMurderPartyPlayer);
        }
    }*/

    public void load() {
        reloadConfig();
        _configuration = new Configuration(this);
        _lobby = new Map(null, null, null, _configuration.getLobbyRegion(), _configuration.getWorld(), null, null);
    }

    public void startGame() {
        this.startGame(null);
    }

    public void startGame(Map mapIn) {
        Map map = this.chooseMap(mapIn);
        _configuration.debug(map.getName());
        List<Player> players = _configuration.getPlayers(_configuration.getLobbyRegion());
        if (players.size() > 0) {
            _map = map;
            Collections.shuffle(players);
            for (int i = 0; i < players.size(); i++){
                Player player = players.get(i);
                Team team = this.chooseTeam(players, i);
                Role role = this.chooseRole(team);
                _players.add(this.createMpPlayer(player, role));
                String message = "YOU ARE A " + role.getTeam().getColor() + role.getName().toUpperCase() + ChatColor.RESET + "!";
                this.sendTitle(player, message, null, 50);
                player.sendMessage(message);
            }
            this.spawnPlayers(_players, _map);
            _task = new GameRunnable(this, _configuration, _map, _players).runTaskTimer(this, 0, 1);
        }
    }

    public void endGame(Team winningTeam) {
        _task.cancel();
        List<Team> winningTeams = this.getNotEmptyTeams();
        List<String> roles = new ArrayList<String>();
        for (Team team : _configuration.getTeams()) {
            for (MurderPartyPlayer mpPlayer : _players) {
                if (mpPlayer.getRole().getTeam() == team) {
                    roles.add(mpPlayer.getRole().getTeam().getColor() + mpPlayer.getRole().getDisplayName() + ": " + mpPlayer.getPlayer().getDisplayName() + ChatColor.RESET);
                }
            }
        }
        this.spawnPlayers(_players, _lobby);
        for (MurderPartyPlayer mpPlayer : _players) {
            Player player = mpPlayer.getPlayer();
            this.removeSpectator(player);
            String outcome = "YOU " + (winningTeam == mpPlayer.getRole().getTeam() ? ChatColor.GREEN + "WON!" : ChatColor.RED + "LOST.");
            if (winningTeam == null) {
                outcome = ChatColor.AQUA + "GAME OVER";
            }
            this.sendTitle(player, outcome, StringUtils.join(roles, ", "), 50);
            player.sendMessage(outcome);
            player.sendMessage(StringUtils.join(roles, ", "));
            player.getInventory().clear();
            break;
        }
        _players = new ArrayList<MurderPartyPlayer>();
        for (java.util.Map.Entry<Entity, MurderPartyPlayer> entry : _bodies.entrySet()) {
            entry.getKey().remove();
        }
        List<Entity> entities = _configuration.getWorld().getEntities();
        for(Entity entity : entities) {
            if (entity instanceof Item && _map.containsLocation(entity.getLocation())) {
                entity.remove();
            }
        }
        _bodies = new LinkedHashMap<Entity, MurderPartyPlayer>();
        _map = null;
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

    public void spawnPlayers(List<MurderPartyPlayer> mpPlayers, Map map) {
        List<Location> spawnLocations = map.getSpawnLocations();
        for (MurderPartyPlayer mpPlayer : mpPlayers) {
            Location spawnLocation = spawnLocations.get((int)Math.floor(spawnLocations.size() * Math.random()));
            spawnLocations.remove(spawnLocation);
            Location aboveLocation = new Location(_configuration.getWorld(), spawnLocation.getBlockX() + 0.5, spawnLocation.getBlockY() + 2, spawnLocation.getBlockZ() + 0.5);
            mpPlayer.getPlayer().teleport(aboveLocation);
        }
    }

    public void killPlayer(MurderPartyPlayer victim, MurderPartyPlayer damager) {
        Player player = victim.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3, 255));
        this.sendTitle(player, ChatColor.RED + "You have been killed!", null, 40);
        Disguise disguise = new PlayerDisguise(player);
        FlagWatcher flagWatcher = disguise.getWatcher();
        flagWatcher.setSleeping(true);
        Entity entity = player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        entity.setGravity(false);
        disguise.setEntity(entity);
        disguise.startDisguise();
        this.setSpectator(player);
        player.getInventory().clear();
        _bodies.put(entity, damager);
        if (getNotEmptyTeams().size() == 1) {
            this.endGame(getNotEmptyTeams().get(0));
        }
    }

    public List<Team> getNotEmptyTeams() {
        List<Team> notEmptyTeams = new ArrayList<Team>();
        for (MurderPartyPlayer mpPlayer : _players) {
            if (mpPlayer.isAlive() && !notEmptyTeams.contains(mpPlayer.getRole().getTeam())) {
                notEmptyTeams.add(mpPlayer.getRole().getTeam());
            }
        }
        return notEmptyTeams;
    }

    public MurderPartyPlayer getPlayer(Entity e) {
        for (MurderPartyPlayer player : _players) {
            if (player.getPlayer().getEntityId() == e.getEntityId()) {
                return player;
            }
        }
        return null;
    }

    public void setSpectator(Player p) {
        if (p.getGameMode() == GameMode.SURVIVAL) {
            p.setGameMode(GameMode.ADVENTURE);
            p.setAllowFlight(true);
            p.setFlying(true);
            for(Player player: Bukkit.getOnlinePlayers()) {
                player.hidePlayer(this, p);
            }
        }
    }

    public void removeSpectator(Player p) {
        if (p.getGameMode() == GameMode.ADVENTURE) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setAllowFlight(false);
            p.setFlying(false);
            for(Player player: Bukkit.getOnlinePlayers()) {
                player.showPlayer(this, p);
            }
        }
    }

    public void sendTitle(Player player, String title, String subtitle, int duration) {
        player.sendTitle(title, subtitle, 20, duration, 20);
    }
}
