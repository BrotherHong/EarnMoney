package me.brotherhong.EarnMoney.listeners;

import com.jeff_media.customblockdata.CustomBlockData;
import me.brotherhong.EarnMoney.EarnMoney;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class EarnByMine implements Listener {

    EarnMoney plugin = EarnMoney.getInstance();
    Economy economy = EarnMoney.getEconomy();
    NamespacedKey orePlacedKey = new NamespacedKey(plugin, "ore-placed-key");

    boolean enabled;
    String message;
    HashMap<Material, Double> orePrices = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOreMined(BlockBreakEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!orePrices.containsKey(block.getType())) return;

        PersistentDataContainer blockData = new CustomBlockData(block, plugin);

        if (blockData.has(orePlacedKey, PersistentDataType.STRING)) {
            blockData.remove(orePlacedKey);
            return;
        }

        EconomyResponse response = economy.depositPlayer(player, orePrices.get(block.getType()));
        if (response.transactionSuccess()) {
            String oreName = block.getType().toString().toLowerCase().replace("_", " ");
            sendActionBarMessage(player, response.amount, oreName);
        }
    }

    @EventHandler
    public void onOrePlaced(BlockPlaceEvent event) {
        if (!enabled) return;
        Block block = event.getBlock();

        if (!orePrices.containsKey(block.getType())) return;

        PersistentDataContainer blockData = new CustomBlockData(block, plugin);

        blockData.set(orePlacedKey, PersistentDataType.STRING, "placed-ore");
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("mine.enabled");
        message = config.getString("mine.message");
        config.getConfigurationSection("mine.ores").getKeys(false).forEach((String type) -> {
            Double price = config.getDouble("mine.ores." + type);
            orePrices.put(Material.valueOf(type), price);
        });
    }

    private void sendActionBarMessage(Player player, double amount, String oreName) {
        String text = message;
        text = text.replace("&", "§");
        text = text.replace("<money>", economy.format(amount));
        text = text.replace("<ore>", oreName);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

}
