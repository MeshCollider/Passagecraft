package net.meshcollision.PassageCraft;

import java.util.ArrayList;
import java.util.List;

public class Kit {
	String name;
	List<Integer> items = new ArrayList<Integer>();
	List<Integer> quantities = new ArrayList<Integer>();
	
	public Kit(String name)
	{
		this.name = name;
	}
	
	public void addItem(int itemid, int quantity)
	{
		items.add(itemid);
		quantities.add(quantity);
	}
}
