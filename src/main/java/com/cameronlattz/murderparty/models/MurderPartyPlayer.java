package com.cameronlattz.murderparty.models;

import org.bukkit.entity.Player;

public class MurderPartyPlayer {
    private Player _player;
    private Role _role;

    public MurderPartyPlayer(Player player, Role role) {
        _player = player;
        _role = role;
        this.setWeapon(role.getWeapon());
    }

    public Player getPlayer() { return _player; }

    public void setWeapon(Weapon weapon) { _player.getInventory().setItem(1, weapon.getItemStack()); }
}
