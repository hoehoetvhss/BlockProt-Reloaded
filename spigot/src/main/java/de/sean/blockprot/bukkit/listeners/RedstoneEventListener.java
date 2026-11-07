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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneEventListener implements Listener {
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

        if (!BlockProt.getDefaultConfig().isLockable(checkBlock.getType(), checkBlock.getWorld())) return;
        if (!ProtectedBlockCache.isProtected(checkBlock)) return;

        try {
            BlockNBTHandler handler = new BlockNBTHandler(checkBlock);
            if (handler.isProtected() && handler.getRedstoneHandler().getCurrentProtection()) {
                event.setNewCurrent(0);
            }
        } catch (RuntimeException ignored) {}
    }
}