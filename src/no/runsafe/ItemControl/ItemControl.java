package no.runsafe.ItemControl;

import no.runsafe.ItemControl.enchant_container.EnchantContainerHandler;
import no.runsafe.ItemControl.spawners.SpawnerHandler;
import no.runsafe.ItemControl.spawners.SpawnerMonitor;
import no.runsafe.ItemControl.trading.*;
import no.runsafe.ItemControl.trading.commands.*;
import no.runsafe.ItemControl.trading.commands.Tag.Create;
import no.runsafe.ItemControl.trading.commands.Tag.Delete;
import no.runsafe.ItemControl.trading.commands.Tag.List;
import no.runsafe.ItemControl.trading.commands.Tag.TagArgument;
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
		addComponent(ItemTagIDRepository.class);
		addComponent(TradingRepository.class);
		addComponent(TradingHandler.class);

		Command traderCommand = new Command("traders", "Trader related commands", null);
		addComponent(traderCommand);
		addComponent(TagArgument.class);
		addComponent(PlayerTransactionRepository.class);

		traderCommand.addSubCommand(getInstance(CreateTagShop.class));
		traderCommand.addSubCommand(getInstance(CreateShop.class));
		traderCommand.addSubCommand(getInstance(DeleteShop.class));
		traderCommand.addSubCommand(getInstance(Info.class));

		Command tag = new Command("tag", "Shop tag ID commands.", null);
		tag.addSubCommand(getInstance(Create.class));
		tag.addSubCommand(getInstance(Delete.class));
		tag.addSubCommand(getInstance(List.class));
		traderCommand.addSubCommand(tag);

		plugin = this;
		//addComponent(ApplyCustomMap.class);
		//addComponent(MapHandler.class);
	}

	public static ItemControl plugin;
}
