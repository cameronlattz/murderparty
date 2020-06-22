package com.cameronlattz.murderparty.models;

import org.bukkit.inventory.ItemStack;

public class Ammo {
    private String _name;
    private ItemStack _itemStack;
    private int _max;
    private Integer _cooldown;
    private int _dropProbability;

    public Ammo(String name, ItemStack itemStack, Integer max, Integer cooldown, Integer dropProbability) {
        _name = name;
        _itemStack = itemStack;
        _max = max != null ? max : 0;
        _cooldown = cooldown;
        _dropProbability = dropProbability != null ? dropProbability : 0;
    }

    public String getName() { return _name; }

    public ItemStack getItemStack() { return _itemStack; }

    public int getMax() { return _max; }

    public Integer getCooldown() { return _cooldown; }

    public int getDropProbability() { return _dropProbability; }
}
