package net.meshcollision.PassageCraft;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class PassageCraft extends JavaPlugin {
	List<Map> maps = new ArrayList<Map>();
	List<Kit> kits = new ArrayList<Kit>();
	public Vector hub = new Vector(1,1,1);
	List<Vector> traps = new ArrayList<Vector>();

	PCConfig configs = new PCConfig(this);
	PCPlayerListener plistener = new PCPlayerListener(this);
	PCBlockListener blistener = new PCBlockListener(this);

	//CHAT COLORS
	String createcolor = "§A";
	String errorcolor = "§C";
	String publishcolor = "§E";
	String infocolor = "§B";
	String scorestitle = ChatColor.BOLD + "" + ChatColor.GOLD;

	@Override
	public void onEnable(){
		getLogger().info("PassageCraft has been enabled");

		this.getServer().getPluginManager().registerEvents(this.plistener, this);
		this.getServer().getPluginManager().registerEvents(this.blistener, this);

		configs.enable();

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		errorcolor = configs.dataconfig.getString("ErrorColour");
		createcolor = configs.dataconfig.getString("CreateColour");
		publishcolor = configs.dataconfig.getString("PublishColour");
		infocolor = configs.dataconfig.getString("InfoColour");
	}

	@Override
	public void onDisable() {
		getLogger().info("PassageCraft has been disabled");
		configs.disable();
	}

	public void startmap(final Map map) {	
		Bukkit.getServer().broadcastMessage(publishcolor + replaceStrings(configs.dataconfig.getString("MapWaitMessage"), null, map.name));
		long waittime = configs.dataconfig.getInt("MapBeginTime");
		long maptime = configs.dataconfig.getInt("MapLengthTime");
		// 20 ticks == 1 sec
		maptime = maptime * 20L;
		waittime = waittime * 20L; 

		//run the map
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			public void run() {
				if(map.running != Map.State.WAITING) return;
				map.running = Map.State.RUNNING;
				Random randomGenerator = new Random();
				Bukkit.getServer().broadcastMessage(publishcolor + replaceStrings(configs.dataconfig.getString("MapStart"), null, map.name));
				for(Player player : map.players)
				{
					int randomInt = randomGenerator.nextInt(4);
					Vector tp = map.spawnpoints[randomInt];
					Location location = new Location(player.getWorld(), tp.getX(), tp.getY(), tp.getZ());
					player.teleport(location);
					player.setGameMode(GameMode.SURVIVAL);
					player.setHealth(player.getMaxHealth());
				}
			}
		}, waittime);
		final Map mapfinal = map;
		//countdown to map end
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if(mapfinal.running == Map.State.RUNNING)
					Bukkit.getServer().broadcastMessage(publishcolor + "Map ending in: " + errorcolor + "3");
			}
		}, waittime + maptime - 60L);
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if(mapfinal.running == Map.State.RUNNING)
					Bukkit.getServer().broadcastMessage(errorcolor + "2");
			}
		}, waittime + maptime - 40L);
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if(mapfinal.running == Map.State.RUNNING)
					Bukkit.getServer().broadcastMessage(errorcolor + "1");
			}
		}, waittime + maptime - 20L);

		//end the map
		this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if(mapfinal.running == Map.State.RUNNING)
					endmap(map);
			}
		}, waittime + maptime);
	}

	public void endmap(final Map map1) {
		final Map map = maps.get(maps.indexOf(map1));
		if(map.running == Map.State.RUNNING)
		{
			maps.get(maps.indexOf(map1)).running = Map.State.UNAVAILABLE;
			Bukkit.getServer().broadcastMessage(publishcolor + replaceStrings(configs.dataconfig.getString("MapEnd"), null, map.name));
			StringBuilder nuked = new StringBuilder();
			StringBuilder saved = new StringBuilder();

			for(Player player : map.players)
			{
				player.setGameMode(GameMode.SURVIVAL);
				player.getInventory().clear();

				if(map.playerdata.get(map.players.indexOf(player)).nukeroom == false)
				{
					player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 3, false, false);
					//player.getWorld().createExplosion(player.getLocation(), 0);
					player.setHealth(0);
					nuked.append(player.getName() + ", ");
				}
				else
				{
					Vector tp = hub;
					Location location = new Location(player.getWorld(), tp.getX(), tp.getY(), tp.getZ());
					player.teleport(location);
					saved.append(player.getName() + ", ");
				}

			}
			if(saved.length() > 2)
				saved.delete(saved.length() - 2, saved.length());
			if(nuked.length() > 2)
				nuked.delete(nuked.length() - 2, nuked.length());

			String survivemessage;
			String killmessage;

			if(nuked.length() <= 1)
				killmessage = configs.dataconfig.getString("EveryoneSurvivedMessage").replace("[MAPNAME]", map.name);
			else
				killmessage = configs.dataconfig.getString("MapKilledMessage").replace("[PLAYERS]", nuked.toString()).replace("[MAPNAME]", map.name);

			if(saved.length() <= 1)
				survivemessage = configs.dataconfig.getString("EveryoneDiedMessage").replace("[MAPNAME]", map.name);
			else
				survivemessage = configs.dataconfig.getString("MapSurvivedMessage").replace("[PLAYERS]", nuked.toString()).replace("[MAPNAME]", map.name);


			Bukkit.getServer().broadcastMessage(errorcolor + killmessage);
			Bukkit.getServer().broadcastMessage(publishcolor + survivemessage);
		}
		map.reload(this);
	}

	public void setRegion(Vector hitlocation, Vector hitlocation2, boolean nukeroom2, boolean protect2) {
		for(Map map : maps)
		{
			if(map.editing)
			{
				map.editing = false;
				if(nukeroom2)
				{
					map.nukeroom1 = hitlocation;
					map.nukeroom2 = hitlocation2;
				}
				else if(protect2)
				{
					map.protect1 = hitlocation;
					map.protect2 = hitlocation2;
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		//DEFAULT COMMAND
		if(cmd.getName().equalsIgnoreCase("passagecraft")){
			sender.sendMessage(publishcolor + replaceStrings(configs.dataconfig.getString("PassageCraft"), sender.getName(), null));
			if(args.length > 0)
			{
				if(args[0].equals("credits") || args[0].equals("code") || args[0].equals("developer"))
					sender.sendMessage(publishcolor + "Developed by MeshCollider");
			}
			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pctrap")){
			if(sender instanceof Player)
			{
				Player player = (Player) sender;
				traps.add(player.getLocation().toVector());
				FallingBlock block = ((Player)sender).getWorld().spawnFallingBlock(((Player)sender).getLocation(), 2, (byte) 0);
			}
			return true;
		}

		//DISPLAYS THE SCOREBOARD FOR THE SPECIFIED MAP
		else if(cmd.getName().equalsIgnoreCase("pcscores")){
			if(args.length == 1)
			{
				int index = -1;
				for(Map map : maps)
				{
					if(map.name.equalsIgnoreCase(args[0]))
					{
						index = maps.indexOf(map);
					}
				}
				if(!(index == -1))
				{
					sender.sendMessage(scorestitle + "MAP: " + ChatColor.AQUA + args[0]);
					List<Player> players = maps.get(index).players;
					for(int i = 0; i < players.size(); i++)
					{
						sender.sendMessage(ChatColor.BLUE + players.get(i).getName() + " - " + ChatColor.RED + maps.get(index).playerdata.get(i).score);
					}
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pccreate")){
			if(args.length == 1)
			{
				boolean exists = false;
				for(Map map : maps)
				{
					if(map.name.equalsIgnoreCase(args[0]))
					{
						sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("AlreadyExistsMessage"), sender.getName(), args[0]));
						exists = true;
					}
				}
				if(!exists)
				{
					Map map = new Map(args[0]);
					map.minMax(configs.dataconfig.getInt("MinPlayers"), configs.dataconfig.getInt("MaxPlayers"));
					maps.add(map);
					sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("CreateMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pclobby")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				if(args.length == 1)
				{
					boolean found = false;

					for(Map map : maps){
						if(map.name.equalsIgnoreCase(args[0]))
						{
							Player player = (Player) sender;
							Vector location = player.getLocation().toVector();
							map.lobby = location;
							sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("LobbyMessage"), sender.getName(), args[0]));
							found = true;
						}
					}
					if(!found){
						sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
					}
					return true;
				}
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcjoin") || cmd.getName().equalsIgnoreCase("pcvipjoin") || cmd.getName().equalsIgnoreCase("pcforcejoin")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				if(args.length == 1)
				{
					Player player = (Player) sender;
					int index = -1;
					Map map = new Map("");
					for(Map map1 : maps){
						if(map1.name.equalsIgnoreCase(args[0]))
						{
							if(map1.players.contains(player))
							{
								sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("AlreadyJoinedMessage"), sender.getName(), map1.name));
								return true;
							}
							index = maps.indexOf(map1);
							map = map1;
						}
					}
					if(!(index == -1)){
						if((map.players.size() <= map.maxPlayers && map.running != Map.State.RUNNING && map.running != Map.State.UNAVAILABLE) || (cmd.getName().equalsIgnoreCase("pcvipjoin") && map.running != Map.State.RUNNING && map.running != Map.State.UNAVAILABLE) || (cmd.getName().equalsIgnoreCase("pcforcejoin") && map.running != Map.State.UNAVAILABLE))
						{
							sender.sendMessage(publishcolor + replaceStrings(configs.dataconfig.getString("JoinMessage"), sender.getName(), map.name));

							Location location = new Location(player.getWorld(), map.lobby.getX(), map.lobby.getY(), map.lobby.getZ());
							player.teleport(location);
							map.addPlayer(player);
							Bukkit.getServer().broadcastMessage(replaceStrings(configs.dataconfig.getString("PublicJoinMessage"), player.getName(), map.name));
							player.setHealth(player.getMaxHealth());
							player.getInventory().clear();
							if(map.kit != null && !(map.kit.items.isEmpty()))
							{
								for(int itemid : map.kit.items)
								{
									player.getInventory().addItem(new ItemStack(itemid, map.kit.quantities.get(map.kit.items.indexOf(itemid))));
								}
							}
							if(map.players.size() >= map.minPlayers && map.running == Map.State.READY)
							{
								map.running = Map.State.WAITING;
								startmap(map);
							}
						}
						else{
							if(map.running != Map.State.RUNNING)
							{
								if(map.running == Map.State.UNAVAILABLE)
								{
									sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapUnavailableMessage"), sender.getName(), map.name));
								}
								else
								{
									sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapFullMessage"), sender.getName(), map.name));
								}
							}
							else
							{
								sender.sendMessage(replaceStrings(configs.dataconfig.getString("MapInProgressMessage"), sender.getName(), map.name));
							}
						}
					}
					else{
						sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
					}
					return true;
				}
				return false;
			}
			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pcspawnpoint")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				if(args.length == 2)
				{
					Map selectedmap = new Map("");
					boolean found = false;
					for(Map map : maps)
					{
						if(map.name.equalsIgnoreCase(args[0]))
						{
							selectedmap = map;
							found = true;
						}
					}
					if(found){
						if(args[1].equals("1") || args[1].equals("2") || args[1].equals("3") || args[1].equals("4"))
						{
							Player player = (Player) sender;
							Location location = player.getLocation();
							selectedmap.spawnpoints[(Integer.parseInt(args[1]) - 1)] = location.toVector();
							sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("SpawnPointMessage"), sender.getName(), selectedmap.name).replace("[SPAWNNUMBER]", args[1]));
						}
						else
						{
							sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("SpawnNumberMessage"), sender.getName(), selectedmap.name));
						}
					}
					else{
						sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
					}
					return true;
				}
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcspawnpoints")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				if(args.length == 1)
				{
					Map selectedmap = new Map("");
					boolean found = false;
					for(Map map : maps)
					{
						if(map.name.equalsIgnoreCase(args[0]))
						{
							selectedmap = map;
							found = true;
						}
					}
					if(found){
						Player player = (Player) sender;
						Location location = player.getLocation();
						selectedmap.spawnpoints[0] = location.toVector();
						selectedmap.spawnpoints[1] = location.toVector();
						selectedmap.spawnpoints[2] = location.toVector();
						selectedmap.spawnpoints[3] = location.toVector();
						sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("SpawnPointMessage"), sender.getName(), selectedmap.name).replace("[SPAWNNUMBER]", "all"));
					}
					else{
						sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
					}
					return true;
				}
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcquit")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				int mapindex = -1;
				int playerindex = -1;
				Map map;
				for(Map map1 : maps){
					for(Player player : map1.players){
						if(((Player)sender).getName().equalsIgnoreCase(player.getName()))
						{
							mapindex = maps.indexOf(map1);
							map = map1;
							playerindex = map.players.indexOf(player);
						}
					}
				}
				Player player = (Player) sender;
				if (!(playerindex == -1))
				{
					Vector vector = hub;
					Location location = new Location(player.getWorld(), vector.getX(), vector.getY(), vector.getZ());
					player.teleport(location);
					Bukkit.getServer().broadcastMessage(replaceStrings(configs.dataconfig.getString("PublicQuitMessage"), player.getName(), maps.get(mapindex).name));
					maps.get(mapindex).removePlayer(player);
					player.getInventory().clear();
					player.setGameMode(GameMode.SURVIVAL);
					if(maps.get(mapindex).players.isEmpty())
					{
						Bukkit.getServer().broadcastMessage(replaceStrings(configs.dataconfig.getString("EmptyMapMessage"), null, maps.get(mapindex).name));
						maps.get(mapindex).running = Map.State.UNAVAILABLE;
						maps.get(mapindex).reload(this);

					}
					player.sendMessage(replaceStrings(configs.dataconfig.getString("QuitMessage"), player.getName(), maps.get(mapindex).name));
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NotJoinedMessage"), player.getName(), args[0]));
				}
				return true;
			}
		}

		else if(cmd.getName().equalsIgnoreCase("pchub")){
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				Player player = (Player) sender;
				hub = new Vector();
				hub = player.getLocation().toVector();
				sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("HubMessage"), player.getName(), null));
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcnukeroom")){
			if(!(args.length == 1)) return false;
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				boolean found = false;
				for(Map map1 : maps)
				{
					if(args[0].equalsIgnoreCase(map1.name));
					{
						found = true;
						map1.editing = true;
					}
				}
				if(found)
				{
					sender.sendMessage(infocolor + "NUKEROOM SELECTION MODE ACTIVE");
					ItemStack is = new ItemStack(Material.WOOD_AXE, 1);
					ItemMeta im = is.getItemMeta();
					im.setDisplayName("Nukeroom editor");
					is.setItemMeta(im);
					if(((Player)sender).getInventory().contains(is)) 
						((Player)sender).getInventory().remove(is);
					//((Player)sender).getInventory().addItem(is);
					((Player)sender).setItemInHand(is);
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcprotect")){
			if(args.length != 1) return false;
			if (!(sender instanceof Player)) {
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), sender.getName(), null));
			} else {
				boolean found = false;
				for(Map map1 : maps)
				{
					if(args[0].equalsIgnoreCase(map1.name));
					{
						found = true;
						map1.editing = true;
					}
				}
				if(found)
				{
					sender.sendMessage(infocolor + "PROTECTION REGION SELECTION MODE ACTIVE");
					ItemStack is = new ItemStack(Material.WOOD_AXE, 1);
					ItemMeta im = is.getItemMeta();
					im.setDisplayName("Protection editor");
					is.setItemMeta(im);
					if(((Player)sender).getInventory().contains(is)) 
						((Player)sender).getInventory().remove(is);
					//((Player)sender).getInventory().addItem(is);
					((Player)sender).setItemInHand(is);
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcmaps")){
			StringBuilder sb = new StringBuilder();

			if(maps.isEmpty())
			{
				sb.append(infocolor + replaceStrings(configs.dataconfig.getString("NoMapsMessage"), sender.getName(), null));
			}
			else
			{
				for(Map map : maps)
				{
					sb.append(map.name);
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());	
			}
			sender.sendMessage(sb.toString());
			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pcpublish")){
			if(args.length == 1)
			{
				Map map = new Map("");
				boolean found = false;
				for(Map map1 : maps)
				{
					if(map1.name.equalsIgnoreCase(args[0]))
					{
						map = map1;
						found = true;
					}
				}
				if(found)
				{
					Bukkit.getServer().broadcastMessage(publishcolor + replaceStrings(configs.dataconfig.getString("PublishMessage"), sender.getName(), map.name));
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcdelete")){
			if(args.length == 1)
			{
				Map map = new Map("");
				boolean found = false;
				for(Map map1 : maps)
				{
					if(map1.name.equalsIgnoreCase(args[0]))
					{
						map = map1;
						found = true;
					}
				}
				if(found)
				{
					maps.remove(map);
					sender.sendMessage(createcolor + replaceStrings(configs.dataconfig.getString("DeleteMessage"), sender.getName(), map.name));
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcgotohub")){
			if(sender instanceof Player)
			{
				Player player = (Player) sender;
				player.teleport(new Location(player.getWorld(), hub.getX(), hub.getY(), hub.getZ()));
			}
			else
			{
				sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("NonPlayerMessage"), null, null));
			}
			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pckit")){
			if(args.length == 2)
			{
				boolean found = false;
				Map map = null;
				for(Map map1 : maps)
				{
					if(map1.name.equalsIgnoreCase(args[0]))
					{
						found = true;
						map = map1;
					}
				}
				if(found)
				{
					found = false;
					for(Kit kit : kits)
					{
						if(kit.name.equalsIgnoreCase(args[1]))
						{
							map.kit = kit;
							found = true;
							sender.sendMessage(infocolor + (replaceStrings(configs.dataconfig.getString("KitSetMessage"), sender.getName(), args[0])).replace("[KITNAME]", args[1]));
						}
					}
					if(!found)
					{
						sender.sendMessage(errorcolor + (replaceStrings(configs.dataconfig.getString("KitNotFoundMessage"), sender.getName(), args[0])).replace("[KITNAME]", args[1]));
					}
				}
				else
				{
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pckits")){
			StringBuilder sb = new StringBuilder();

			if(kits.isEmpty())
			{
				sb.append(infocolor + replaceStrings(configs.dataconfig.getString("NoKitsMessage"), sender.getName(), null));
			}
			else
			{
				for(Kit kit : kits)
				{
					sb.append(kit.name);
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());	
			}
			sender.sendMessage(sb.toString());
			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pcexplode")){
			if(args.length == 1)
			{
				Player player = getServer().getPlayer(args[0]);
				player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 3, false, false);
			}
			else if(args.length == 2)
			{
				Player player = getServer().getPlayer(args[0]);
				player.getWorld().createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), Float.parseFloat(args[1]), false, false);
			}

			return true;
		}

		else if(cmd.getName().equalsIgnoreCase("pcplayers")){
			if(args.length == 3)
			{
				for(Map map : maps)
				{
					if(map.name.equalsIgnoreCase(args[0]))
					{
						map.minPlayers = Integer.parseInt(args[1]);
						map.maxPlayers = Integer.parseInt(args[2]);
						sender.sendMessage(infocolor + replaceStrings(configs.dataconfig.getString("PlayerSetMessage"), sender.getName(), map.name));
					}
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcjoinserver")){
			if(args.length == 1 && sender instanceof Player)
			{
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);
				try{
					out.writeUTF("Connect");
					out.writeUTF(args[1]);
				}
				catch(Exception e)
				{
					sender.sendMessage(errorcolor + "Error");
				}
				((Player)sender).sendPluginMessage((Plugin)this,"BungeeCord", b.toByteArray());
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pckitdetails")){
			if(args.length == 1)
			{
				Kit kit = new Kit("");
				boolean found = false;
				for(Kit kit1 : kits)
				{
					if(kit1.name.equalsIgnoreCase(args[0]))
					{
						kit = kit1;
						found = true;
					}
				}
				if(found)
				{
					sender.sendMessage(infocolor + configs.dataconfig.getString("KitInfoMessage").replace("[KITNAME]", args[0]));
					for(int i : kit.items)
					{
						Material mat = Material.getMaterial(i);
						sender.sendMessage(infocolor + "" + mat.name() + "(" + i + ")" + " x " + kit.quantities.get(kit.items.indexOf(i)));
					}
				}
				else
				{
					sender.sendMessage(errorcolor + configs.dataconfig.getString("KitNotFoundMessage").replace("[KITNAME]", args[0]));
				}
				return true;
			}
			return false;
		}

		else if(cmd.getName().equalsIgnoreCase("pcdetails")){
			if(args.length == 1)
			{
				Map map = new Map("");
				boolean found = false;
				for(Map map1 : maps)
				{
					if(map1.name.equalsIgnoreCase(args[0]))
					{
						map = map1;
						found = true;
					}
				}
				if(found)
				{
					sender.sendMessage(infocolor + replaceStrings(configs.dataconfig.getString("InfoMessage"), sender.getName(), map.name));
					sender.sendMessage(infocolor + "Name: " + map.name);
					sender.sendMessage(infocolor + "Lobby Location: " + (int)map.lobby.getX() + " " + (int)map.lobby.getY() + " " + (int)map.lobby.getZ());
					StringBuilder sb = new StringBuilder();
					sb.append("SpawnPoints: ");

					sb.append("1. (");
					sb.append((int)map.spawnpoints[0].getX() + " " + (int)map.spawnpoints[0].getY() + " " + (int)map.spawnpoints[0].getZ());
					sb.append("), ");

					sb.append("2. (");
					sb.append((int)map.spawnpoints[1].getX() + " " + (int)map.spawnpoints[1].getY() + " " + (int)map.spawnpoints[1].getZ());
					sb.append("), ");

					sb.append("3. (");
					sb.append((int)map.spawnpoints[2].getX() + " " + (int)map.spawnpoints[2].getY() + " " + (int)map.spawnpoints[2].getZ());
					sb.append("), ");

					sb.append("4. (");
					sb.append((int)map.spawnpoints[3].getX() + " " + (int)map.spawnpoints[3].getY() + " " + (int)map.spawnpoints[3].getZ());
					sb.append(")");

					sender.sendMessage(infocolor + sb.toString());

					StringBuilder sb3 = new StringBuilder();
					StringBuilder sb4 = new StringBuilder();

					sb3.append("Nukeroom: ");
					sb3.append("(" + (int)map.nukeroom1.getX() + " " + (int)map.nukeroom1.getY() + " " + (int)map.nukeroom1.getZ() + ") - (" 
							+ (int)map.nukeroom2.getX() + " " + (int)map.nukeroom2.getY() + " " + (int)map.nukeroom2.getZ() + ")");

					sb4.append("Protect: ");
					sb4.append("(" + (int)map.protect1.getX() + " " + (int)map.protect1.getY() + " " + (int)map.protect1.getZ() + ") - (" 
							+ (int)map.protect2.getX() + " " + (int)map.protect2.getY() + " " + (int)map.protect2.getZ() + ")");

					sender.sendMessage(infocolor + sb3.toString());
					sender.sendMessage(infocolor + sb4.toString());

					if(map.kit != null)
						sender.sendMessage(infocolor + "Kit: " + map.kit.name);
					else
						sender.sendMessage(infocolor + "Kit: " + "null");
					sender.sendMessage(infocolor + "Min/Max Players: " + map.minPlayers + "/" + map.maxPlayers);
					sender.sendMessage(publishcolor + "Running: " + map.running);
					StringBuilder sb2 = new StringBuilder();
					sb2.append("Players: ");
					if(!map.players.isEmpty())
					{
						for(int i = 0; i < map.players.size(); i++)
						{
							sb2.append(map.players.get(i).getName() + ", ");

						}
						sb2.delete(sb2.length() - 2, sb2.length());
						sender.sendMessage(sb2.toString());
					}

				}
				else
				{
					sender.sendMessage(infocolor + "Hub Location: " + (int)hub.getX() + " " + (int)hub.getY() + " " + (int)hub.getZ());
					sender.sendMessage(errorcolor + replaceStrings(configs.dataconfig.getString("MapNotFoundMessage"), sender.getName(), args[0]));
				}
				return true;
			}
			return false;
		}

		//command not found
		return false;
	}

	public String replaceStrings(String text, String player, String mapname)
	{
		try
		{
			if(text != null)
			{
				if(mapname != null)
					text = text.replace("[MAPNAME]", mapname);
				if(player != null)
					text = text.replace("[PLAYER]", player);
			}
			else
			{
				text = errorcolor + "The text recieved was null";
			}
			return text;
		}
		catch(Exception e)
		{
			return errorcolor + "An error occured replacing the strings";
		}
	}
}