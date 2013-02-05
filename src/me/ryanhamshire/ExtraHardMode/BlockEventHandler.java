/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.ExtraHardMode;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Torch;
import org.bukkit.util.Vector;

//event handlers related to blocks
public class BlockEventHandler implements Listener 
{
	//constructor
	public BlockEventHandler()
	{
	}
	
	//when a player breaks a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent breakEvent)
	{	
		Block block = breakEvent.getBlock();
		World world = block.getWorld();
		Player player = breakEvent.getPlayer();
		
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		//FEATURE: stone breaks tools much more quickly
		if(ExtraHardMode.instance.config_superHardStone)
		{			
			ItemStack inHandStack = player.getItemInHand();			
			
			//if breaking stone with an item in hand and the player does NOT have the bypass permission
			if(	(block.getType() == Material.STONE || block.getType() == Material.ENDER_STONE) && 
				inHandStack != null)
			{				
				//if not using an iron or diamond pickaxe, don't allow breakage and explain to the player
				Material tool = inHandStack.getType();
				if(tool != Material.IRON_PICKAXE && tool != Material.DIAMOND_PICKAXE)
				{
					notifyPlayer(player, Messages.StoneMiningHelp, "ehm.silent.stone_mining_help", Sound.CAT_HISS, 10);
					breakEvent.setCancelled(true);
					return;
				}
				
				//otherwise, drastically reduce tool durability when breaking stone
				else
				{
					short amount = 0;
					
					if(tool == Material.IRON_PICKAXE)
						amount = 8;
					else
						amount = 22;
					
					inHandStack.setDurability((short)(inHandStack.getDurability() + amount));
				}
			}
			
			//when ore is broken, it softens adjacent stone
			//important to ensure players can reach the ore they break
			if(block.getType().name().endsWith("_ORE"))
			{
				Block adjacentBlock = block.getRelative(BlockFace.DOWN);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.UP);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.EAST);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.WEST);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.NORTH);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
				
				adjacentBlock = block.getRelative(BlockFace.SOUTH);
				if(adjacentBlock.getType() == Material.STONE) adjacentBlock.setType(Material.COBBLESTONE);
			}
		}
		
		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: breaking a melon stem can result in 0-2 seeds returned
		if(ExtraHardMode.instance.config_seedReduction)
		{
			if(block.getType() == Material.MELON_STEM)
			{
				Collection<ItemStack> drops = block.getDrops();
				drops.clear();
				
				int randomNumber = ExtraHardMode.randomNumberGenerator.nextInt(100);
				
				if(randomNumber >= 30)
				{
					drops.add(new ItemStack(Material.MELON_SEEDS));
				}
				
				if(randomNumber >= 70)
				{
					drops.add(new ItemStack(Material.MELON_SEEDS));
				}				
			}
		}
		
		//FEATURE: breaking a wheat can result in 0-2 seeds returned
		if(ExtraHardMode.instance.config_seedReduction)
		{
			if(block.getType() == Material.CROPS)
			{
				Collection<ItemStack> drops = block.getDrops();
				
				//remove any seeds
				Iterator<ItemStack> iterator = drops.iterator();
				while(iterator.hasNext())
				{
					ItemStack itemStack = iterator.next();
					if(itemStack.getType() == Material.SEEDS)
					{
						iterator.remove();
					}
				}				
				
				//add back in the right amount
				int randomNumber = ExtraHardMode.randomNumberGenerator.nextInt(100);
				
				if(randomNumber >= 50)
				{
					//drops.add(new ItemStack(Material.SEEDS));
				}
			}
		}
		
		//FEATURE: no nether wart farming (always drops exactly 1 nether wart when broken)
		if(ExtraHardMode.instance.config_noFarmingNetherWart)
		{
			if(block.getType() == Material.NETHER_WARTS)
			{
				block.getDrops().clear();
				block.getDrops().add(new ItemStack(Material.NETHER_STALK));
			}
		}
		
		//FEATURE: breaking netherrack may start a fire
		if(ExtraHardMode.instance.config_brokenNetherrackCatchesFirePercent > 0 && block.getType() == Material.NETHERRACK)
		{
			Block underBlock = block.getRelative(BlockFace.DOWN);
			if(underBlock.getType() == Material.NETHERRACK && ExtraHardMode.random(ExtraHardMode.instance.config_brokenNetherrackCatchesFirePercent))
			{
				breakEvent.setCancelled(true);
				block.setType(Material.FIRE);
			}
		}
	}
	
	//when a player places a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent placeEvent)
	{
		Player player = placeEvent.getPlayer();
		Block block = placeEvent.getBlock();
		World world = block.getWorld();
		
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || player.hasPermission("extrahardmode.bypass")) return;
		
		//FIX: prevent players from placing ore as an exploit to work around the hardened stone rule
		if(ExtraHardMode.instance.config_superHardStone && block.getType().name().endsWith("_ORE"))
		{
			Block [] adjacentBlocks = new Block [] {
				block.getRelative(BlockFace.DOWN),
				block.getRelative(BlockFace.UP),
				block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.WEST),
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.SOUTH) };
				
			for(int i = 0; i < adjacentBlocks.length; i++)
			{
				Block adjacentBlock = adjacentBlocks[i];
				if(adjacentBlock.getType() == Material.STONE)
				{
					notifyPlayer(player, Messages.NoPlacingOreAgainstStone, "ehm.silent.no_placing_ore_against_stone", Sound.ARROW_HIT, 10);
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
			
		//FEATURE: no farming nether wart
		if(block.getType() == Material.NETHER_WARTS && ExtraHardMode.instance.config_noFarmingNetherWart)
		{
			placeEvent.setCancelled(true);
			return;
		}
		
		//FEATURE: more falling blocks
		ExtraHardMode.physicsCheck(block, 0, true);
		
		//FEATURE: no standard torches, jack o lanterns, or fire on top of netherrack near diamond level
		if(ExtraHardMode.instance.config_standardTorchMinY > 0)
		{
			if(	world.getEnvironment() == Environment.NORMAL &&
				block.getY() < ExtraHardMode.instance.config_standardTorchMinY &&
				(block.getType() == Material.TORCH || block.getType() == Material.JACK_O_LANTERN || (block.getType() == Material.FIRE && block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)))
				{
					notifyPlayer(player, Messages.NoTorchesHere, "ehm.silent.no_torches_here", Sound.FIZZ, 20);
					placeEvent.setCancelled(true);
					return;
				}
		}
		
		//FEATURE: players can't place blocks from weird angles (using shift to hover over in the air beyond the edge of solid ground)
		//or directly beneath themselves, for that matter
		if(ExtraHardMode.instance.config_limitedBlockPlacement)
		{
			if(	block.getX() == player.getLocation().getBlockX() &&
				block.getZ() == player.getLocation().getBlockZ() &&
				block.getY() <  player.getLocation().getBlockY() )
			{
				notifyPlayer(player, Messages.RealisticBuilding, "ehm.silent.realistic_building_1", Sound.NOTE_STICKS, 1);
				placeEvent.setCancelled(true);
				return;
			}
			
			Block underBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			
			//if standing directly over lava, prevent placement
			if(underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
			{
				notifyPlayer(player, Messages.RealisticBuilding, "ehm.silent.realistic_building_2", Sound.NOTE_STICKS, 1);
				placeEvent.setCancelled(true);
				return;
			}
			
			//otherwise if hovering over air, check one block lower
			else if(underBlock.getType() == Material.AIR)
			{
				underBlock = underBlock.getRelative(BlockFace.DOWN);
				
				//if over lava or more air, prevent placement
				if(underBlock.getType() == Material.AIR || underBlock.getType() == Material.LAVA || underBlock.getType() == Material.STATIONARY_LAVA)
				{
					notifyPlayer(player, Messages.RealisticBuilding, "ehm.silent.realistic_building_3", Sound.NOTE_STICKS, 1);
					placeEvent.setCancelled(true);
					return;
				}
			}
		}
		
		//FEATURE: players can't attach torches to common "soft" blocks
		if(ExtraHardMode.instance.config_limitedTorchPlacement && block.getType() == Material.TORCH)
		{
			Torch torch = new Torch(Material.TORCH, block.getData());
			Material attachmentMaterial = block.getRelative(torch.getAttachedFace()).getType();
			
			if(	attachmentMaterial == Material.DIRT ||
				attachmentMaterial == Material.GRASS ||
				attachmentMaterial == Material.LONG_GRASS ||
				attachmentMaterial == Material.SAND)
			{
				notifyPlayer(player, Messages.LimitedTorchPlacements, "ehm.silent.limited_torch_placement", Sound.FIZZ, 20);
				placeEvent.setCancelled(true);
				return;				
			}
		}
	}
		
	//when a dispenser dispenses...
	void onBlockDispense(BlockDispenseEvent event)
	{
		//FEATURE: can't move water source blocks
		if(ExtraHardMode.instance.config_dontMoveWaterSourceBlocks)
		{
			World world = event.getBlock().getWorld();
			if(!ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
			
			//only care about water
			if(event.getItem().getType() == Material.WATER_BUCKET)
			{
				//plan to evaporate the water next tick
				Block block;
				Vector velocity = event.getVelocity();
				if(velocity.getX() > 0)
				{
					block = event.getBlock().getLocation().add(1, 0, 0).getBlock();
				}
				else if(velocity.getX() < 0)
				{
					block = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
				}
				else if(velocity.getZ() > 0)
				{
					block = event.getBlock().getLocation().add(0, 0, 1).getBlock();
				}
				else
				{
					block = event.getBlock().getLocation().add(0, 0, -1).getBlock();
				}
				
				EvaporateWaterTask task = new EvaporateWaterTask(block);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, 1L);
			}				
		}
	}
	
	//when a piston pushes...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonExtend (BlockPistonExtendEvent event)
	{		
		List<Block> blocks = event.getBlocks();
		World world = event.getBlock().getWorld();
		
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pushing the ore next to stone before breaking it
		
		if(!ExtraHardMode.instance.config_superHardStone || !ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
				
		//which blocks are being pushed?
		for(int i = 0; i < blocks.size(); i++)
		{
			//if any are ore or stone, don't push
			Block block = blocks.get(i);
			Material material = block.getType();
			if(material == Material.STONE || material.name().endsWith("_ORE"))
			{
				event.setCancelled(true);
				return;
			}
		}		
	}
	
	//when a piston pulls...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonRetract (BlockPistonRetractEvent event)
	{
		//FEATURE: prevent players from circumventing hardened stone rules by placing ore, then pulling the ore next to stone before breaking it
		
		//we only care about sticky pistons
		if(!event.isSticky()) return;
		
		Block block = event.getRetractLocation().getBlock();
		World world = block.getWorld();
		
		if(!ExtraHardMode.instance.config_superHardStone || !ExtraHardMode.instance.config_enabled_worlds.contains(world)) return;
		
		Material material = block.getType();
		if(material == Material.STONE || material.name().endsWith("_ORE"))
		{
			event.setCancelled(true);
			return;
		}
	} 
	
	//when the weather changes...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onWeatherChange (WeatherChangeEvent event)
	{
		//FEATURE: rainfall breaks exposed torches (exposed to the sky)
		World world = event.getWorld();
		if(!ExtraHardMode.instance.config_enabled_worlds.contains(world) || !ExtraHardMode.instance.config_rainBreaksTorches) return;
		
		if(!event.toWeatherState()) return;  //if not raining
		
		//plan to remove torches chunk by chunk gradually throughout the rain period
		Chunk [] chunks = world.getLoadedChunks();
		if(chunks.length > 0)
		{
			int startOffset = ExtraHardMode.randomNumberGenerator.nextInt(chunks.length);
			for(int i = 0; i < chunks.length; i++)
			{
				Chunk chunk = chunks[(startOffset + i) % chunks.length];
				
				RemoveExposedTorchesTask task = new RemoveExposedTorchesTask(chunk);
				ExtraHardMode.instance.getServer().getScheduler().scheduleSyncDelayedTask(ExtraHardMode.instance, task, i * 20L);
			}
		}
	} 
	
	//when a block grows...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockGrow (BlockGrowEvent event)
	{
		//FEATURE: fewer seeds = shrinking crops.  when a plant grows to its full size, it may be replaced by a dead shrub
		if(!ExtraHardMode.instance.allowGrow(event.getBlock(), event.getNewState().getData().getData()))
		{
			event.setCancelled(true);
			event.getBlock().setType(Material.LONG_GRASS); //dead shrub
		}
	}
	
	/**
	 * Send the player an informative message to explain what he's doing wrong.
	 * After that play errorsounds instead of spamming the chat window. Uses the permission
	 * to temporarily store which message has been shown already. 
	 * @author diemex
	 * @param player
	 * @param permissionName name of the temporary permission
	 * @param sound errorsound to play after the event got cancelled
	 * @param soundPitch
	 */
	public void notifyPlayer 	(Player player, Messages msgName,
								String permissionName, Sound sound, float soundPitch)
	{
		if (!player.hasPermission(permissionName) && permissionName != null) 
		{
			ExtraHardMode.sendMessage(player, TextMode.Instr, msgName);
			player.addAttachment(ExtraHardMode.instance, permissionName, true);
		}
		try
		{
			player.playSound(player.getLocation(), sound, 1, soundPitch);
		}
		catch (Exception e){
			ExtraHardMode.instance.getLogger().log(Level.WARNING, 
					"Problem playing back sound: " + sound.toString());
		}
	}
	
}
