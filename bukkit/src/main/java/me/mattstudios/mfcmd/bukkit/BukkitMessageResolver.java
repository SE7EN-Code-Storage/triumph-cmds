package me.mattstudios.mfcmd.bukkit;

import me.mattstudios.mfcmd.base.components.MessageResolver;
import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface BukkitMessageResolver extends MessageResolver<CommandSender> {

    @Override
    void resolve(CommandSender sender);

}
