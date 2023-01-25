package me.brotherhong.EarnMoney.listeners;

import me.brotherhong.EarnMoney.EarnMoney;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;

public class EarnByKill implements Listener {

    EarnMoney plugin = EarnMoney.getInstance();
    Economy economy = EarnMoney.getEconomy();

    boolean enabled;
    String message;
    Double friendlyPrices;
    HashMap<EntityType, Double> hostilePrices = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityKilled(EntityDeathEvent event) {
        if (!enabled) return;
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if (player == null) return;

        // Enemy
        if (hostilePrices.containsKey(entity.getType())) {
            // pay money
            EconomyResponse response = economy.depositPlayer(player, hostilePrices.get(entity.getType()));
            if (response.transactionSuccess()) {
                sendActionBarMessage(player, response.amount, entity.getName());
            }
        }
        // friendly
        else {
            // pay money
            EconomyResponse response = economy.depositPlayer(player, friendlyPrices);
            if (response.transactionSuccess()) {
                sendActionBarMessage(player, response.amount, entity.getName());
            }
        }
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("mob.enabled");
        message = config.getString("mob.message");
        friendlyPrices = config.getDouble("mob.friendly");
        config.getConfigurationSection("mob.hostile")
                .getKeys(false)
                .forEach((String type) -> {
                    double price = config.getDouble("mob.hostile." + type);
                    hostilePrices.put(EntityType.valueOf(type), price);
                });
    }

    private void sendActionBarMessage(Player player, double amount, String mobName) {
        String text = message;
        text = text.replaceAll("&", "§");
        text = text.replaceAll("<money>", economy.format(amount));
        text = text.replaceAll("<mob>", mobName);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

}
