package com.cameronlattz.murderparty.models;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Weapon implements ObjectInterface {
    private String _name;
    private String _displayName;
    private Material _material;
    private int _dropProbability = 0;
    private boolean _canDamage;
    private List<String> _lore = new ArrayList<String>();
    private LinkedHashMap<Enchantment, Integer> _enchantments = new LinkedHashMap<Enchantment, Integer>();
    private List<Ammo> _ammos;
    private List<Ability> _abilities = new ArrayList<Ability>();
    private ItemStack _itemStack;

    public Weapon(String name, String displayName, Material material, boolean canDamage, Integer dropProbability, List<String> lore, LinkedHashMap<Enchantment, Integer> enchantments, List<Ammo> ammos, List<Ability> abilities) {
        _name = name;
        _displayName = displayName;
        _material = material;
        _canDamage = canDamage;
        _dropProbability = dropProbability != null ? dropProbability : 0;
        _lore = lore != null ? lore : _lore;
        _enchantments = enchantments != null ? enchantments : _enchantments;
        _ammos = ammos != null ? ammos : _ammos;
        _abilities = abilities != null ? abilities : _abilities;
        ItemStack itemStack = new ItemStack(_material);
        if (_material != Material.AIR) {
            if (_enchantments.size() != 0) {
                itemStack.addUnsafeEnchantments(_enchantments);
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (_lore != null && _lore.size() != 0) {
                itemMeta.setLore(_lore);
            }
            if (_displayName != null && _displayName.length() != 0) {
                itemMeta.setDisplayName(_displayName);
            }
            if (itemMeta instanceof Damageable) {
                itemMeta.setUnbreakable(true);
            }
            itemMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            itemStack.setItemMeta(itemMeta);
        }
        _itemStack = itemStack;
    }

    public String getName() { return _name; }

    public String getDisplayName() { return _displayName; }

    public boolean canDamage() { return _canDamage; }

    public int getDropProbability() { return _dropProbability; }

    public List<Ammo> getAmmos() { return _ammos; }

    public List<Ability> getAbilities() { return _abilities; }

    public ItemStack getItemStack() { return _itemStack; }

    public List<String> getInfo() {
        List<String> info = new ArrayList<String>();
        if (_displayName != null) {
            info.add("  name: " + _displayName);
            info.add("  can damage: " + _canDamage);
            info.add("  drop probability: " + _dropProbability);
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
            info.add("  abilities:" + StringUtils.join(_abilities, ", "));
            for (Ammo ammo : _ammos) {
                info.add("  ammo - " + ammo.getName() + ": ");
                info.add("      material: " + ammo.getItemStack().getType());
                info.add("      count: " + ammo.getItemStack().getAmount());
                info.add("      max: " + ammo.getMax());
                info.add("      cooldown: " + ammo.getCooldown());
            }
        }
        return info;
    }

    public static LinkedHashMap<String, String> getOptions() {
        return new LinkedHashMap<String, String>() {{
            put("name", "string");
            put("material", "string");
            put("canDamage", "boolean");
            put("dropProbability", "int");
            put("lore", "string");
            put("enchantments", "string");
            put("ammo", "string");
            put("abilities", "string");
        }};
    }
}
