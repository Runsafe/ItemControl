package no.runsafe.ItemControl;

import no.runsafe.framework.event.entity.IMobSpawnerPulsed;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.entity.RunsafeLivingEntity;

public class EntityListener implements IMobSpawnerPulsed
{
	public EntityListener(Globals globals, IOutput console)
	{
		this.globals = globals;
		this.console = console;
	}

	@Override
	public boolean OnMobSpawnerPulsed(RunsafeLivingEntity entity, RunsafeLocation location)
	{
		if (!globals.spawnerTypeValid(entity.getRaw().getType().name(), null))
		{
			console.writeColoured(
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

	private Globals globals;
	private IOutput console;
}
