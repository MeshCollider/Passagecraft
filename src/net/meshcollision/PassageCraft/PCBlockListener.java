package net.meshcollision.PassageCraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.material.Sign;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

public class PCBlockListener implements Listener{
	PassageCraft plugin;
	boolean hitswitch = false;
	Vector hit1 = new Vector();
	Vector hit2 = new Vector();

	public PCBlockListener(PassageCraft plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		wandEvent(event);
		signEvent(event);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		if(block == null) return;
		for(Map map : plugin.maps)
		{
			Vector protect1 = map.protect1;
			Vector protect2 = map.protect2;
			if(protect1 != null && protect2 != null)
			{
				double minX = Math.min(protect1.getX(), protect2.getX());
				double maxX = Math.max(protect1.getX(), protect2.getX());

				double minY = Math.min(protect1.getY(), protect2.getY());
				double maxY = Math.max(protect1.getY(), protect2.getY());

				double minZ = Math.min(protect1.getZ(), protect2.getZ());
				double maxZ = Math.max(protect1.getZ(), protect2.getZ());

				if((block.getX() >= minX && block.getX() <= maxX) && 
						(block.getY() >= minY && block.getY() <= maxY) && 
						(block.getZ() >= minZ && block.getZ() <= maxZ))
				{
					if(event.getPlayer().hasPermission("passagecraft.breakprotected"))
					{
						event.getPlayer().sendMessage(plugin.infocolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("ProtectedBlockBrokenMessage"), event.getPlayer().getName(), map.name));
					}
					else
					{
						event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("ProtectedBlockMessage"), event.getPlayer().getName(), map.name));
						event.setCancelled(true);
					}
				}
			}
		}	
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		if(block == null) return;
		for(Map map : plugin.maps)
		{
			Vector protect1 = map.protect1;
			Vector protect2 = map.protect2;
			if(protect1 != null && protect2 != null)
			{
				double minX = Math.min(protect1.getX(), protect2.getX());
				double maxX = Math.max(protect1.getX(), protect2.getX());

				double minY = Math.min(protect1.getY(), protect2.getY());
				double maxY = Math.max(protect1.getY(), protect2.getY());

				double minZ = Math.min(protect1.getZ(), protect2.getZ());
				double maxZ = Math.max(protect1.getZ(), protect2.getZ());

				if((block.getX() >= minX && block.getX() <= maxX) && 
						(block.getY() >= minY && block.getY() <= maxY) && 
						(block.getZ() >= minZ && block.getZ() <= maxZ))
				{
					if(event.getPlayer().hasPermission("passagecraft.placeprotected"))
					{
						event.getPlayer().sendMessage(plugin.infocolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("ProtectedBlockPlacedMessage"), event.getPlayer().getName(), map.name));
					}
					else
					{
						event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("ProtectedAreaMessage"), event.getPlayer().getName(), map.name));
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
	}

	@EventHandler
	public void SignClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (e.getClickedBlock().getState() instanceof Sign) 
			{
				Sign sign = (Sign) e.getClickedBlock().getState();
			}
		}
	}
	
	public void wandEvent(PlayerInteractEvent event)
	{
		if(event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK )
		{
			Block block = event.getClickedBlock();
			//if the item used is a wand
			if(event.getItem() != null && event.getPlayer().getItemInHand().getItemMeta().hasDisplayName())
			{
				if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals("Nukeroom editor"))
				{
					if(hitswitch == false)
					{
						hit1 = block.getLocation().toVector();
						event.getPlayer().sendMessage(plugin.createcolor + "Location 1 set");
					}
					else
					{
						hit2 = block.getLocation().toVector();
						plugin.setRegion(hit1, hit2, true, false);
						event.getPlayer().sendMessage(plugin.createcolor + "Nukeroom location 2 set");
					}
					hitswitch = !hitswitch;
				}
				else if(event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equals("Protection editor"))
				{
					if(hitswitch == false)
					{
						hit1 = block.getLocation().toVector();
						event.getPlayer().sendMessage(plugin.createcolor + "Location 1 set");
					}
					else
					{
						hit2 = block.getLocation().toVector();
						event.getPlayer().sendMessage(plugin.createcolor + "Protection location 2 set");
						plugin.setRegion(hit1, hit2, false, true);
					}
					hitswitch = !hitswitch;
				}
			}
		}
	}
	
	public void signEvent(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			if(block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign) event.getClickedBlock().getState();
				if(sign.getLine(0).contains("[CHECKPOINT]"))
				{
					for(Map map : plugin.maps)
					{
						if(map.players.contains(event.getPlayer()))
						{
							map.playerdata.get(map.players.indexOf(event.getPlayer())).spawnpoint = event.getPlayer().getLocation().toVector();
							map.playerdata.get(map.players.indexOf(event.getPlayer())).checkpoint = true;
							event.getPlayer().sendMessage(plugin.infocolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("CheckpointMessage"), event.getPlayer().getName(), map.name));
						}
					}
				}
				else if(sign.getLine(0).contains("PASSAGECRAFT"))
				{
					boolean found = false;
					Map map = null;
					for(Map map2 : plugin.maps)
					{
						if(sign.getLine(1).contains(map2.name))
						{
							found = true;
							map = map2;
							for(Map map1 : plugin.maps){
								if(map1.players.contains(event.getPlayer()))
								{
									event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("AlreadyJoinedMessage"), event.getPlayer().getName(), map1.name));
									return;
								}
							}
						}
					}
					if(!found)
					{
						event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("MapNotFoundMessage"), event.getPlayer().getName(), sign.getLine(1)));
					}
					else
					{
						if(map.players.size() <= map.maxPlayers && map.running != Map.State.RUNNING && map.running != Map.State.UNAVAILABLE && event.getPlayer().hasPermission("passagecraft.join"))
						{
							event.getPlayer().sendMessage(plugin.publishcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("JoinMessage"), event.getPlayer().getName(), map.name));

							Location location = new Location(event.getPlayer().getWorld(), map.lobby.getX(), map.lobby.getY(), map.lobby.getZ());
							event.getPlayer().teleport(location);
							map.addPlayer(event.getPlayer());
							Bukkit.getServer().broadcastMessage(plugin.replaceStrings(plugin.configs.dataconfig.getString("PublicJoinMessage"), event.getPlayer().getName(), map.name));
							event.getPlayer().setHealth(event.getPlayer().getMaxHealth());
							event.getPlayer().getInventory().clear();
							if(map.kit != null && !(map.kit.items.isEmpty()))
							{
								for(int itemid : map.kit.items)
								{
									event.getPlayer().getInventory().addItem(new ItemStack(itemid, map.kit.quantities.get(map.kit.items.indexOf(itemid))));
								}
							}
							if(map.players.size() >= map.minPlayers && map.running == Map.State.READY)
							{
								map.running = Map.State.WAITING;
								plugin.startmap(map);
							}
						}
						else{
							if(map.running != Map.State.RUNNING)
							{
								if(map.running == Map.State.UNAVAILABLE)
								{
									event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("MapUnavailableMessage"), event.getPlayer().getName(), map.name));
								}
								else
								{
									event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("MapFullMessage"), event.getPlayer().getName(), map.name));
								}
							}
							else
							{
								event.getPlayer().sendMessage(plugin.errorcolor + plugin.replaceStrings(plugin.configs.dataconfig.getString("MapInProgressMessage"), event.getPlayer().getName(), map.name));
							}
						}
					}
				}
			}
		}
	}
}
