package com.cameronlattz.murderparty;

import com.cameronlattz.murderparty.models.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class CommandHelper {
    static List<String> extendedCommands = Arrays.asList("ammo", "map", "team", "role", "weapon");

    public static List<String> onTabComplete(Configuration configuration, String[] args) {
        List<String> autoCompletes = new ArrayList<String>();
        if (args.length > 0) {
            List<String> options = Arrays.asList("list", "add", "edit", "remove", "info");
            String arg0 = args[0].toLowerCase();
            if ("start".equals(arg0)) {
                if (args.length == 2) {
                    for (String mapName : configuration.getKeys("maps")) {
                        if (mapName.startsWith(args[1].toLowerCase())) {
                            autoCompletes.add(mapName);
                        }
                    }
                }
            } else if ("debug".equals(arg0)) {
                if (args.length == 2) {
                    if ("true".startsWith(args[1].toLowerCase())) {
                        autoCompletes.add("true");
                    } else if ("false".startsWith(args[1].toLowerCase())) {
                        autoCompletes.add("false");
                    }
                }
            } else if (extendedCommands.contains(arg0)) {
                if (args.length == 2) {
                    for (String option : options) {
                        if (option.startsWith(args[1].toLowerCase())) {
                            autoCompletes.add(option);
                        }
                    }
                } else if (args.length > 2 && options.contains(args[1].toLowerCase())) {
                    if (args.length == 3) {
                        for (String name : configuration.getKeys(arg0 + "s")) {
                            if (name.startsWith(args[2].toLowerCase())) {
                                autoCompletes.add(name);
                            }
                        }
                    } else if (args.length > 3) {
                        List<String> names = configuration.getKeys(arg0 + "s");
                        int nameIndex = names.indexOf(args[2].toLowerCase());
                        if (nameIndex != -1) {
                            if (args.length == 4) {
                                for (String option : getOptions(arg0).keySet()) {
                                    if (option.toLowerCase().startsWith(args[3].toLowerCase())) {
                                        autoCompletes.add(option);
                                    }
                                }
                            } else if (args.length == 5) {
                                if (extendedCommands.contains(args[3].toLowerCase())) {
                                    List<String> values = configuration.getKeys(args[3].toLowerCase() + "s");
                                    for (String value : values) {
                                        if (value.startsWith(args[4])) {
                                            autoCompletes.add(value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (args.length == 1)  {
                List<String> commands = new ArrayList<String>(Arrays.asList("debug", "reload", "start", "end"));
                commands.addAll(extendedCommands);
                for (String command : commands) {
                    if (command.startsWith(args[0].toLowerCase())) {
                        autoCompletes.add(command);
                    }
                }
            }
        }
        return autoCompletes;
    }

    public static boolean onCommand(MurderParty murderParty, Configuration configuration, CommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            List<String> messages = new ArrayList<String>(Arrays.asList(
                "/mp reload - Reload game",
                "/mp debug [true/false] - Output to console",
                "/mp start - Start game",
                "/mp start [map name] - Start game",
                "/mp end - End game"
            ));
            for (String command : extendedCommands) {
                messages.add("/mp " + command + " - List " + command + " commands");
            }
            sender.sendMessage(messages.toArray(new String[0]));
            return true;
        }
        else {
            String arg0 = args[0].toLowerCase();
            if ("reload".equals(arg0)) {
                murderParty.load();
                if (sender instanceof Player) {
                    sender.sendMessage("Murder Party reloaded.");
                }
                return true;
            } else if ("debug".equals(arg0)) {
                if (args.length == 1) {
                    sender.sendMessage("Command usage:");
                    sender.sendMessage("/mp debug [true/false]");
                } else if (args[1].toLowerCase().equals("true") || args[1].toLowerCase().equals("false")) {
                    configuration.set(murderParty, "debug", args[1].toLowerCase(), "boolean");
                }
            } else if ("start".equals(arg0)) {
                if (args.length == 1) {
                    murderParty.startGame();
                    return true;
                }
                else if (args.length == 2) {
                    if (configuration.getKeys("map").contains(args[2])) {
                        murderParty.startGame(configuration.getMap(args[2]));
                        return true;
                    }
                }
            } else if ("end".equals(arg0)) {
                murderParty.endGame(null);
                return true;
            } else if (extendedCommands.contains(arg0)) {
                String arg0upper = arg0.substring(0, 1).toUpperCase() + arg0.substring(1);
                if (args.length == 1 && sender instanceof Player) {
                    String[] messages = {
                            "Commands:",
                            "/mp " + arg0 + " list - List " + arg0 + "s",
                            "/mp " + arg0 + " add - Add new " + arg0,
                            "/mp " + arg0 + " edit - Edit " + arg0,
                            "/mp " + arg0 + " remove - Remove " + arg0,
                            "/mp " + arg0 + " info - Show " + arg0 + " info"
                    };
                    sender.sendMessage(messages);
                    return true;
                }
                else if ("list".equals(args[1].toLowerCase()) && sender instanceof Player) {
                    List<String> loaded = new ArrayList<String>();
                    List<String> config = new ArrayList<String>();
                    for (String key : configuration.getKeys(arg0 + "s")) {
                        ObjectInterface object = configuration.getObject(arg0, key);
                        if (object != null && object.getName().equals(key)) {
                            loaded.add(key);
                        } else {
                            config.add(key);
                        }
                    }
                    if (loaded.size() != 0) {
                        sender.sendMessage("Loaded " + arg0 + "s: " + StringUtils.join(loaded, ", "));
                    }
                    if (config.size() != 0) {
                        sender.sendMessage("Config " + arg0 + "s: " + StringUtils.join(config, ", "));
                    }
                    if (loaded.size() != 0 && config.size() != 0) {
                        sender.sendMessage("No " + arg0 + "s found.");
                    }
                    return true;
                }
                else if ("add".equals(args[1].toLowerCase())) {
                    if (args.length == 2 && sender instanceof Player) {
                        sender.sendMessage("Command usage:");
                        sender.sendMessage("/mp " + arg0 + " add [" + arg0 + " name]");
                        return true;
                    } else if (args.length > 2) {
                        if (configuration.addSection(murderParty,arg0 + "s", args[2].toLowerCase())) {
                            sender.sendMessage(arg0upper + " '" + args[2] + "' added.");
                        } else {
                            sender.sendMessage(arg0upper + " '" + args[2] + "' already exists.");
                        }
                        return true;
                    }
                }
                else if ("edit".equals(args[1].toLowerCase())) {
                    if (args.length == 2 && sender instanceof Player) {
                        sender.sendMessage("Command usage:");
                        sender.sendMessage("/mp " + arg0 + " edit [" + arg0 + " name]");
                        return true;
                    } else if (args.length > 2) {
                        if (configuration.getKeys(arg0 + "s").contains(args[2])) {
                            if (args.length < 5 && sender instanceof Player) {
                                List<String> options = new ArrayList<String>(getOptions(arg0).keySet());
                                String[] messages = new String[options.size()];
                                for (int i = 0; i < messages.length; i++) {
                                    messages[i] = "/mp " + arg0 + " edit " + args[2] + " " + options.get(i) + " [value]";
                                }
                                sender.sendMessage("Command usage:");
                                sender.sendMessage(messages);
                            } else {
                                for (java.util.Map.Entry<String, String> entry : getOptions(arg0).entrySet()) {
                                    String option = entry.getKey();
                                    if (option.equals(args[3])) {
                                        boolean set = true;
                                        if (extendedCommands.contains(args[3])) {
                                            if (!configuration.getKeys(args[3] + "s").contains(args[4].toLowerCase())) {
                                                set = false;
                                            }
                                        }
                                        if (set) {
                                            configuration.set(murderParty, arg0 + "s." + args[2].toLowerCase() + "." + args[3], args[4], entry.getValue());
                                            if (sender instanceof Player) {
                                                sender.sendMessage(args[3] + " set to " + args[4] + " for " + args[2].toLowerCase());
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            sender.sendMessage("Map '" + args[2] + "' not found.");
                        }
                        return true;
                    }
                }
                else if ("info".equals(args[1].toLowerCase()) && sender instanceof Player) {
                    if (args.length == 2) {
                        sender.sendMessage("Command usage:");
                        sender.sendMessage("/mp " + arg0 + " info [" + arg0 + " name]");
                    } else {
                        List<String> messages = new ArrayList<String>();
                        String arg2 = args[2].toLowerCase();
                        if (configuration.getKeys(arg0 +"s").contains(arg2)) {
                            List<String> validList = configuration.getInfo(arg0, arg2);
                            if (validList != null) {
                                messages.add(arg0upper + " '" + arg2 + "' (LOADED) information:");
                                messages.addAll(validList);
                            } else {
                                messages.add(arg0upper + " '" + arg2 + "' (CONFIG) information:");
                                for (java.util.Map.Entry<String, String> entry : getOptions(arg0).entrySet()) {
                                    String option = entry.getKey();
                                    String info = configuration.get(arg0 + "s." + arg2 + "." + option, entry.getValue());
                                    if (info != null) {
                                        messages.add("  " + option + ": " + info);
                                    }
                                }
                            }
                        }
                        sender.sendMessage(messages.toArray(new String[0]));
                    }
                    return true;
                }
                else if ("remove".equals(args[1].toLowerCase()) && sender instanceof Player) {
                    if (args.length == 2) {
                        sender.sendMessage("Command usage:");
                        sender.sendMessage("/mp " + arg0 + " remove [" + arg0 + " name]");
                        return true;
                    } else if (args.length > 2) {
                        if (configuration.remove(murderParty, arg0 + "s." + args[2])) {
                            sender.sendMessage(arg0upper + " '" + args[2] + "' removed.");
                        } else {
                            sender.sendMessage(arg0upper + " '" + args[2] + "' not found.");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static LinkedHashMap<String, String> getOptions(String commandName) {
        if (commandName.equals("map")) {
            return Map.getOptions();
        } else if (commandName.equals("role")) {
            return Role.getOptions();
        } else if (commandName.equals("team")) {
            return Team.getOptions();
        } else if (commandName.equals("weapon")) {
            return Weapon.getOptions();
        }
        return new LinkedHashMap<String, String>();
    }
}
