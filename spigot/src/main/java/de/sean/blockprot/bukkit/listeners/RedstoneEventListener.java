/*
 * Copyright (C) 2021 - 2026 spnda
 * Modifications Copyright (C) 2025 - 2026 Zaynr (Zar)
 * This file is part of BlockProt Reloaded <https://github.com/VictorGugug/BlockProt-Reloaded>.
 * Based on BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit.listeners;

import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.nbt.BlockNBTHandler;
import de.sean.blockprot.bukkit.storage.ProtectedBlockCache;
import de.sean.blockprot.bukkit.util.BlockUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.jetbrains.annotations.NotNull;

public class RedstoneEventListener implements Listener {
    private static final BlockFace[] NEIGHBOR_FACES = new BlockFace[]{
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (BlockProt.getDefaultConfig().isWorldExcluded(block.getWorld())) return;

        Block checkBlock = block;
        if (BlockProt.getDefaultConfig().isLockableDoor(block.getType(), block.getWorld())) {
            if (!ProtectedBlockCache.isProtected(checkBlock)) {
                Block other = BlockUtil.getOtherDoorHalf(checkBlock.getState());
                if (other != null && ProtectedBlockCache.isProtected(other)) {
                    checkBlock = other;
                }
            }
        }

        if (BlockProt.getDefaultConfig().isLockable(checkBlock.getType(), checkBlock.getWorld())
            && ProtectedBlockCache.isProtected(checkBlock)) {
            try {
                BlockNBTHandler handler = new BlockNBTHandler(checkBlock);
                if (handler.isProtected() && handler.getRedstoneHandler().getCurrentProtection()) {
                    event.setNewCurrent(0);
                    return;
                }
            } catch (RuntimeException ignored) {}
        }

        if (event.getNewCurrent() > 0) {
            for (BlockFace face : NEIGHBOR_FACES) {
                Block neighbor = block.getRelative(face);
                if (isProtectedRedstoneGuarded(neighbor)) {
                    event.setNewCurrent(0);
                    return;
                }
            }
            org.bukkit.block.data.BlockData data = block.getBlockData();
            if (data instanceof org.bukkit.block.data.Directional dir) {
                Block attached = block.getRelative(dir.getFacing().getOppositeFace());
                for (BlockFace face : NEIGHBOR_FACES) {
                    Block neighbor = attached.getRelative(face);
                    if (isProtectedRedstoneGuarded(neighbor)) {
                        event.setNewCurrent(0);
                        return;
                    }
                }
            }
        }
    }

    private boolean isProtectedRedstoneGuarded(@NotNull Block block) {
        if (!ProtectedBlockCache.isProtected(block)) {
            if (block.getBlockData() instanceof Door) {
                Block other = BlockUtil.getOtherDoorHalf(block.getState());
                if (other == null || !ProtectedBlockCache.isProtected(other)) return false;
                block = other;
            } else {
                return false;
            }
        }
        if (!BlockProt.getDefaultConfig().isLockable(block.getType(), block.getWorld())) return false;
        try {
            BlockNBTHandler handler = new BlockNBTHandler(block);
            return handler.isProtected() && handler.getRedstoneHandler().getCurrentProtection();
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}