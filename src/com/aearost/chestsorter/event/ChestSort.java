package com.aearost.chestsorter.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.aearost.chestsorter.ChestSorter;

public class ChestSort implements Listener {

	public ChestSort(ChestSorter plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Handles the sorting of a chest
	 * 
	 * @param e
	 */
	@EventHandler
	public void onShopCreate(final PlayerInteractEvent e) {
		
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.LEFT_CLICK_BLOCK) {
			Block block = e.getClickedBlock();
			if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
				Player player = e.getPlayer();
				if (player.isSneaking()) {
					Chest chest = (Chest) block.getState();
					ItemStack[] contents = chest.getInventory().getStorageContents();
					
					
				}
			}
			
			
		}
		
	}

}
