package ru.leonidm.millida.rating.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ConfigUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([0-9a-fA-F])>");

    public static int getInt(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isInt(key)) {
            throw loadException(section, key, "must be integer");
        }

        return section.getInt(key);
    }

    public static int getPositiveInt(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isInt(key)) {
            throw loadException(section, key, "must be positive integer");
        }

        return section.getInt(key);
    }

    public static double getDouble(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isDouble(key)) {
            if (section.isInt(key)) {
                return section.getInt(key);
            }

            throw loadException(section, key, "must be double");
        }

        return section.getDouble(key);
    }

    public static boolean getBoolean(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isBoolean(key)) {
            throw loadException(section, key, "must be boolean");
        }

        return section.getBoolean(key);
    }

    @NotNull
    public static String getString(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        String value;
        if (!section.isString(key)) {
            if (section.isList(key)) {
                value = String.join("\n", getStringList(section, key));
            } else {
                throw loadException(section, key, "must be string");
            }
        } else {
            value = section.getString(key);
        }

        return convert(value);
    }

    @NotNull
    private static String convert(@NotNull String string) {
        string = ChatColor.translateAlternateColorCodes('&', string);

        StringBuffer stringBuffer = new StringBuffer();

        Matcher matcher = HEX_PATTERN.matcher(string);
        while (matcher.find()) {
            String group = matcher.group(1);
            char[] c = group.toCharArray();
            String replacement = "§x§" + c[0] + "§" + c[1] + "§" + c[2] + "§" + c[3] + "§" + c[4] + "§" + c[5];
            matcher.appendReplacement(stringBuffer, replacement);
        }

        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    @NotNull
    public static List<String> getStringList(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isList(key)) {
            if (section.isString(key)) {
                List<String> list = new ArrayList<>();
                list.add(section.getString(key));
                return list;
            }

            throw loadException(section, key, "must be string list");
        }

        return section.getStringList(key).stream()
                .map(ConfigUtils::convert)
                .collect(Collectors.toList());
    }

    @NotNull
    public static Material getMaterial(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        Material material;
        if (section.isInt(key)) {
            int id = section.getInt(key);

            material = Material.getMaterial(id);
            if (material == null) {
                throw ConfigUtils.loadException(section, key, "represent non-existing material '" + id + "'");
            }
        } else {
            String rawId = getString(section, key).toUpperCase();
            try {
                material = Material.valueOf(rawId);
            } catch (IllegalArgumentException e) {
                try {
                    material = Material.valueOf("LEGACY_" + rawId);
                } catch (IllegalArgumentException ex) {
                    throw ConfigUtils.loadException(section, key, "represent non-existing material '" + rawId + "'");
                }
            }
        }

        if (material == Material.AIR) {
            throw ConfigUtils.loadException(section, key, "must be non-air material");
        }

        return material;
    }

    @NotNull
    public static ItemStack getItemStack(@NotNull ConfigurationSection icon) throws ConfigLoadException {
        Material material = ConfigUtils.getMaterial(icon, "id");

        int amount = 1;
        if (icon.contains("amount")) {
            amount = ConfigUtils.getPositiveInt(icon, "amount");
        }

        byte data = 0;
        if (icon.contains("data")) {
            data = (byte) ConfigUtils.getPositiveInt(icon, "data");
        }

        ItemStack itemStack = new ItemStack(material, amount, (short) 0, data);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (icon.contains("name")) {
            String name = ConfigUtils.getString(icon, "name");
            itemMeta.setDisplayName(name);
        }

        if (icon.contains("lore")) {
            List<String> lore = ConfigUtils.getStringList(icon, "lore");
            itemMeta.setLore(lore);
        }

        if (icon.contains("enchanted") && ConfigUtils.getBoolean(icon, "enchanted")) {
            itemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (material == Material.SKULL_ITEM && itemMeta instanceof SkullMeta) {
            if (icon.contains("player")) {
                String player = ConfigUtils.getString(icon, "player");
                ((SkullMeta) itemMeta).setOwner(player);
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @NotNull
    public static ConfigurationSection getSection(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        if (!section.isConfigurationSection(key)) {
            throw loadException(section, key, "is not configured (is config malformed?)");
        }

        return section.getConfigurationSection(key);
    }

    @NotNull
    public static Location getLocation(@NotNull ConfigurationSection section, @NotNull String key) throws ConfigLoadException {
        ConfigurationSection location = getSection(section, key);
        String worldName = getString(location, "world");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new ConfigLoadException("represents non-existing world '" + worldName + "'");
        }

        double x = getDouble(location, "x");
        double y = getDouble(location, "y");
        double z = getDouble(location, "z");
        return new Location(world, x, y, z);
    }

    @NotNull
    public static ConfigLoadException loadException(@NotNull ConfigurationSection section, @NotNull String key,
                                             @NotNull String message) {
        return new ConfigLoadException("Key '" + path(section, key) + "' " + message);
    }

    @NotNull
    public static String path(@NotNull ConfigurationSection section, @NotNull String key) {
        String path = section.getCurrentPath();
        if (path.isEmpty()) {
            return key;
        } else {
            return path + "." + key;
        }
    }
}
