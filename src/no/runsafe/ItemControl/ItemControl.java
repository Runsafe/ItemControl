package no.runsafe.ItemControl;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.ItemControl.trading.commands.CreateTrader;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Events;

public class ItemControl extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		addComponent(Events.class);
		addComponent(Globals.class);
		addComponent(Commands.class);
		addComponent(PlayerListener.class);
		addComponent(BlockListener.class);
		addComponent(EntityListener.class);
		addComponent(TradingHandler.class);
		addComponent(CreateTrader.class);
	}
}
