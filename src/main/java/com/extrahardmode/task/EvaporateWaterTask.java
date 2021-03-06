/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.extrahardmode.task;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.module.BlockModule;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Changes a water source block to a non-source block, allowing it to spread and evaporate away.
 */
public class EvaporateWaterTask implements Runnable
{

    /**
     * Target block.
     */
    private final Block block;

    /**
     * Module for Metadata
     */
    private final BlockModule blockModule;


    /**
     * Constructor.
     *
     * @param block - Target block.
     */
    public EvaporateWaterTask(Block block, ExtraHardMode plugin)
    {
        this.block = block;
        blockModule = plugin.getModuleForClass(BlockModule.class);
    }


    @Override
    public void run()
    {
    	boolean isWater = block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER;
    	boolean isLava = block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.LAVA;
        if (isWater || isLava)
        {
            block.setData((byte) 1);
            //Finished processing
            blockModule.removeMark(block);
        }
    }
}
