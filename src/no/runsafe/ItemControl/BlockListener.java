package no.runsafe.ItemControl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import no.runsafe.framework.interfaces.IConfiguration;
import no.runsafe.framework.interfaces.IPluginEnabled;
import no.runsafe.framework.interfaces.IScheduler;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
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
	private HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
	
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
		
		if (this.blockShouldDrop(event.getPlayer().getWorld(), theBlock.getTypeId()) && heldItem.containsEnchantment(new EnchantmentWrapper(33)))
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
			}
			catch (Exception e)
			{
				//Diddums
			}
		}
	}

	@Override
	public void OnPluginEnabled()
	{
		this.loadConfig();
	}
	
	private Boolean blockShouldDrop(World world, Integer blockId)
	{
		if(worldBlockDrops.containsKey("*") && worldBlockDrops.get("*").contains(blockId))
			return true;
				
		if(worldBlockDrops.containsKey(world.getName()) && worldBlockDrops.get(world.getName()).contains(blockId))
			return true;
			
		return false;
	}
	
	private void loadConfig()
	{
		ConfigurationSection block = this.config.getSection("blockDrops");
		if(block == null)
			return;
		
		Set<String> keys = block.getKeys(true);
		if(keys == null)
			return;
		
		for(String key : block.getKeys(true))
		{
			if(!this.worldBlockDrops.containsKey(key))
				this.worldBlockDrops.put(key, block.getIntegerList(key));
		}
	}
	
}
