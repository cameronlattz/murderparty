package com.cameronlattz.murderparty.models;

import org.bukkit.entity.Player;

public class MurderPartyPlayer {
    private Player _player;
    private Role _role;

    public MurderPartyPlayer(Player player, Role role) {
        _player = player;
        _role = role;
        Weapon weapon = role.getWeapon();
        player.getInventory().clear();
        if (weapon != null) {
            this.setWeapon(role.getWeapon());
        }
        player.sendTitle("You are a " + role.getColor() + role.getName(), "", 20, 40, 20);
    }

    public Player getPlayer() { return _player; }

    public Team getTeam() { return _role.getTeam(); }

    public Role getRole() { return _role; }

    public void setWeapon(Weapon weapon) { _player.getInventory().setItem(1, weapon.getItemStack()); }
}
