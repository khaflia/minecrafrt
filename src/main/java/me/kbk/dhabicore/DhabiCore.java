package me.kbk.dhabicore;

import me.kbk.dhabicore.commands.*;
import me.kbk.dhabicore.listeners.*;
import me.kbk.dhabicore.managers.*;
import me.kbk.dhabicore.scoreboard.ScoreboardManager;
import me.kbk.dhabicore.utils.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DhabiCore extends JavaPlugin {

    private static DhabiCore instance;

    private RankManager rankManager;
    private HomeManager homeManager;
    private TpaManager tpaManager;
    private VeinMinerManager veinMinerManager;
    private TreeFellerManager treeFellerManager;
    private ScoreboardManager scoreboardManager;
    private ZoneManager zoneManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("messages.yml", false);
        MessageManager.init(this);

        rankManager       = new RankManager(this);
        homeManager       = new HomeManager(this);
        tpaManager        = new TpaManager(this);
        veinMinerManager  = new VeinMinerManager(this);
        treeFellerManager = new TreeFellerManager(this);
        scoreboardManager = new ScoreboardManager(this);
        zoneManager       = new ZoneManager(this);

        // Commands
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(this));
        getCommand("tpadeny").setExecutor(new TpaDenyCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("menu").setExecutor(new MenuCommand(this));
        getCommand("dhabi").setExecutor(new MenuCommand(this));
        getCommand("setrank").setExecutor(new SetRankCommand(this));
        getCommand("veinminer").setExecutor(new VeinMinerCommand(this));
        getCommand("treefeller").setExecutor(new TreeFellerCommand(this));
        getCommand("zone").setExecutor(new ZoneCommand(this));
        getCommand("pos1").setExecutor(new PosCommand(this, 1));
        getCommand("pos2").setExecutor(new PosCommand(this, 2));

        // Listeners
        getServer().getPluginManager().registerEvents(new TpaListener(this), this);
        getServer().getPluginManager().registerEvents(new HomeListener(this), this);
        getServer().getPluginManager().registerEvents(new VeinMinerListener(this), this);
        getServer().getPluginManager().registerEvents(new TreeFellerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ZoneListener(this), this);

        getLogger().info("DhabiCore enabled — khaleeji.lol");
    }

    @Override
    public void onDisable() {
        if (homeManager       != null) homeManager.saveAll();
        if (rankManager       != null) rankManager.saveAll();
        if (veinMinerManager  != null) veinMinerManager.saveAll();
        if (treeFellerManager != null) treeFellerManager.saveAll();
        if (zoneManager       != null) zoneManager.saveAll();
        getLogger().info("DhabiCore disabled.");
    }

    public static DhabiCore getInstance()              { return instance; }
    public RankManager       getRankManager()          { return rankManager; }
    public HomeManager       getHomeManager()          { return homeManager; }
    public TpaManager        getTpaManager()           { return tpaManager; }
    public VeinMinerManager  getVeinMinerManager()     { return veinMinerManager; }
    public TreeFellerManager getTreeFellerManager()    { return treeFellerManager; }
    public ScoreboardManager getScoreboardManager()    { return scoreboardManager; }
    public ZoneManager       getZoneManager()          { return zoneManager; }

    public String msg(String key) { return MessageManager.get(key); }
}
