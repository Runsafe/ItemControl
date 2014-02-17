package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;

public class EntityListener implements IEntityDamageByEntityEvent
{
	public EntityListener(TradingHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		if (handler.isTrader(event.getEntity()))
			event.cancel();
	}

	private TradingHandler handler;
}
