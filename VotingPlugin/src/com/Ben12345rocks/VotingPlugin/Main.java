package com.Ben12345rocks.VotingPlugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.Ben12345rocks.AdvancedCore.Commands.GUI.AdminGUI;
import com.Ben12345rocks.AdvancedCore.Objects.CommandHandler;
import com.Ben12345rocks.AdvancedCore.Objects.RewardHandler;
import com.Ben12345rocks.AdvancedCore.Objects.UUID;
import com.Ben12345rocks.AdvancedCore.Util.Inventory.BInventory.ClickEvent;
import com.Ben12345rocks.AdvancedCore.Util.Inventory.BInventoryButton;
import com.Ben12345rocks.AdvancedCore.Util.Logger.Logger;
import com.Ben12345rocks.AdvancedCore.Util.Metrics.Metrics;
import com.Ben12345rocks.AdvancedCore.Util.Updater.Updater;
import com.Ben12345rocks.VotingPlugin.Commands.CommandLoader;
import com.Ben12345rocks.VotingPlugin.Commands.Commands;
import com.Ben12345rocks.VotingPlugin.Commands.Executers.CommandAdminVote;
import com.Ben12345rocks.VotingPlugin.Commands.Executers.CommandVote;
import com.Ben12345rocks.VotingPlugin.Commands.TabCompleter.AdminVoteTabCompleter;
import com.Ben12345rocks.VotingPlugin.Commands.TabCompleter.VoteTabCompleter;
import com.Ben12345rocks.VotingPlugin.Config.Config;
import com.Ben12345rocks.VotingPlugin.Config.ConfigFormat;
import com.Ben12345rocks.VotingPlugin.Config.ConfigGUI;
import com.Ben12345rocks.VotingPlugin.Config.ConfigOtherRewards;
import com.Ben12345rocks.VotingPlugin.Config.ConfigTopVoterAwards;
import com.Ben12345rocks.VotingPlugin.Config.ConfigVoteSites;
import com.Ben12345rocks.VotingPlugin.Data.ServerData;
import com.Ben12345rocks.VotingPlugin.Events.BlockBreak;
import com.Ben12345rocks.VotingPlugin.Events.PlayerInteract;
import com.Ben12345rocks.VotingPlugin.Events.PlayerJoinEvent;
import com.Ben12345rocks.VotingPlugin.Events.SignChange;
import com.Ben12345rocks.VotingPlugin.Events.VotiferEvent;
import com.Ben12345rocks.VotingPlugin.Events.VotingPluginUpdateEvent;
import com.Ben12345rocks.VotingPlugin.Objects.SignHandler;
import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.Objects.VoteSite;
import com.Ben12345rocks.VotingPlugin.OtherRewards.OtherVoteReward;
import com.Ben12345rocks.VotingPlugin.Signs.Signs;
import com.Ben12345rocks.VotingPlugin.TopVoter.TopVoter;
import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
import com.Ben12345rocks.VotingPlugin.Util.Updater.CheckUpdate;
import com.Ben12345rocks.VotingPlugin.VoteParty.VoteParty;
import com.Ben12345rocks.VotingPlugin.VoteReminding.VoteReminding;

/**
 * The Class Main.
 */
public class Main extends JavaPlugin {

	/** The config. */
	public static Config config;

	/** The config bonus reward. */
	public static ConfigOtherRewards configBonusReward;

	/** The config GUI. */
	public static ConfigGUI configGUI;

	/** The config format. */
	public static ConfigFormat configFormat;

	/** The config vote sites. */
	public static ConfigVoteSites configVoteSites;

	/** The plugin. */
	public static Main plugin;

	/** The top voter monthly. */
	public HashMap<User, Integer> topVoterMonthly;

	/** The top voter weekly. */
	public HashMap<User, Integer> topVoterWeekly;

	/** The top voter daily. */
	public HashMap<User, Integer> topVoterDaily;

	/** The updater. */
	public Updater updater;

	/** The vote command. */
	public ArrayList<CommandHandler> voteCommand;

	/** The admin vote command. */
	public ArrayList<CommandHandler> adminVoteCommand;

	/** The vote sites. */
	public ArrayList<VoteSite> voteSites;

	/** The vote today. */
	public HashMap<User, HashMap<VoteSite, Date>> voteToday;

	/** The place holder API enabled. */
	public boolean placeHolderAPIEnabled;

	/** The signs. */
	public ArrayList<SignHandler> signs;

	/** The vote log. */
	public Logger voteLog;

	/**
	 * Check advanced core.
	 */
	public void checkAdvancedCore() {
		if (Bukkit.getPluginManager().getPlugin("AdvancedCore") != null) {
			plugin.getLogger().info("Found AdvancedCore");
		} else {
			plugin.getLogger().severe("Failed to find AdvancedCore, plugin disabling");
			plugin.getLogger().severe("Download at: https://www.spigotmc.org/resources/advancedcore.28295/");
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}

	/**
	 * Check place holder API.
	 */
	public void checkPlaceHolderAPI() {
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			placeHolderAPIEnabled = true;
			plugin.debug("PlaceholderAPI found, will attempt to parse placeholders");
		} else {
			placeHolderAPIEnabled = false;
			plugin.debug("PlaceholderAPI not found, PlaceholderAPI placeholders will not work");
		}
	}

	/**
	 * Check votifier.
	 */
	public void checkVotifier() {
		if (getServer().getPluginManager().getPlugin("Votifier") == null
				&& getServer().getPluginManager().getPlugin("NuVotifier") == null) {
			plugin.debug("Votifier and NuVotifier not found, votes may not work");
		}
	}

	/**
	 * Debug.
	 *
	 * @param message
	 *            the message
	 */
	public void debug(String message) {
		com.Ben12345rocks.AdvancedCore.Main.plugin.debug(plugin, message);
	}

	/**
	 * Gets the user.
	 *
	 * @param playerName
	 *            the player name
	 * @return the user
	 */
	public User getUser(String playerName) {
		return UserManager.getInstance().getVotingPluginUser(playerName);
	}

	/**
	 * Gets the user.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the user
	 */
	public User getUser(UUID uuid) {
		return UserManager.getInstance().getVotingPluginUser(uuid);
	}

	/**
	 * Gets the vote site.
	 *
	 * @param siteName
	 *            the site name
	 * @return the vote site
	 */
	public VoteSite getVoteSite(String siteName) {
		for (VoteSite voteSite : voteSites) {
			if (voteSite.getSiteName().equalsIgnoreCase(siteName)) {
				return voteSite;
			}
		}
		if (config.getAutoCreateVoteSites()) {
			configVoteSites.generateVoteSite(siteName.replace(".", "_"));
			return new VoteSite(siteName.replace(".", "_"));
		} else {
			return null;
		}
	}

	/**
	 * Gets the vote site name.
	 *
	 * @param url
	 *            the url
	 * @return the vote site name
	 */
	public String getVoteSiteName(String url) {
		ArrayList<String> sites = ConfigVoteSites.getInstance().getVoteSitesNames();
		if (url == null) {
			return null;
		}
		if (sites != null) {
			for (String siteName : sites) {
				String URL = ConfigVoteSites.getInstance().getServiceSite(siteName);
				if (URL != null) {
					if (URL.equals(url)) {
						return siteName;
					}
				}
			}
		}
		return url;

	}

	/**
	 * Load vote sites.
	 */
	public void loadVoteSites() {
		configVoteSites.setup();
		voteSites = configVoteSites.getVoteSitesLoad();

		plugin.debug("Loaded VoteSites");

	}

	/**
	 * Log vote.
	 *
	 * @param date
	 *            the date
	 * @param playerName
	 *            the player name
	 * @param voteSite
	 *            the vote site
	 */
	public void logVote(Date date, String playerName, String voteSite) {
		if (Config.getInstance().getLogVotesToFile()) {
			String str = new SimpleDateFormat("EEE, d MMM yyyy HH:mm").format(date);
			voteLog.logToFile(str + ": " + playerName + " voted on " + voteSite);
		}
	}

	/**
	 * Metrics.
	 */
	private void metrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
			plugin.debug("Loaded Metrics");
		} catch (IOException e) {
			plugin.getLogger().info("Can't submit metrics stats");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		Signs.getInstance().storeSigns();
		plugin = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		plugin = this;
		checkAdvancedCore();
		setupFiles();
		registerCommands();
		registerEvents();
		checkVotifier();
		metrics();

		CheckUpdate.getInstance().startUp();

		checkPlaceHolderAPI();

		loadVoteSites();

		VoteReminding.getInstance().loadRemindChecking();
		UserManager.getInstance().loadUsers();

		Bukkit.getScheduler().runTask(plugin, new Runnable() {

			@Override
			public void run() {
				Signs.getInstance().loadSigns();
			}
		});

		topVoterMonthly = new HashMap<User, Integer>();
		topVoterWeekly = new HashMap<User, Integer>();
		topVoterDaily = new HashMap<User, Integer>();
		voteToday = new HashMap<User, HashMap<VoteSite, Date>>();

		voteLog = new Logger(plugin, new File(plugin.getDataFolder(), "votelog.txt"));

		AdminGUI.getInstance().addButton(
				new BInventoryButton("&cVotingPlugin AdminGUI", new String[] {}, new ItemStack(Material.PAPER)) {

					@Override
					public void onClick(ClickEvent clickEvent) {
						com.Ben12345rocks.VotingPlugin.Commands.GUI.AdminGUI.getInstance()
								.openAdminGUI(clickEvent.getPlayer());

					}
				});

		if (ConfigOtherRewards.getInstance().getVotePartyEnabled()) {
			VoteParty.getInstance().check();
		}
		VoteParty.getInstance().register();

		TopVoter.getInstance().register();

		plugin.getLogger().info("Enabled VotingPlgin " + plugin.getDescription().getVersion());
		com.Ben12345rocks.AdvancedCore.Main.plugin.registerHook(this);

		RewardHandler.getInstance().addRewardFolder(new File(plugin.getDataFolder(), "Rewards"));
		RewardHandler.getInstance().setDefaultFolder(new File(plugin.getDataFolder(), "Rewards"));

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				update();
			}
		});

	}

	/**
	 * Register commands.
	 */
	private void registerCommands() {
		CommandLoader.getInstance().loadCommands();
		CommandLoader.getInstance().loadAliases();

		// /vote, /v
		getCommand("vote").setExecutor(new CommandVote(this));
		getCommand("vote").setTabCompleter(new VoteTabCompleter());
		getCommand("v").setExecutor(new CommandVote(this));
		getCommand("v").setTabCompleter(new VoteTabCompleter());

		// /adminvote, /av
		getCommand("adminvote").setExecutor(new CommandAdminVote(this));
		getCommand("adminvote").setTabCompleter(new AdminVoteTabCompleter());
		getCommand("av").setExecutor(new CommandAdminVote(this));
		getCommand("av").setTabCompleter(new AdminVoteTabCompleter());

		plugin.debug("Loaded Commands");

	}

	/**
	 * Register events.
	 */
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new PlayerJoinEvent(this), this);
		pm.registerEvents(new VotiferEvent(this), this);

		pm.registerEvents(new SignChange(this), this);

		pm.registerEvents(new BlockBreak(this), this);

		pm.registerEvents(new PlayerInteract(this), this);

		pm.registerEvents(new VotingPluginUpdateEvent(this), this);
		pm.registerEvents(OtherVoteReward.getInstance(), this);

		plugin.debug("Loaded Events");

	}

	/**
	 * Reload.
	 */
	public void reload() {
		config.reloadData();
		configGUI.reloadData();
		configFormat.reloadData();
		plugin.loadVoteSites();
		configBonusReward.reloadData();
		plugin.setupFiles();
		ServerData.getInstance().reloadData();
		plugin.update();
		CommandLoader.getInstance().loadTabComplete();
		com.Ben12345rocks.AdvancedCore.Main.plugin.reload();
		UserManager.getInstance().loadUsers();
	}

	/**
	 * Setup files.
	 */
	public void setupFiles() {
		config = Config.getInstance();
		configVoteSites = ConfigVoteSites.getInstance();
		configFormat = ConfigFormat.getInstance();
		configBonusReward = ConfigOtherRewards.getInstance();
		configGUI = ConfigGUI.getInstance();

		config.setup();
		configFormat.setup();
		configBonusReward.setup();
		configGUI.setup();

		ConfigTopVoterAwards.getInstance().setup();

		plugin.debug("Loaded Files");

	}

	/**
	 * Update.
	 */
	public synchronized void update() {
		com.Ben12345rocks.AdvancedCore.Thread.Thread.getInstance().run(new Runnable() {

			@Override
			public void run() {
				try {
					TopVoter.getInstance().updateTopVoters();
					Commands.getInstance().updateVoteToday();
					ServerData.getInstance().updateValues();
					Signs.getInstance().updateSigns();
					plugin.debug("Background task ran");

				} catch (Exception ex) {
					plugin.getLogger().info("Looks like there are no data files or something went wrong.");
					ex.printStackTrace();
				}

			}
		});

	}

}