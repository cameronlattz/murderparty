package com.cameronlattz.murderparty.models;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

public class MurderPartyPlayer {
    private Player _player;
    private Role _role;

    public MurderPartyPlayer(Player player, Role role) {
        _player = player;
        _role = role;
        List<Weapon> weapons = role.getWeapons();
        player.getInventory().clear();
        for (int i = 0; i < weapons.size(); i++) {
            this.setWeapon(weapons.get(i), i+1);
        }
    }

    public Player getPlayer() { return _player; }

    public Role getRole() { return _role; }

    public void setWeapon(Weapon weapon, int slot) {
        _player.getInventory().setItem(slot, weapon.getItemStack());
    }

    public boolean isAlive() {
        return _player.getGameMode() == GameMode.SURVIVAL;
    }
}
