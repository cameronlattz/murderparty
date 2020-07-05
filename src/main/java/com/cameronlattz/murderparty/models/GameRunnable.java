package com.cameronlattz.murderparty.models;

import com.cameronlattz.murderparty.Configuration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameRunnable extends BukkitRunnable {
    private final JavaPlugin _plugin;
    private final Configuration _configuration;
    private final Map _map;
    private final List<MurderPartyPlayer> _players;
    private final List<Ammo> _droppableAmmos;
    private final List<Weapon> _droppableWeapons;
    private static int _ticksPerAmmoStock = 20;
    private int _iteration = 0;

    public GameRunnable(JavaPlugin plugin, Configuration configuration, Map map, List<MurderPartyPlayer> players) {
        _plugin = plugin;
        _configuration = configuration;
        _map = map;
        _players = Collections.unmodifiableList(players);
        List<Ammo> droppableAmmos = new ArrayList<Ammo>();
        for (Ammo ammo : _configuration.getAmmos()) {
            if (ammo.getDropProbability() > 0) {
                droppableAmmos.add(ammo);
            }
        }
        _droppableAmmos = Collections.unmodifiableList(droppableAmmos);
        List<Weapon> droppableWeapon = new ArrayList<Weapon>();
        for (Weapon weapon : _configuration.getWeapons()) {
            if (weapon.getDropProbability() > 0) {
                droppableWeapon.add(weapon);
            }
        }
        _droppableWeapons = Collections.unmodifiableList(droppableWeapon);
    }

    public void run() {
        for (MurderPartyPlayer player : _players) {
            if (player.getPlayer().isOnline() && player.isAlive()) {
                checkAbilities(player);
                if (_iteration % _ticksPerAmmoStock == 0) {
                    checkAmmo(player.getPlayer());
                }
                if (_map.getTicksPerAmmoDrop() != 0 && _iteration % _map.getTicksPerAmmoDrop() == 0) {
                    //dropAmmo((int)Math.floor(_players.size()));
                }
                if (_map.getTicksPerWeaponDrop() != 0 && _iteration % _map.getTicksPerWeaponDrop() == 0) {
                    //dropWeapon((int)Math.floor(_players.size()));
                }
            }
        }
        _iteration++;
    }

    public void checkAmmo(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        for (ItemStack itemStack : inventory) {
            if (itemStack != null) {
                Weapon weapon = _configuration.getWeapon(itemStack);
                if (weapon != null) {
                    List<Ammo> ammos = weapon.getAmmos();
                    for (Ammo ammo: ammos) {
                        ItemStack ammoItemStack = ammo.getItemStack();
                        int ammoCount = 0;
                        for (ItemStack inventoryItemStack : inventory) {
                            if (inventoryItemStack != null && inventoryItemStack.getType() == ammoItemStack.getType()) {
                                ammoCount += inventoryItemStack.getAmount();
                            }
                        }
                        if (ammo.getMax() > ammoCount) {
                            if (ammo.getCooldown() != null && _iteration % (ammo.getCooldown() * _ticksPerAmmoStock) == 0) {
                                player.getInventory().addItem(ammo.getItemStack());
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkAbilities(MurderPartyPlayer mpPlayer) {
        for (Ability ability : mpPlayer.getRole().getAbilities()) {
            ItemStack mainHand = mpPlayer.getPlayer().getInventory().getItemInMainHand();
            if (mainHand != null) {
                Weapon weapon = _configuration.getWeapon(mainHand);
                if (weapon != null && weapon.getAbilities().contains(ability)) {
                    switch (ability) {
                        case TRACKING:
                            ability.setCompassTarget(mpPlayer.getPlayer(), ability.getCompassTarget());
                            break;
                    }
                }
            }
        }
    }

    public void spawnAmmo(int playerCount) {
        Location randomLocation = _map.getDropLocations().get((int)Math.floor(Math.random() * _map.getDropLocations().size()));
        Ammo randomAmmo = _droppableAmmos.get((int)Math.floor(Math.random() * _droppableAmmos.size()));
        randomLocation.getWorld().dropItem(randomLocation, randomAmmo.getItemStack());
    }

    public void spawnWeapon(int playerCount) {
        Location randomLocation = _map.getDropLocations().get((int)Math.floor(Math.random() * _map.getDropLocations().size()));
        Weapon randomWeapon = _droppableWeapons.get((int)Math.floor(Math.random() * _droppableAmmos.size()));
        randomLocation.getWorld().dropItem(randomLocation, randomWeapon.getItemStack());
    }
}