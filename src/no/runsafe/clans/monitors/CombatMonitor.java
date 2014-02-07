package no.runsafe.clans.monitors;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.entity.RunsafeLivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeProjectile;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CombatMonitor implements IEntityDamageByEntityEvent, IPlayerDeathEvent
{
	public CombatMonitor(IServer server, IScheduler scheduler, ClanHandler clanHandler)
	{
		this.server = server;
		this.scheduler = scheduler;
		this.clanHandler = clanHandler;
	}

	@Override
	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
	{
		IPlayer deadPlayer = event.getEntity();
		String deadPlayerName = deadPlayer.getName();

		// Check we tracked the player getting hit and they are in a clan!
		if (track.containsKey(deadPlayerName) && clanHandler.playerIsInClan(deadPlayerName))
		{
			String killerName = track.get(deadPlayerName).getAttacker(); // Grab the name of the last player to hit them.
			if (clanHandler.playerIsInClan(killerName))
			{
				clanHandler.addClanKill(killerName); // Stat the kill
				clanHandler.addClanDeath(deadPlayerName); // Stat the death
			}
		}
	}

	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof IPlayer)
		{
			IPlayer victim = (IPlayer) event.getEntity();
			if (!victim.isVanished())
			{
				IPlayer source = null;
				RunsafeEntity attacker = event.getDamageActor();

				if (attacker instanceof IPlayer)
					source = (IPlayer) attacker;
				else if (attacker instanceof RunsafeProjectile)
				{
					RunsafeProjectile projectile = (RunsafeProjectile) attacker;
					if (!(projectile.getEntityType() == ProjectileEntity.Egg || projectile.getEntityType() == ProjectileEntity.Snowball))
						source = this.findPlayer(((RunsafeProjectile) attacker).getShooter());
				}

				if (source == null || source.isVanished() || source.shouldNotSee(victim) || isSamePlayer(victim, source))
					return;

				registerHit(victim, source); // Register the hit!
			}
		}
	}

	private void registerHit(IPlayer victim, IPlayer attacker)
	{
		final String victimName = victim.getName();

		// Check to see if we have a timer existing.
		if (track.containsKey(victimName))
			scheduler.cancelTask(track.get(victimName).getTimerID()); // Cancel existing timer.
		else
			track.put(victimName, new CombatTrackingNode()); // Create blank node.

		// Update the node with new information.
		track.get(victimName).setAttacker(attacker.getName()).setTimerID(scheduler.startAsyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				track.remove(victimName); // Remove after 10 seconds.
			}
		}, 10));
	}

	private boolean isSamePlayer(IPlayer one, IPlayer two)
	{
		return one.getName().equalsIgnoreCase(two.getName());
	}

	private IPlayer findPlayer(RunsafeLivingEntity entity)
	{
		List<IPlayer> onlinePlayers = server.getOnlinePlayers();
		for (IPlayer player : onlinePlayers)
			if (entity != null && player != null && entity.getEntityId() == player.getEntityId())
				return player;

		return null;
	}

	private final IServer server;
	private final IScheduler scheduler;
	private final ClanHandler clanHandler;
	private final ConcurrentHashMap<String, CombatTrackingNode> track = new ConcurrentHashMap<String, CombatTrackingNode>(0);
}
