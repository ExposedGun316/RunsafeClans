package no.runsafe.clans.commands;

import no.runsafe.clans.Clan;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class KickClanMember extends PlayerAsyncCommand
{
	public KickClanMember(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("kick", "Kick a member from your clan", "runsafe.clans.kick", scheduler, new Player().require());
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		String playerName = executor.getName();
		if (!clanHandler.playerIsInClan(playerName))
			return "&cYou are not in a clan.";

		if (!clanHandler.playerIsClanLeader(playerName))
			return "&cYou are not the clan leader.";

		IPlayer targetPlayer = parameters.getValue("player");

		if (targetPlayer == null)
			return "&cInvalid player.";

		String targetPlayerName = targetPlayer.getName();
		if (targetPlayerName.equals(playerName))
			return "&cYou cannot kick yourself.";

		Clan playerClan = clanHandler.getPlayerClan(playerName); // Grab the players clan.

		if (!clanHandler.playerIsInClan(targetPlayerName, playerClan.getId()))
			return "&cThat player is not in your clan.";

		clanHandler.kickClanMember(targetPlayer, executor); // Kick the player.

		if (targetPlayer.isOnline())
			targetPlayer.sendColouredMessage("&cYou have been kicked from the clan.");

		return null;
	}

	private final ClanHandler clanHandler;
}
