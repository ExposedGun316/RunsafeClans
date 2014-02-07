package no.runsafe.clans.commands;

import no.runsafe.clans.Clan;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.AnyPlayerRequired;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class LookupClan extends PlayerAsyncCommand
{
	public LookupClan(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("lookup", "Lookup which clan a player is in", "runsafe.clans.lookup", scheduler, new AnyPlayerRequired());
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getPlayer("player");
		if (targetPlayer == null)
			return "&cInvalid player!";

		String playerName = targetPlayer.getName();

		if (!clanHandler.playerIsInClan(playerName))
			return "&cThat player is not in a clan.";

		Clan clan = clanHandler.getPlayerClan(playerName);

		return targetPlayer.getPrettyName() + "&f has been a member of " + clan + " for " + clanHandler.getPlayerJoinString(targetPlayer) + ".";
	}

	private final ClanHandler clanHandler;
}
