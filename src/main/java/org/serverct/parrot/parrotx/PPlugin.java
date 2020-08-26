package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.I18n;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Consumer;

public class PPlugin extends JavaPlugin {

    @Getter
    private static PPlugin instance;
    public I18n lang;
    public String localeKey;
    protected PConfig pConfig;
    private Consumer<PluginManager> listenerRegister = null;
    private String timeLog = null;
    @Getter
    private CommandHandler cmdHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        final long timestamp = System.currentTimeMillis();

        init();

        if (Objects.nonNull(listenerRegister)) {
            listenerRegister.accept(Bukkit.getPluginManager());
        }

        if (Objects.nonNull(timeLog)) {
            final long time = System.currentTimeMillis() - timestamp;
            lang.logRaw(MessageFormat.format(timeLog, time));
        }
    }

    public void init() {
        lang = new I18n(this, "Chinese");

        preload();

        pConfig.init();

        localeKey = pConfig.getConfig().getString("Language");

        load();
    }

    protected void preload() {
    }

    protected void load() {
    }

    protected void listen(Consumer<PluginManager> register) {
        this.listenerRegister = register;
    }

    protected void setTimeLog(final String format) {
        this.timeLog = format;
    }

    protected void registerCommand(@NonNull CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(handler.mainCmd);
        if (command != null) {
            this.cmdHandler = handler;
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            lang.logError(I18n.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
    }
}
