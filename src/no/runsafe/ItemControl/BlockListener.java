package no.runsafe.ItemControl;

import java.lang.reflect.Field;
import java.util.ArrayList;

import no.runsafe.framework.interfaces.IConfiguration;
import no.runsafe.framework.interfaces.IPluginEnabled;
import no.runsafe.framework.interfaces.IScheduler;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener, IPluginEnabled
{
	private IConfiguration config;
	private IScheduler scheduler;
	private ArrayList<Integer> blockDrops = new ArrayList<Integer>();
	
	public BlockListener(IConfiguration config, IScheduler scheduler)
	{
		this.config = config;
		this.scheduler = scheduler;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player thePlayer = event.getPlayer();
		ItemStack heldItem = thePlayer.getItemInHand();
		
		final Block theBlock = event.getBlock();
		
		if (theBlock.getType() == Material.MOB_SPAWNER)
		{
			final short mobID = heldItem.getDurability();
			
			this.scheduler.setTimedEvent(new Runnable() {

			@Override
			public void run() {
				setSpawnerEntityID(theBlock, mobID);
				
			} }, 1);
			
		}
	}
	
	public void setSpawnerEntityID(Block block, short entityID)
	{
		try
		{
			Field mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");
			mobIDField.setAccessible(true);
			
			Field tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
	        tileField.setAccessible(true);
	        
	        
			BlockState blockState = block.getState();
			if (!(blockState instanceof CreatureSpawner))
			{
				throw new IllegalArgumentException("setSpawnerEntityID called on non-spawner block: " + block);
			}
	
			CraftCreatureSpawner spawner = ((CraftCreatureSpawner)blockState);
	
			if (tileField != null && mobIDField != null)
			{
				try
				{
					String mobID = EntityType.fromId(entityID).getName();
	
					net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);
	
					tile.a(mobID);
					return;
				}
				catch (Exception e)
				{
					//diddums
				}
			}
	
			// Fallback to wrapper
			EntityType ct = EntityType.fromId(entityID);
			if (ct == null)
			{
				throw new IllegalArgumentException("Failed to find creature type for "+entityID);
			}
	
			spawner.setSpawnedType(ct);
			spawner.update();
			blockState.update();
		}
		catch (Exception e)
		{
			//diddums
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player thePlayer = event.getPlayer();
		ItemStack heldItem = thePlayer.getItemInHand();
		Block theBlock = event.getBlock();
		
		if (this.blockDrops.contains(theBlock.getTypeId()) && heldItem.containsEnchantment(new EnchantmentWrapper(33)))
		{
			World theWorld = theBlock.getWorld();
			
			try
			{
				Field tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
	            tileField.setAccessible(true);
				
				BlockState blockState = theBlock.getState();
				CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState;
				
				ItemStack itemToDrop = new ItemStack(theBlock.getTypeId(), 1, spawner.getSpawnedType().getTypeId());
				theWorld.dropItem(theBlock.getLocation(), itemToDrop);
				
				//TileEntityMobSpawner rawSpawner = (TileEntityMobSpawner) tileField.get(spawner);
				//rawSpawner.a("COW");
				
				//spawner.setSpawnedType(EntityType.COW);
				//blockState.update();
			}
			catch (Exception e)
			{
				//Diddums
			}
			
			//mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");  // MCP "mobID"
               // mobIDField.setAccessible(true);
			
		}
	}

	@Override
	public void OnPluginEnabled()
	{
		this.loadConfig();
	}
	
	private void loadConfig()
	{
		String blockDrops = this.config.getConfigValueAsString("blockDrops");
		String[] blockList = blockDrops.split(",");
		
		for (int i = 0; i < blockList.length; i++)
		{
			this.blockDrops.add(Integer.parseInt(blockList[i]));
		}
	}
	
}
