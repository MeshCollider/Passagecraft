package net.meshcollision.PassageCraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

public class PCConfig {
	PassageCraft plugin;
	
	File mapsFile;
	FileConfiguration mapsconfig;
	File dataFile;
	FileConfiguration dataconfig;
	File kitsFile;
	FileConfiguration kitsconfig;
	
	public PCConfig(PassageCraft plugin)
	{
		this.plugin = plugin;
		
	}
	
	public void enable()
	{
		mapsFile = new File(plugin.getDataFolder() + File.separator + "maps.yml");
		dataFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
		kitsFile = new File(plugin.getDataFolder() + File.separator + "kits.yml");
		
		InputStream defConfigStream = plugin.getResource("config.yml");
		FileConfiguration defconf = YamlConfiguration.loadConfiguration(defConfigStream);
		
		InputStream defConfigStream2 = plugin.getResource("kits.yml");
		FileConfiguration defconf2 = YamlConfiguration.loadConfiguration(defConfigStream2);
		
		dataconfig = YamlConfiguration.loadConfiguration(dataFile);
		mapsconfig = YamlConfiguration.loadConfiguration(mapsFile);
		kitsconfig = YamlConfiguration.loadConfiguration(kitsFile);
		
	    dataconfig.addDefaults(defconf);
	    dataconfig.setDefaults(defconf);
	    dataconfig.options().copyDefaults(true);
	    
	    kitsconfig.addDefaults(defconf2);
	    kitsconfig.setDefaults(defconf2);
	    kitsconfig.options().copyDefaults(true);
	    
	    plugin.saveDefaultConfig();
		
	    loadKitData();
		loadMapData();
		
	}

	public void disable()
	{
		try
		{
			if(!mapsFile.delete())
			{
				Bukkit.getServer().broadcastMessage(plugin.errorcolor + "Error deleting the maps config file");
			}
			else
			{
				mapsFile.createNewFile();
			}
			mapsconfig = YamlConfiguration.loadConfiguration(mapsFile);
			saveMapData();
			saveKitData();
			dataconfig.save(dataFile);
			mapsconfig.save(mapsFile);
			kitsconfig.save(kitsFile);
		}
		catch(Exception e)
		{
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "Error saving YML files: " + e.getMessage());
		}
	}

	private void saveMapData() {
		mapsconfig.set("hub", plugin.hub);
		for(Map map : plugin.maps)
		{
			mapsconfig.set(map.name + ".min", map.minPlayers);
			mapsconfig.set(map.name + ".max", map.maxPlayers);
			mapsconfig.set(map.name + ".lobby", map.lobby);
			mapsconfig.set(map.name + ".spawnpoints", map.spawnpoints);
			mapsconfig.set(map.name + ".name", map.name);
			mapsconfig.set(map.name + ".nr1", map.nukeroom1);
			mapsconfig.set(map.name + ".nr2", map.nukeroom2);
			mapsconfig.set(map.name + ".protect1", map.protect1);
			mapsconfig.set(map.name + ".protect2", map.protect2);
			if(map.kit != null)
				mapsconfig.set(map.name + ".kit", map.kit.name);
			else
				mapsconfig.set(map.name + ".kit", "null");
		}
	}
	
	private void loadMapData() {
		if(mapsconfig.isVector("hub"));
			plugin.hub = mapsconfig.getVector("hub");
		Set<String> mapsnames = mapsconfig.getKeys(false);
		if(mapsnames.isEmpty())
			return;
		for(String mapname : mapsnames)
		{
			if(mapname.equalsIgnoreCase("hub"))
				continue;
			Map map = new Map(mapname);
			if(mapsconfig.isVector(mapname + ".lobby"))
				map.lobby = mapsconfig.getVector(mapname + ".lobby");
			try{
				if(mapsconfig.isList(mapname + ".spawnpoints")){
					List<Vector> spawns = (List<Vector>) mapsconfig.getList(mapname + ".spawnpoints");
					if(!spawns.isEmpty())
					{
						map.spawnpoints[0] = (Vector) spawns.get(0);
						map.spawnpoints[1] = (Vector) spawns.get(1);
						map.spawnpoints[2] = (Vector) spawns.get(2);
						map.spawnpoints[3] = (Vector) spawns.get(3);
					}
				}
			}
			catch(Exception e)
			{
				Bukkit.getServer().broadcastMessage(plugin.errorcolor + "Error loading spawn points data");
			}
			if(mapsconfig.isVector(map.name + ".nr1"))
				map.nukeroom1 = mapsconfig.getVector(map.name + ".nr1");
			if(mapsconfig.isVector(map.name + ".nr2"))
				map.nukeroom2 = mapsconfig.getVector(map.name + ".nr2");
			if(mapsconfig.isVector(map.name + ".protect1"))
				map.protect1 = mapsconfig.getVector(map.name + ".protect1");
			if(mapsconfig.isVector(map.name + ".protect2"))
				map.protect2 = mapsconfig.getVector(map.name + ".protect2");
			if(!plugin.kits.isEmpty())
			{
				for(Kit kit : plugin.kits)
				{
					if(kit.name.equalsIgnoreCase(mapsconfig.getString(map.name + ".kit")))
					{
						map.kit = kit;
					}
				}
			}
			map.minMax(mapsconfig.getInt(map.name + ".min"), mapsconfig.getInt(map.name + ".max"));
			plugin.maps.add(map);
		}
	}
	
	private void loadKitData() {
		Set<String> kits = kitsconfig.getKeys(false);
		if(kits.isEmpty()) 
			return;
		for(String s : kits)
		{
			Kit kit = new Kit(kitsconfig.getString(s + ".name"));
			String items = kitsconfig.getString(s + ".items");
			String[] items1 = items.split(",");
			for(String item : items1)
			{
				String[] itemComponents = item.split("-");
				kit.addItem(Integer.parseInt(itemComponents[0]), Integer.parseInt(itemComponents[1]));
			}
			plugin.kits.add(kit);
		}
		
	}

	private void saveKitData() {
		// TODO Auto-generated method stub
		
	}
}
