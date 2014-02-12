package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.ItemControl.trading.VillagerController;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class CreateTrader extends PlayerCommand implements IPlayerInteractEntityEvent
{
	public CreateTrader(TradingHandler handler)
	{
		super("createtrader", "Create a trader!", "runsafe.itemcontrol.traders.create");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String playerName = executor.getName();
		if (!interactTrack.contains(playerName))
			interactTrack.add(playerName);

		return "&eRight-click on a villager to make it a trader!";
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();
		String playerName = player.getName();

		if (interactTrack.contains(playerName))
		{
			RunsafeEntity entity = event.getRightClicked();
			if (entity.getEntityType() == LivingEntity.Villager)
			{
				VillagerController controller = new VillagerController(entity);
				controller.clearTrades();
				controller.addTrade(Item.BuildingBlock.Dirt.getItem(), Item.BuildingBlock.Emerald.getItem());
				player.sendColouredMessage(entity.getUniqueId().toString());
			}
			else
			{
				player.sendColouredMessage("&cThat is not a villager.");
			}
			interactTrack.remove(playerName);
		}
	}

	private final List<String> interactTrack = new ArrayList<String>(0);
	private final TradingHandler handler;
}
