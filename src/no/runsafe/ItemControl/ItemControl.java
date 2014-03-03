package no.runsafe.ItemControl;

import no.runsafe.ItemControl.custom_maps.ApplyCustomMap;
import no.runsafe.ItemControl.enchant_container.EnchantContainerHandler;
import no.runsafe.ItemControl.spawners.SpawnerHandler;
import no.runsafe.ItemControl.spawners.SpawnerMonitor;
import no.runsafe.ItemControl.trading.*;
import no.runsafe.ItemControl.trading.commands.CreateNamedTrader;
import no.runsafe.ItemControl.trading.commands.CreateTrader;
import no.runsafe.ItemControl.trading.commands.EditTrader;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Database;
import no.runsafe.framework.features.Events;

public class ItemControl extends RunsafeConfigurablePlugin
{
	public static IDebug Debugger = null;

	@Override
	protected void pluginSetup()
	{
		Debugger = getComponent(IDebug.class);

		addComponent(Events.class);
		addComponent(Commands.class);
		addComponent(Database.class);

		// Core functions
		addComponent(Globals.class);
		addComponent(PlayerListener.class);
		addComponent(BlockListener.class);

		// XP bottle handling
		addComponent(EnchantContainerHandler.class);

		// Mob spawners
		addComponent(SpawnerHandler.class);
		addComponent(SpawnerMonitor.class);

		// Merchants
		addComponent(TradingRepository.class);
		addComponent(TradingHandler.class);

		addComponent(InventoryMonitor.class);

		Command traderCommand = new Command("traders", "Trader related commands", null);
		addComponent(traderCommand);

		traderCommand.addSubCommand(getInstance(CreateTrader.class));
		traderCommand.addSubCommand(getInstance(CreateNamedTrader.class));
		traderCommand.addSubCommand(getInstance(EditTrader.class));

		addComponent(ApplyCustomMap.class);

		plugin = this;
	}

	public static ItemControl plugin;
}
