package com.cameronlattz.murderparty.models;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Weapon implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Material _material;
    private boolean _drops;
    private String _lore;
    private LinkedHashMap<Enchantment, Integer> _enchantments;
    private Material _ammoMaterial;
    private Integer _ammoCooldown;

    public Weapon(String name, String displayName, Material material, boolean drops, String lore, LinkedHashMap<Enchantment, Integer> enchantments) {
        _name = name;
        _displayName = displayName;
        _material = material;
        _drops = drops;
        _lore = lore;
        _enchantments = enchantments;
    }

    public String getName() { return _name; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  drops: " + _drops);
        if (_material != null) {
            info.add("  material: " + _material.name());
        }
        if (_lore != null) {
            info.add("  lore: " + _lore);
        }
        if (_enchantments.size() > 0) {
            info.add("  enchantments: ");
            for (java.util.Map.Entry<Enchantment, Integer> entry : _enchantments.entrySet()) {
                info.add("   -" + entry.getKey().toString() + " " + entry.getValue());
            }
        }
        return info;
    }

    public ItemStack getItemStack() {
        // TODO: flesh this out
        return new ItemStack(_material);
    }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("material", "string");
            put("lore", "string");
            put("enchantments", "string");
        }};
    }
}
