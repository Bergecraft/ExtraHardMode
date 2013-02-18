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

package me.ryanhamshire.ExtraHardMode.task;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class RemoveExposedTorchesTask implements Runnable
{
	private Chunk chunk;
	
	public RemoveExposedTorchesTask(Chunk chunk)
	{
		this.chunk = chunk;
	}

	@Override
	public void run()
	{
		//if rain has stopped, don't do anything
		if(!this.chunk.getWorld().hasStorm()) return;
		
		for(int x = 0; x < 16; x++)
		{
			for(int z = 0; z < 16; z++)
			{				
				for(int y = chunk.getWorld().getMaxHeight() - 1; y > 0; y--)
				{
					Block block = chunk.getBlock(x, y, z);
					
					if(block.getType() != Material.AIR)
					{
						if(block.getType() == Material.TORCH)
						{
							Biome biome = block.getBiome();
							if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS) break;
							
							block.setType(Material.AIR);
							chunk.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.TORCH, 1));
						}
						else
						{
							break;
						}
					}
				}
			}
		}
	}
}
