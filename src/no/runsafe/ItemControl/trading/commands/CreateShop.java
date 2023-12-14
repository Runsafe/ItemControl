package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.Globals;
import no.runsafe.ItemControl.trading.PurchaseData;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.BooleanArgument;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateShop extends PlayerCommand
{
	public CreateShop(TradingHandler handler)
	{
		super(
			"create",
			"Create a shop",
			"runsafe.traders.create",
			new BooleanArgument(COMPARE_NAME).withDefault(true),
			new BooleanArgument(COMPARE_DURABILITY).withDefault(true),
			new BooleanArgument(COMPARE_LORE).withDefault(true),
			new BooleanArgument(COMPARE_ENCHANTS).withDefault(true)
		);
		this.handler = handler;
	}
	private static final String COMPARE_NAME = "compareName";
	private static final String COMPARE_DURABILITY = "compareDurability";
	private static final String COMPARE_LORE = "compareLore";
	private static final String COMPARE_ENCHANTS = "compareEnchants";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.getCreatingPlayers().put(executor, new PurchaseData(null,
			parameters.getRequired(COMPARE_NAME), parameters.getRequired(COMPARE_DURABILITY),
			parameters.getRequired(COMPARE_LORE), parameters.getRequired(COMPARE_ENCHANTS)
		));

		return Globals.getCommandsShopCreateMessage();
	}

	private final TradingHandler handler;
}
