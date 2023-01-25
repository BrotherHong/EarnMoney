package me.brotherhong.EarnMoney.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import me.brotherhong.EarnMoney.EarnMoney;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EarnByHarvest implements Listener {

    EarnMoney plugin = EarnMoney.getInstance();
    Economy economy = EarnMoney.getEconomy();
    NamespacedKey cropPlacedKey = new NamespacedKey(plugin, "crop-placed-key");

    final List<Material> maxAgeCrops = new ArrayList<>();
    final List<Material> blockCrops = new ArrayList<>();

    boolean enabled;
    String message;
    HashMap<Material, Double> cropPrices = new HashMap<>();

    public EarnByHarvest() {
        maxAgeCrops.add(Material.WHEAT);
        maxAgeCrops.add(Material.CARROTS);
        maxAgeCrops.add(Material.POTATOES);
        maxAgeCrops.add(Material.COCOA);
        maxAgeCrops.add(Material.NETHER_WART);
        maxAgeCrops.add(Material.BEETROOT);

        blockCrops.add(Material.SUGAR_CANE);
        blockCrops.add(Material.PUMPKIN);
        blockCrops.add(Material.MELON);
    }

    @EventHandler
    public void onBlockHarvest(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();

        if (!cropPrices.containsKey(type)) return;

        String cropName = type.toString().toLowerCase().replace("_", " ");

        if (maxAgeCrops.contains(type)) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() != ageable.getMaximumAge()) return;

            EconomyResponse response = economy.depositPlayer(player, cropPrices.get(type));
            if (response.transactionSuccess()) {
                sendActionBarMessage(player, response.amount, cropName);
            }

            return;
        }

        if (blockCrops.contains(type)) {

            int count = 0;

            // count sugar cane
            if (type == Material.SUGAR_CANE) {
                Location p = block.getLocation().clone();
                World world = p.getWorld();

                assert world != null;
                Block topBlock = world.getBlockAt(p);

                while (topBlock.getType() == Material.SUGAR_CANE) {
                    PersistentDataContainer pdc = new CustomBlockData(topBlock, plugin);
                    if (pdc.has(cropPlacedKey, PersistentDataType.STRING)) {
                        pdc.remove(cropPlacedKey);
                    } else {
                        count++;
                    }

                    p.add(new Vector(0, 1, 0));
                    topBlock = world.getBlockAt(p);
                }
            }
            // pumpkin or melon
            else {
                PersistentDataContainer blockData = new CustomBlockData(block, plugin);
                if (blockData.has(cropPlacedKey, PersistentDataType.STRING)) {
                    blockData.remove(cropPlacedKey);
                    return;
                }
                count = 1;
            }

            if (count == 0) return;

            EconomyResponse response = economy.depositPlayer(player, cropPrices.get(type) * count);
            if (response.transactionSuccess()) {
                sendActionBarMessage(player, response.amount, cropName);
            }

            return;
        }
    }

    @EventHandler
    public void onCropBlockBreak(BlockPlaceEvent event) {
        if (!enabled) return;
        Block block = event.getBlock();
        Material type = block.getType();

        if (!blockCrops.contains(type)) return;

        PersistentDataContainer blockData = new CustomBlockData(block, plugin);

        blockData.set(cropPlacedKey, PersistentDataType.STRING, "placed-crop");
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("harvest.enabled");
        message = config.getString("harvest.message");
        config.getConfigurationSection("harvest.crops").getKeys(false).forEach((String type) -> {
            Double price = config.getDouble("harvest.crops." + type);
            cropPrices.put(Material.valueOf(type), price);
        });
    }

    private void sendActionBarMessage(Player player, double amount, String cropName) {
        String text = message;
        text = text.replace("&", "§");
        text = text.replace("<money>", economy.format(amount));
        text = text.replace("<crop>", cropName);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

}
