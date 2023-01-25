package me.brotherhong.EarnMoney;

import me.brotherhong.EarnMoney.listeners.EarnByFish;
import me.brotherhong.EarnMoney.listeners.EarnByHarvest;
import me.brotherhong.EarnMoney.listeners.EarnByKill;
import me.brotherhong.EarnMoney.listeners.EarnByMine;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class EarnMoney extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    private static EarnMoney instance;

    private EarnByKill earnByKill;
    private EarnByMine earnByMine;
    private EarnByHarvest earnByHarvest;
    private EarnByFish earnByFish;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        this.saveDefaultConfig();

        // listeners
        earnByKill = new EarnByKill();
        earnByMine = new EarnByMine();
        earnByHarvest = new EarnByHarvest();
        earnByFish = new EarnByFish();

        // register listeners
        getServer().getPluginManager().registerEvents(earnByKill, this);
        getServer().getPluginManager().registerEvents(earnByMine, this);
        getServer().getPluginManager().registerEvents(earnByHarvest, this);
        getServer().getPluginManager().registerEvents(earnByFish, this);

        // command
        getCommand("earnmoney").setExecutor(new CommandManager());

        // load
        loadPlugin();
    }

    public void loadPlugin() {
        reloadConfig();
        earnByKill.load();
        earnByMine.load();
        earnByHarvest.load();
        earnByFish.load();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static EarnMoney getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return econ;
    }

}
