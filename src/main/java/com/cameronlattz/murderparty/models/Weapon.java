package com.cameronlattz.murderparty.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Weapon {
    private String _name;
    private String _displayName;
    private Material _material;
    private boolean _drops;
    private String _lore;
    private LinkedHashMap<Enchantment, Integer> _enchantments;
    private Material _ammoMaterial;
    private int _ammoCooldown;

    public Weapon(String name, String displayName, Material material, boolean drops, String lore, LinkedHashMap<Enchantment, Integer> enchantments) {
        _name = name;
        _displayName = displayName;
        _material = material;
        _drops = drops;
        _lore = lore;
        _enchantments = enchantments;
    }

    public String getName() { return _name; }

    public String[] getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("Name: " + _name);
        info.add("Material: " + _material.name());
        info.add("Lore: " + _lore);
        info.add("Enchantments: ");
        for (java.util.Map.Entry<Enchantment, Integer> entry : _enchantments.entrySet()) {
            info.add("   -" + entry.getKey().toString() + " " + entry.getValue());
        }
        return info.toArray(new String[0]);
    }

    public ItemStack getItemStack() {
        // TODO: flesh this out
        return new ItemStack(_material);
    }

    public static List<String> getOptions() {
        return Arrays.asList("Name", "Material", "Lore", "Enchantments");
    }
}
