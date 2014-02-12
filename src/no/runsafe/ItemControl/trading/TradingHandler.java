package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import org.bukkit.metadata.FixedMetadataValue;

public class TradingHandler
{
	public TradingHandler(ItemControl plugin)
	{
		this.plugin = plugin;
	}

	public boolean isTrader(RunsafeEntity entity)
	{
		return entity.hasMetadata("runsafe.trade.test");
	}

	public TradeNode getTradeNode(RunsafeEntity entity)
	{
		if (isTrader(entity))
			return (TradeNode) entity.getMetadata("runsafe.trade.test");

		return null;
	}

	public void setTradeNode(RunsafeEntity entity, TradeNode node)
	{
		entity.setMetadata("runsafe.trade.test", new FixedMetadataValue(plugin, node));
	}

	private final ItemControl plugin;
}
