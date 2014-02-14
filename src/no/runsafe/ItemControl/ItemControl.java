package no.runsafe.ItemControl;

import no.runsafe.ItemControl.enchant_container.EnchantContainerHandler;
import no.runsafe.ItemControl.trading.InventoryMonitor;
import no.runsafe.ItemControl.trading.PlayerMonitor;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.ItemControl.trading.TradingRepository;
import no.runsafe.ItemControl.trading.commands.CreateMonitor;
import no.runsafe.ItemControl.trading.commands.CreateTrader;
import no.runsafe.ItemControl.trading.commands.EditMonitor;
import no.runsafe.ItemControl.trading.commands.EditTrader;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.api.command.Command;
import no.runsafe.framework.features.Commands;
import no.runsafe.framework.features.Database;
import no.runsafe.framework.features.Events;

public class ItemControl extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		addComponent(Events.class);
		addComponent(Globals.class);
		addComponent(Commands.class);
		addComponent(Database.class);
		addComponent(PlayerListener.class);
		addComponent(BlockListener.class);
		addComponent(EntityListener.class);

		addComponent(TradingRepository.class);
		addComponent(TradingHandler.class);

		addComponent(CreateMonitor.class);
		addComponent(EditMonitor.class);
		addComponent(PlayerMonitor.class);
		addComponent(InventoryMonitor.class);

		addComponent(EnchantContainerHandler.class);

		Command traderCommand = new Command("traders", "Trader related commands", null);
		addComponent(traderCommand);

		traderCommand.addSubCommand(getInstance(CreateTrader.class));
		traderCommand.addSubCommand(getInstance(EditTrader.class));
	}
}
