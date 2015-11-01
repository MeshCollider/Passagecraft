package net.meshcollision.PassageCraft;

import java.util.Random;

import net.meshcollision.PassageCraft.Map.State;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PCPlayerListener implements Listener {
	PassageCraft plugin;

	public PCPlayerListener(PassageCraft plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Map map = null;
		boolean found = false;

		for(Map map1 : plugin.maps){
			for(Player player1 : map1.players)
			{
				if(player1.getName().equals(player.getName()))
				{
					map = map1;
					found = true;
				}
			}
		}
		if (found)
		{
			Vector vector = plugin.hub;
			Location location = new Location(player.getWorld(), vector.getX(), vector.getY(), vector.getZ());
			player.teleport(location);
			Bukkit.getServer().broadcastMessage(plugin.replaceStrings(plugin.configs.dataconfig.getString("PublicQuitMessage"), player.getName(), map.name));
			map.removePlayer(player);
			player.getInventory().clear();
			player.setGameMode(GameMode.SURVIVAL);
			if(map.players.isEmpty())
			{
				plugin.endmap(map);
			}
			player.sendMessage(plugin.replaceStrings(plugin.configs.dataconfig.getString("QuitMessage"), player.getName(), map.name));
		}
		else
		{
			//The player is not in a map
		}
	}

	@EventHandler
	public void onKill(PlayerDeathEvent e)
	{
		Player killed = e.getEntity();
		Player killer = e.getEntity().getKiller();
		for(Map map : plugin.maps){
			for(Player player : map.players)
			{
				if(player.getName().equalsIgnoreCase(killed.getName()) && map.running == Map.State.RUNNING)
				{
					e.setDeathMessage(plugin.errorcolor + plugin.configs.dataconfig.getString("KillMessage").replace("[PLAYERNAME1]",  killed.getName()).replace("[PLAYERNAME2]", killer.getName()).replace("MAPNAME", map.name));
					map.playerdata.get(map.players.indexOf(killer)).score += 1;

					map.playerdata.get(map.players.indexOf(killed)).killstreak = 0;
					int killscore = map.playerdata.get(map.players.indexOf(killer)).killstreak + 1;
					map.playerdata.get(map.players.indexOf(killer)).killstreak += 1;
					if(killscore >= plugin.configs.dataconfig.getInt("KillStreak"))
					{
						plugin.getServer().broadcastMessage(plugin.publishcolor + plugin.configs.dataconfig.getString("KillStreakMessage").replace("[KILLSTREAK]", Integer.toString(killscore)).replace("[PLAYER]", killer.getName()));
						boolean found = false;
						for(Kit kit : plugin.kits)
						{
							if(kit.name.equalsIgnoreCase("killstreak") && plugin.configs.dataconfig.getBoolean("KillStreakAwards"))
							{
								found = true;
								for(int itemid : kit.items)
								{
									ItemStack is = new ItemStack(itemid, kit.quantities.get(kit.items.indexOf(itemid)));
									killer.getInventory().addItem(is);
								}
								killer.sendMessage(plugin.publishcolor + plugin.configs.dataconfig.getString("KillStreakAwardMessage"));
							}
						}
						if(!found)
						{
							killer.sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("KitNotFoundMessage"), killer.getName(), null).replace("[KITNAME]", "killstreak"));
						}
					}
					
					if(!plugin.configs.dataconfig.getBoolean("InvincibleInNukeroom") && map.playerdata.get(map.players.indexOf(player)).nukeroom)
					{
						map.playerdata.get(map.players.indexOf(player)).nukeroom = false;
						Bukkit.getServer().broadcastMessage(plugin.publishcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("KilledNukeroomMessage"), player.getName(), map.name));
					}
				}
				else if(player.getName().equalsIgnoreCase(killed.getName()) && map.running == Map.State.WAITING)
				{
					e.setDeathMessage("");
				}
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		for(Map map : plugin.maps)
		{
			if(map.players.contains(player) && map.running == Map.State.RUNNING) //the player has been killed in the map game
			{
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(4);
				Vector tp = map.spawnpoints[randomInt];
				Location location = new Location(player.getWorld(), tp.getX(), tp.getY(), tp.getZ());
				if(map.playerdata.get(map.players.indexOf(event.getPlayer())).checkpoint == true)
				{
					Vector spawn = map.playerdata.get(map.players.indexOf(event.getPlayer())).spawnpoint;
					location = new Location(player.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ());
				}
				event.setRespawnLocation(location);
			}
			else if(map.players.contains(player) && map.running == Map.State.WAITING) //the player has been killed in the lobby
			{
				Vector tp = map.lobby;
				Location location = new Location(player.getWorld(), tp.getX(), tp.getY(), tp.getZ());
				event.setRespawnLocation(location);
			}
			else if(map.players.contains(player)) //the map has ended and the player has died in the explosion
			{
				Vector tp = plugin.hub;
				Location location = new Location(player.getWorld(), tp.getX(), tp.getY(), tp.getZ());
				event.setRespawnLocation(location);
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		Location to = event.getTo();
		Location from = event.getFrom();

		if(to.getX() == from.getX() && to.getY() == from.getY() && to.getZ() == from.getZ())
			return; //the player has not moved physically

		boolean ingame = false;
		Map map = null;
		
		for(Map map1 : plugin.maps)
		{
			if(map1.players.contains(event.getPlayer())) 
			{
				ingame = true;
				map = map1;
			}
		}
		
		if(ingame)
		{
			Vector nukeroom1 = map.nukeroom1;
			Vector nukeroom2 = map.nukeroom2;
			Location location = player.getLocation();
			if(nukeroom1 != null && nukeroom2 != null)
			{
				double minX = Math.min(nukeroom1.getX(), nukeroom2.getX());
				double maxX = Math.max(nukeroom1.getX(), nukeroom2.getX());

				double minY = Math.min(nukeroom1.getY(), nukeroom2.getY());
				double maxY = Math.max(nukeroom1.getY(), nukeroom2.getY());

				double minZ = Math.min(nukeroom1.getZ(), nukeroom2.getZ());
				double maxZ = Math.max(nukeroom1.getZ(), nukeroom2.getZ());

				if((location.getX() >= minX && location.getX() <= maxX) && 
						(location.getY() >= minY && location.getY() <= maxY) && 
						(location.getZ() >= minZ && location.getZ() <= maxZ))
				{
					// the player is in the nukeroom
					if(map.playerdata.get(map.players.indexOf(event.getPlayer())).nukeroom == true)
					{
						
					}
					else
					{
						if(map.running == Map.State.RUNNING)
						{
							map.playerdata.get(map.players.indexOf(event.getPlayer())).nukeroom = true;
							Bukkit.getServer().broadcastMessage(plugin.publishcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("NukeroomMessage"), event.getPlayer().getName(), map.name));
							if(plugin.configs.dataconfig.getBoolean("InvincibleInNukeroom"))
							{
								player.setGameMode(GameMode.CREATIVE);
							}
						}
					}
				}
				else
				{
					if(map.playerdata.get(map.players.indexOf(event.getPlayer())).nukeroom == true && map.running == Map.State.RUNNING)
					{
						//the player should be in the nukeroom
						player.teleport(new Location(from.getWorld(), (from.getX() - to.getX()) + from.getX(), (from.getY() - to.getY()) + from.getY(), (from.getZ() - to.getZ()) + from.getZ(), from.getYaw(), from.getPitch()));
					}
				}
			}
		}
		
		updateSigns(event.getPlayer());
		trapPlayers(event.getPlayer());
	}

	private void trapPlayers(Player player) {
		for(Vector block : plugin.traps)
		{
			if(((int)player.getLocation().getX()) == ((int)block.getX()))
			{
				if(((int)player.getLocation().getZ()) == ((int)block.getZ()))
				{
					int y = player.getLocation().getBlockY();
					if(((int)block.getY()) == y)
					{
						Block block1 = player.getWorld().getBlockAt(new Location(player.getWorld(), player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()));
						player.getWorld().spawnFallingBlock(block1.getLocation(), 2, (byte) 0);
						block1.setType(Material.AIR);
						plugin.traps.remove(player.getLocation().toVector());
					}
				}
			}
		}
		
		
	}

	private void updateSigns(Player player) {
		for(int x = (int) player.getLocation().getX() - 20; x < player.getLocation().getX() + 20; x++)
		{
			for(int y = (int) player.getLocation().getY() - 20; y < player.getLocation().getY() + 20; y++)
			{
				for(int z = (int) player.getLocation().getZ() - 20; z < player.getLocation().getZ() + 20; z++)
				{
					Block block = player.getWorld().getBlockAt(x,y,z);
					if(block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
						Sign sign = (Sign) block.getState();
						if(sign.getLine(0).contains("PASSAGECRAFT"))
						{
							boolean found = false;
							Map map = new Map("");
							for(Map map1 : plugin.maps){
								if(sign.getLine(1).contains(map1.name))
								{
									map = map1;
									found = true;
								}
							}
							if(found)
							{
								sign.setLine(0, plugin.configs.dataconfig.getString("SignColourFirstLine") + "PASSAGECRAFT");
								sign.setLine(1, plugin.configs.dataconfig.getString("SignColourSecondLine") + map.name);
								sign.setLine(2, plugin.configs.dataconfig.getString("SignColourThirdLine") + map.players.size() + "/" + map.maxPlayers);
								sign.setLine(3, plugin.configs.dataconfig.getString("SignColourFourthLine") + map.running);
								sign.update();
							}
							else
							{
								sign.setLine(0, plugin.configs.dataconfig.getString("SignColourFirstLine") + "PASSAGECRAFT");
								sign.setLine(1, plugin.configs.dataconfig.getString("SignColourSecondLine") + sign.getLine(1).replace(plugin.configs.dataconfig.getString("SignColourSecondLine"), ""));
								sign.setLine(2, ChatColor.RED + "MAP NOT FOUND");
								sign.update();
							}
						}
						
					}
				}
			}
		}
	}
}