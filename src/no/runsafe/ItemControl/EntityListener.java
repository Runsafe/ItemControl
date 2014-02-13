package no.runsafe.ItemControl;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.api.event.entity.IMobSpawnerPulsed;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.minecraft.entity.RunsafeLivingEntity;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;

public class EntityListener implements IMobSpawnerPulsed, IEntityDamageByEntityEvent
{
	public EntityListener(Globals globals, IConsole console, TradingHandler handler)
	{
		this.globals = globals;
		this.console = console;
		this.handler = handler;
	}

	@Override
	public boolean OnMobSpawnerPulsed(RunsafeLivingEntity entity, ILocation location)
	{
		if (!globals.spawnerTypeValid(entity.getEntityType(), null))
		{
			console.logInformation(
				"SPAWNER WARNING: &cBlocked invalid spawner of &e%s&c at (%s,%d,%d,%d)",
				entity.getRaw().getType().name(),
				location.getWorld().getName(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ()
			);
			return false;
		}
		return true;
	}

	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		if (handler.isTrader(event.getEntity()))
			event.cancel();
	}

	private Globals globals;
	private IConsole console;
	private TradingHandler handler;
}
