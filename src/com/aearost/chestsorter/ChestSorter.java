package com.aearost.chestsorter;

import org.bukkit.plugin.java.JavaPlugin;

import com.aearost.chestsorter.event.ChestSort;

public class ChestSorter extends JavaPlugin {

	@Override
	public void onEnable() {
		
		new ChestSort(this);
		
	}

}