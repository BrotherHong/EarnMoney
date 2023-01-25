package me.brotherhong.EarnMoney;

import me.brotherhong.EarnMoney.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {

    EarnMoney plugin = EarnMoney.getInstance();

    final List<String> commands = new ArrayList<>();
    final String prefix = "&f[&aEarn&eMoney&f]";

    public CommandManager() {
        commands.add("reload");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length == 0) return true;

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.loadPlugin();
            player.sendMessage(TextUtil.colorize(prefix + " &aReload Completed!"));
            return true;
        }

        player.sendMessage(TextUtil.colorize(prefix + " &cUnknown command!"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            for (String cmd : commands) {
                if (cmd.startsWith(args[0])) {
                    result.add(cmd);
                }
            }
        }

        return result;
    }
}
