package me.brotherhong.EarnMoney.listeners;

import me.brotherhong.EarnMoney.EarnMoney;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EarnByFish implements Listener {

    EarnMoney plugin = EarnMoney.getInstance();
    Economy economy = EarnMoney.getEconomy();

    final List<Material> treasures = new ArrayList<>();

    boolean enabled;
    String message;
    Double trashPrice;
    Double treasurePrice;

    public EarnByFish() {
        treasures.add(Material.BOW);
        treasures.add(Material.FISHING_ROD);
        treasures.add(Material.ENCHANTED_BOOK);
        treasures.add(Material.NAME_TAG);
        treasures.add(Material.NAUTILUS_SHELL);
        treasures.add(Material.SADDLE);
    }

    @EventHandler
    public void onPlayerFished(PlayerFishEvent event) {
        if (!enabled) return;
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        Entity caught = event.getCaught();

        if (caught == null) return;
        if (!(caught instanceof Item)) return;

        ItemStack itemStack = ((Item)caught).getItemStack();
        Material type = itemStack.getType();

        boolean isTreasure = treasures.contains(type);
        if (isTreasure && (type == Material.FISHING_ROD || type == Material.BOW) &&
            !itemStack.getItemMeta().hasEnchants()) {
            isTreasure = false;
        }

        double reward = (isTreasure ? treasurePrice : trashPrice);

        EconomyResponse response = economy.depositPlayer(player, reward);
        if (response.transactionSuccess()) {
            sendActionBarMessage(player, response.amount, type.toString().toLowerCase().replace("_", " "));
        }

    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        enabled = config.getBoolean("fish.enabled");
        message = config.getString("fish.message");
        trashPrice = config.getDouble("fish.caughts.trash");
        treasurePrice = config.getDouble("fish.caughts.treasure");
    }

    private void sendActionBarMessage(Player player, double amount, String caughtName) {
        String text = message;
        text = text.replace("&", "§");
        text = text.replace("<money>", economy.format(amount));
        text = text.replace("<caught>", caughtName);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

}
