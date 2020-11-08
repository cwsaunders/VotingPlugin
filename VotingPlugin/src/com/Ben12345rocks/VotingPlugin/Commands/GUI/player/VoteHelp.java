package com.Ben12345rocks.VotingPlugin.Commands.GUI.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Ben12345rocks.AdvancedCore.CommandAPI.CommandHandler;
import com.Ben12345rocks.AdvancedCore.Util.Messages.StringParser;
import com.Ben12345rocks.AdvancedCore.gui.GUIHandler;
import com.Ben12345rocks.AdvancedCore.gui.GUIMethod;
import com.Ben12345rocks.VotingPlugin.Main;
import com.Ben12345rocks.VotingPlugin.Config.Config;

import net.md_5.bungee.api.chat.TextComponent;

public class VoteHelp extends GUIHandler {

	private Main plugin;
	private int page;

	public VoteHelp(Main plugin, CommandSender player, int page) {
		super(player);
		this.plugin = plugin;
		this.page = page;
	}

	@Override
	public ArrayList<String> getChat(CommandSender sender) {
		return null;
	}

	@Override
	public void onBook(Player player) {

	}

	@Override
	public void onChat(CommandSender sender) {
		sendMessageJson(voteHelpText(sender));
	}

	@Override
	public void onChest(Player player) {

	}

	@Override
	public void open() {
		open(GUIMethod.CHAT);
	}

	public ArrayList<TextComponent> voteHelpText(CommandSender sender) {
		int pagesize = Config.getInstance().getFormatPageSize();
		ArrayList<TextComponent> msg = new ArrayList<TextComponent>();
		ArrayList<TextComponent> text = helpText(sender);

		int maxPage = text.size() / pagesize;
		if ((text.size() % pagesize) != 0) {
			maxPage++;
		}

		msg.add(StringParser.getInstance()
				.stringToComp(Config.getInstance().getFormatCommandsVoteHelpTitle() + " " + page + "/" + maxPage));

		for (int i = pagesize * (page - 1); (i < text.size()) && (i < ((page) * pagesize)); i++) {
			msg.add(text.get(i));
		}

		return msg;
	}

	public ArrayList<TextComponent> helpText(CommandSender sender) {
		ArrayList<TextComponent> msg = new ArrayList<TextComponent>();
		HashMap<String, TextComponent> unsorted = new HashMap<String, TextComponent>();

		boolean requirePerms = Config.getInstance().getFormatCommandsVoteHelpRequirePermission();

		for (CommandHandler cmdHandle : plugin.getVoteCommand()) {
			if (requirePerms && sender.hasPermission(cmdHandle.getPerm())) {
				unsorted.put(cmdHandle.getHelpLineCommand("/vote"),
						cmdHandle.getHelpLine("/vote", Config.getInstance().getFormatCommandsVoteHelpLine()));
			} else {
				unsorted.put(cmdHandle.getHelpLineCommand("/vote"), cmdHandle.getHelpLine("/vote"));
			}
		}
		ArrayList<String> unsortedList = new ArrayList<String>();
		unsortedList.addAll(unsorted.keySet());
		Collections.sort(unsortedList, String.CASE_INSENSITIVE_ORDER);
		for (String cmd : unsortedList) {
			msg.add(unsorted.get(cmd));
		}

		return msg;
	}

}
