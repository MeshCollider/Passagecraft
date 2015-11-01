package net.meshcollision.PassageCraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Map {
	public List<Player> players = new ArrayList<Player>();
	public List<PlayerData> playerdata = new ArrayList<PlayerData>();
	public String name = "";
	public Vector lobby;
	public Vector nukeroom1;
	public Vector nukeroom2;
	public Vector protect1;
	public Vector protect2;
	public State running = State.READY;
	public boolean editing = false;
	public Vector[] spawnpoints = new Vector[4];
	public Kit kit;
	public int minPlayers = 0;
	public int maxPlayers = 0;
	
	public static enum State
	{
		RUNNING, READY, WAITING, UNAVAILABLE;
	}
	
	public void addPlayer(Player player)
	{
		players.add(player);
		playerdata.add(new PlayerData(player.getName()));
	}
	
	public void removePlayer(Player player)
	{
		int index = players.indexOf(player);
		players.remove(index);
		playerdata.remove(index);
	}

	public Map(String name)
	{
		this.name = name;
		lobby = new Vector();
		nukeroom1 = new Vector();
		nukeroom2 = new Vector();
		protect1 = new Vector();
		protect2 = new Vector();
		for(int i = 0; i < 4; i++)
		{
			spawnpoints[i] = new Vector();
			spawnpoints[i].setX(0.0);
			spawnpoints[i].setY(0.0);
			spawnpoints[i].setZ(0.0);
		}
	}
	
	public void minMax(int min, int max)
	{
		minPlayers = min;
		maxPlayers = max;
	}
	
	public Vector lobbyToVector()
	{
		return lobby;
	}
	
	public void reload(PassageCraft plugin)
	{
		long reload = plugin.configs.dataconfig.getInt("MapReloadTime");
		reload = reload * 20L; //one second = 20 ticks
		final Map map = this;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				map.players.clear();
				map.playerdata.clear();
				map.running = Map.State.READY;
			}
		}, reload);
	}
}
