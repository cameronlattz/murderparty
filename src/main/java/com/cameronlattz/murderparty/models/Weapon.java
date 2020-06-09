package com.cameronlattz.murderparty.models;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class Weapon implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Material _material;
    private boolean _drops;
    private List<String> _lore = new ArrayList<String>();
    private LinkedHashMap<Enchantment, Integer> _enchantments;
    private Material _ammoMaterial;
    private Integer _ammoCooldown;

    public Weapon(String name, String displayName, Material material, boolean drops, List<String> lore, LinkedHashMap<Enchantment, Integer> enchantments) {
        _name = name;
        _displayName = displayName;
        _material = material;
        _drops = drops;
        _lore = lore;
        _enchantments = enchantments;
    }

    public String getName() { return _name; }

    public String getDisplayName() { return _displayName; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        info.add("  name: " + _displayName);
        info.add("  drops: " + _drops);
        if (_material != null) {
            info.add("  material: " + _material.name());
        }
        if (_lore.size() > 0) {
            info.add("  lore: ");
            for (String loreLine : _lore) {
                info.add("   -" + loreLine);
            }
        }
        if (_enchantments.size() > 0) {
            info.add("  enchantments: ");
            for (java.util.Map.Entry<Enchantment, Integer> entry : _enchantments.entrySet()) {
                String name = WordUtils.capitalize(entry.getKey().getKey().getKey().replace("_", " "));
                info.add("   -" + name + " " + entry.getValue());
            }
        }
        return info;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(_material);
        if (_enchantments.size() > 0) {
            itemStack.addUnsafeEnchantments(_enchantments);
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(_lore);
        itemMeta.setDisplayName(_displayName);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
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
