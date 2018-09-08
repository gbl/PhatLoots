package com.codisimus.plugins.phatloots.loot;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsUtil;
import com.codisimus.plugins.phatloots.gui.Button;
import com.codisimus.plugins.phatloots.gui.InventoryListener;
import com.codisimus.plugins.phatloots.gui.Tool;
import java.util.*;
import com.tealcube.minecraft.bukkit.mythicdrops.MythicDropsPlugin;
import com.tealcube.minecraft.bukkit.mythicdrops.api.items.ItemGenerationReason;
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier;
import com.tealcube.minecraft.bukkit.mythicdrops.tiers.TierMap;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.ItemStackUtil;
import com.tealcube.minecraft.bukkit.mythicdrops.utils.TierUtil;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A MythicDropsItem is an ItemStack generated by the plugin MythicDrops
 *
 * @author Cody
 */
@SerializableAs("MythicDropsItem")
public class MythicDropsItem extends Loot {
    private static ArrayList<String> tierList = null;
    public String tierName;
    public int amountLower = 1;
    public int amountUpper = 1;
    public int durabilityLower = 0;
    public int durabilityUpper = 0;
    
    static {
        instantiateTierList();
    }

    /**
     * Adds a MythicDrops Item as Loot
     */
    private static class AddMythicDropsItemButton extends Button {
        private AddMythicDropsItemButton(ItemStack item) {
            super(item);
        }

        @Override
        public boolean onClick(ClickType click, Inventory inv, PhatLoot phatLoot, List<Loot> lootList) {
            Loot loot;
            switch (click) {
            case RIGHT: //MythicDrops Gem
                loot = new Gem();
                break;
            case SHIFT_LEFT: //Unidentified Item
                loot = new UnidentifiedItem();
                break;
            default:
                //Add a new MythicDrops Item with the first tier
                loot = new MythicDropsItem(tierList.get(0));
                break;
            }
            lootList.add(loot);
            return true;
        }
    }

    /**
     * Registers the MythicDropsItem button and tool for the loot GUI
     */
    public static void registerButtonAndTool() {
        //Register the Add Collection Button
        ItemStack item = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(item.getType());
        List<String> uses = new ArrayList<>();
        info.setDisplayName("§2Add new MythicDrops Item...");
        uses.add("§4LEFT CLICK:");
        uses.add("§6 Add new MythicDrops Item");
        uses.add("§4RIGHT CLICK:");
        uses.add("§6 Add new Gem");
        uses.add("§4SHIFT + LEFT CLICK:");
        uses.add("§6 Add new Unidentified Item");
        info.setLore(uses);
        item.setItemMeta(info);

        InventoryListener.registerButton(new AddMythicDropsItemButton(item));


        item = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        List<String> lore = new ArrayList<>();
        meta.setDisplayName("§2MythicDrops Toggle (Click to change Tool)");
        lore.add("§1Toggles through the names of Tiers and Gems");
        lore.add("§1Names toggle in alphabetical order");
        lore.add("§4LEFT CLICK:");
        lore.add("§6 Next Name");
        lore.add("§4RIGHT CLICK:");
        lore.add("§6 Previous Name");
        lore.add("§4DOUBLE CLICK:");
        lore.add("§6 Ahead 10 Names");
        lore.add("§4SHIFT + LEFT CLICK:");
        lore.add("§6 Ahead 100 Names");
        lore.add("§4SHIFT + RIGHT CLICK:");
        lore.add("§6 Back 100 Names");
        lore.add("§4SCROLL CLICK:");
        lore.add("§6 Back to first Name");
        meta.setLore(lore);
        item.setItemMeta(meta);
        new Tool("MYTHICDROPS", item).registerTool();
    }

    /**
     * Constructs a new Loot with the given tier
     *
     * @param tierName The name of the MythicDrops Tier
     */
    public MythicDropsItem(String tierName) {
        this.tierName = tierName;
    }

    /**
     * Constructs a new Loot with the given tier and amount/durability ranges
     *
     * @param tierName The name of the MythicDrops Tier
     * @param amountLower The lower bound of the amount range
     * @param amountUpper The upper bound of the amount range
     * @param durabilityLower The lower bound of the durability range
     * @param durabilityUpper The upper bound of the durability range
     */
    public MythicDropsItem(String tierName, int amountLower, int amountUpper, int durabilityLower, int durabilityUpper) {
        this.tierName = tierName;
        this.amountLower = amountLower;
        this.amountUpper = amountUpper;
        this.durabilityLower = durabilityLower;
        this.durabilityUpper = durabilityUpper;
    }

    /**
     * Constructs a new MythicDrops Item from a Configuration Serialized phase
     *
     * @param map The map of data values
     */
    @SuppressWarnings({"UnusedAssignment", "OverridableMethodCallInConstructor"})
    public MythicDropsItem(Map<String, Object> map) {
        String currentLine = null; //The value that is about to be loaded (used for debugging)
        try {
            Object number = map.get(currentLine = "Probability");
            setProbability((number instanceof Double) ? (Double) number : (Integer) number);
            tierName = (String) map.get(currentLine = "Tier");
            if (map.containsKey(currentLine = "Amount")) {
                amountLower = amountUpper = (Integer) map.get(currentLine);
            } else if (map.containsKey(currentLine = "AmountLower")) {
                amountLower = (Integer) map.get(currentLine);
                amountUpper = (Integer) map.get(currentLine = "AmountUpper");
            }
            if (map.containsKey(currentLine = "Durability")) {
                durabilityLower = durabilityUpper = (Integer) map.get(currentLine);
            } else if (map.containsKey(currentLine = "DurabilityLower")) {
                durabilityLower = (Integer) map.get(currentLine);
                durabilityUpper = (Integer) map.get(currentLine = "DurabilityUpper");
            }
        } catch (Exception ex) {
            //Print debug messages
            PhatLoots.logger.log(Level.SEVERE, "Failed to load MythicDropsItem line: {0}", currentLine);
            PhatLoots.logger.log(Level.SEVERE, "of PhatLoot: {0}", PhatLoot.current == null ? "unknown" : PhatLoot.current);
            PhatLoots.logger.severe("Last successfull load was...");
            PhatLoots.logger.log(Level.SEVERE, "PhatLoot: {0}", PhatLoot.last == null ? "unknown" : PhatLoot.last);
            PhatLoots.logger.log(Level.SEVERE, "Loot: {0}", Loot.last == null ? "unknown" : Loot.last.toString());
        }
    }

    /**
     * Generates a MythicDrops item and adds it to the item list
     *
     * @param lootBundle The loot that has been rolled for
     * @param lootingBonus The increased chance of getting rarer loots
     */
    @Override
    public void getLoot(LootBundle lootBundle, double lootingBonus) {
        Tier tier = TierUtil.getTier(tierName);
        int amount = PhatLootsUtil.rollForInt(amountLower, amountUpper);
        while (amount > 0) {
            ItemStack mis = MythicDropsPlugin.getNewDropBuilder().useDurability(false)
                    .withItemGenerationReason(ItemGenerationReason.EXTERNAL).withTier(tier).build();
            if (durabilityLower > 0 || durabilityUpper > 0) {
                mis.setDurability(ItemStackUtil.getDurabilityForMaterial(mis.getType(), durabilityLower, durabilityUpper));
            }
            lootBundle.addItem(mis);
            amount--;
        }
    }

    /**
     * Returns the information of the MythicDrops Item in the form of an ItemStack
     *
     * @return An ItemStack representation of the Loot
     */
    @Override
    public ItemStack getInfoStack() {
        //A MythicDropsItem is represented by an Enchantment Table
        ItemStack infoStack = new ItemStack(Material.ENCHANTING_TABLE);

        //Set the display name of the item
        ItemMeta info = Bukkit.getItemFactory().getItemMeta(infoStack.getType());
        info.setDisplayName("§2MythicDrops Item");

        //Add more specific details of the item
        List<String> details = new ArrayList();
        details.add("§1Tier: §6" + tierName);
        details.add("§1Probability: §6" + getProbability());
        if (amountLower == amountUpper) {
            details.add("§1Amount: §6" + amountLower);
        } else {
            details.add("§1Amount: §6" + amountLower + '-' + amountUpper);
        }
        if (durabilityLower == durabilityUpper) {
            details.add("§1Durability: §6" + durabilityLower);
        } else {
            details.add("§1Durability: §6" + durabilityLower + '-' + durabilityUpper);
        }

        //Construct the ItemStack and return it
        info.setLore(details);
        infoStack.setItemMeta(info);
        return infoStack;
    }

    /**
     * Toggles a Loot setting depending on the type of Click
     *
     * @param click The type of Click (Only SHIFT_LEFT, SHIFT_RIGHT, and MIDDLE are used)
     * @return true if the Loot InfoStack should be refreshed
     */
    @Override
    public boolean onToggle(ClickType click) {
        return false;
    }

    /**
     * Toggles the MythicDrops Tier depending on the type of Click
     *
     * @param tool The Tool that was used to click
     * @param click The type of Click (Only LEFT, RIGHT, MIDDLE, SHIFT_LEFT, SHIFT_RIGHT, and DOUBLE_CLICK are used)
     * @return true if the Loot InfoStack should be refreshed
     */
    @Override
    public boolean onToolClick(Tool tool, ClickType click) {
        if (!tool.getName().equals("MYTHICDROPS")) {
            return false;
        }

        if (tierList.isEmpty()) {
            return false;
        }

        int index = tierList.indexOf(tierName);
        switch (click) {
        case LEFT: //+1
            index++;
            break;
        case DOUBLE_CLICK: //+9
            index += 9;
            break;
        case RIGHT: //-1
            index += -1;
            break;
        case SHIFT_LEFT: //+100
            index += 100;
            break;
        case SHIFT_RIGHT: //-100
            index += -100;
            break;
        case MIDDLE: //default tier
            index = 0;
            break;
        default:
            return false;
        }

        while (index >= tierList.size()) {
            index -= tierList.size();
        }
        while (index < 0) {
            index += tierList.size();
        }

        tierName = tierList.get(index);
        return true;
    }

    private static void instantiateTierList() {
        if (tierList == null) {
            //Cache Tiers alphabetically
            tierList = new ArrayList<>();
            for (Tier tier : TierMap.getInstance().values()) {
                tierList.add(tier.getName());
            }
            Collections.sort(tierList);
        }
    }

    /**
     * Modifies the amount associated with the Loot
     *
     * @param amount The amount to modify by (may be negative)
     * @param both true if both lower and upper ranges should be modified, false for only the upper range
     * @return true if the Loot InfoStack should be refreshed
     */
    @Override
    public boolean modifyAmount(int amount, boolean both) {
        if (both) {
            amountLower += amount;
            if (amountLower < 0) {
                amountLower = 0;
            }
        }
        amountUpper += amount;
        //Upper bound cannot be less than lower bound
        if (amountUpper < amountLower) {
            amountUpper = amountLower;
        }
        return true;
    }

    /**
     * Resets the amount of Loot to 1
     *
     * @return true if the Loot InfoStack should be refreshed
     */
    @Override
    public boolean resetAmount() {
        amountLower = 1;
        amountUpper = 1;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(amountLower);
        if (amountLower != amountUpper) {
            sb.append('-');
            sb.append(amountUpper);
        }

        sb.append(" ");
        sb.append(tierName);
        sb.append(" tiered MythicDrops item ");

        sb.append(" @ ");
        //Only display the decimal values if the probability is not a whole number
        sb.append(Math.floor(getProbability()) == getProbability() ? String.valueOf((int) getProbability()) : String.valueOf(getProbability()));

        sb.append("%");

        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MythicDropsItem)) {
            return false;
        }

        MythicDropsItem loot = (MythicDropsItem) object;
        return loot.tierName.equals(tierName)
                && loot.amountLower == amountLower
                && loot.amountUpper == amountUpper
                && loot.durabilityLower == durabilityLower
                && loot.durabilityUpper == durabilityUpper;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.tierName);
        hash = 31 * hash + this.amountLower;
        hash = 31 * hash + this.amountUpper;
        hash = 31 * hash + this.durabilityLower;
        hash = 31 * hash + this.durabilityUpper;
        return hash;
    }

    @Override
    public Map<String, Object> serialize() {
        Map map = new TreeMap();
        map.put("Probability", getProbability());
        map.put("Tier", tierName);
        if (amountLower == amountUpper) {
            if (amountLower != 1) {
                map.put("Amount", amountLower);
            }
        } else {
            map.put("AmountLower", amountLower);
            map.put("AmountUpper", amountUpper);
        }
        if (durabilityLower == durabilityUpper) {
            if (durabilityLower != 1) {
                map.put("Durability", durabilityLower);
            }
        } else {
            map.put("DurabilityLower", durabilityLower);
            map.put("DurabilityUpper", durabilityUpper);
        }
        return map;
    }
}
