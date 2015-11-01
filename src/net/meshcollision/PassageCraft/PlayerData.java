package net.meshcollision.PassageCraft;

import org.bukkit.util.Vector;

public class PlayerData {

	public int score = 0;
	public int killstreak = 0;
	public String name = "";
	public boolean nukeroom = false;
	Vector spawnpoint = new Vector();
	public boolean checkpoint = false;
	
	public PlayerData(String name)
	{
		this.name = name;
	}
}
