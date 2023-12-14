package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.Globals;
import no.runsafe.ItemControl.trading.PurchaseData;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.ItemControl.trading.commands.Tag.TagArgument;
import no.runsafe.framework.api.command.argument.BooleanArgument;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateTagShop extends PlayerCommand
{
	public CreateTagShop(TradingHandler handler)
	{
		super(
			"createtagshop",
			"Create a shop using a tag",
			"runsafe.traders.create",
			new TagArgument(TAG_NAME, handler),
			new BooleanArgument(COMPARE_NAME).withDefault(true),
			new BooleanArgument(COMPARE_DURABILITY).withDefault(true),
			new BooleanArgument(COMPARE_LORE).withDefault(true),
			new BooleanArgument(COMPARE_ENCHANTS).withDefault(true)
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";
	private static final String COMPARE_NAME = "compareName";
	private static final String COMPARE_DURABILITY = "compareDurability";
	private static final String COMPARE_LORE = "compareLore";
	private static final String COMPARE_ENCHANTS = "compareEnchants";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String tag = parameters.getValue(TAG_NAME);
		if (tag == null || tag.contains("%") || !handler.getAllTags().contains(tag))
			tag = null;

		handler.getCreatingPlayers().put(executor, new PurchaseData(tag,
			parameters.getRequired(COMPARE_NAME), parameters.getRequired(COMPARE_DURABILITY),
			parameters.getRequired(COMPARE_LORE), parameters.getRequired(COMPARE_ENCHANTS)
		));

		if (tag == null)
			return Globals.getCommandsShopCreateMessage();
		else
			return String.format(Globals.getCommandsShopCreateTagMessage(), tag);
	}

	private final TradingHandler handler;
}
