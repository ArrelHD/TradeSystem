package de.arrel.chest.system.listener;

import de.arrel.main.Main;
import de.arrel.main.SaveMain;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener{
	private Main main;
	private SaveMain save;
	
	private String path = "plugins/Money/chestShops.yml";
	private HashMap<Block, Block> waitingChestShops = new HashMap<Block, Block>();
	private ArrayList<ChestShop> createdChestShop = new ArrayList<ChestShop>();

	public SignListener(Main main) {
		this.main = main;
		this.save = main.getSave();
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if("LEGACY_WALL_SIGN".equalsIgnoreCase(e.getBlock().getType()+"")) {
			//IST ein SCHILD
			if("LEGACY_CHEST".equalsIgnoreCase(e.getBlockAgainst().getType()+"")) {
				//IST AN EINER CHEST GEPLACED
				Block sign = e.getBlockPlaced();
				Block chest = e.getBlockAgainst();
				waitingChestShops.put(sign, chest);
			}
			
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if("LEGACY_WALL_SIGN".equalsIgnoreCase(e.getBlock().getType()+"") || "LEGACY_CHEST".equalsIgnoreCase(e.getBlock().getType()+"")) {
			//Ist eine Chest oder Truhe gewesen
			for(int i = 0; i < createdChestShop.size(); i++) {
				if(createdChestShop.get(i).getSign().equals(e.getBlock())) {
					if(e.getPlayer().getName().equalsIgnoreCase(save.getStringFile(path, i + ".owner")) || e.getPlayer().isOp()) {
						//Das Schild wurde abgebaut -> löschen
						createdChestShop.remove(i);
						e.getPlayer().sendMessage("§cDu hast erfolgreich deinen Shop entfernt.");
						save();
						return;
					}else {
						e.setCancelled(true);
					}
					
				}else if(createdChestShop.get(i).getChest().equals(e.getBlock())) {
					if(e.getPlayer().getName().equalsIgnoreCase(save.getStringFile(path, i + ".owner")) || e.getPlayer().isOp()) {
						//Die Chest wurde abgebaut -> auch löschen
						createdChestShop.remove(i);
						e.getPlayer().sendMessage("§cDu hast erfolgreich deinen Shop entfernt.");
						save();
						return;
					}else {
						e.setCancelled(true);
					}
					
				}
			}
		}
	}
	
	@EventHandler
	public void onSign(SignChangeEvent e) {
		for(Block all : waitingChestShops.keySet()) {
			if(e.getBlock().equals(all)) {
				if(e.getLine(0).equalsIgnoreCase("[SHOP]") && e.getLine(3).equalsIgnoreCase("[SHOP]")) {
					if(!main.getAddAllowedShopOwners().canOwnShop(e.getPlayer().getName())) {
						e.getPlayer().sendMessage("§cDu darfst keinen Shop haben.");
						return;
					}
					
					int ammount = 0;
					int price = 0;
					try {
						ammount = Integer.parseInt(e.getLine(1));
						price = Integer.parseInt(e.getLine(2));
					}catch (NumberFormatException err) {
						e.getPlayer().sendMessage("§cBitte trage in die erste Zeile die Anzahl und in die Zweite einen Preis ein.");
						return;
					}
					
					if(price < 0) {
						e.getPlayer().sendMessage("§cUngültiger Preis!.");
						return;
					}
					
					if(ammount > 64) {
						e.getPlayer().sendMessage("§cBitte beachte, dass du nie mehr verkaufen kannst, als ein Stack zulässt.");
						return;
					}
					System.out.println("Changes");
					e.setLine(0, "§a-------------");
					e.setLine(1, "§6" + ammount + "x " + "");
					e.setLine(2, "§aPreis: §6" + price);
					e.setLine(3, "§a-------------");
					
					createdChestShop.add(new ChestShop(all, waitingChestShops.get(all), ammount, price, e.getPlayer().getName()));
					e.getPlayer().sendMessage("§aDu hast einen neuen Shop erstellt");
					save();
				}
				
				
				return;
			}
		}
		
	}
	
	
	public void save() {
		//save.clearFile(path);
		for(int i = 0; i < createdChestShop.size(); i++) {
			save.saveFile(path, i + ".created", true);
			save.saveFile(path, i + ".signloc.world", createdChestShop.get(i).getSign().getLocation().getWorld().getName());
			save.saveFile(path, i + ".signloc.x", createdChestShop.get(i).getSign().getLocation().getBlockX());
			save.saveFile(path, i + ".signloc.y", createdChestShop.get(i).getSign().getLocation().getBlockY());
			save.saveFile(path, i + ".signloc.z", createdChestShop.get(i).getSign().getLocation().getBlockZ());
			
			save.saveFile(path, i + ".chestloc.world", createdChestShop.get(i).getChest().getLocation().getWorld().getName());
			save.saveFile(path, i + ".chestloc.x", createdChestShop.get(i).getChest().getLocation().getBlockX());
			save.saveFile(path, i + ".chestloc.y", createdChestShop.get(i).getChest().getLocation().getBlockY());
			save.saveFile(path, i + ".chestloc.z", createdChestShop.get(i).getChest().getLocation().getBlockZ());
			
			save.saveFile(path, i + ".ammount", createdChestShop.get(i).getAmmount());
			save.saveFile(path, i + ".price", createdChestShop.get(i).getPrice());
			save.saveFile(path, i + ".owner", createdChestShop.get(i).getOwner());
		}
	}
	
	public void get() {
		createdChestShop = new ArrayList<ChestShop>();
		
		int i = 0;

		while(save.getBooleanFile(path, i + ".created")) {
			if(save.getStringFile(path, i + ".signloc.world") == null) {
				return;
			}
			Location signLoc = new Location(Bukkit.getWorld(save.getStringFile(path, i + ".signloc.world")), Integer.parseInt(save.getStringFile(path, i + ".signloc.x")), Integer.parseInt(save.getStringFile(path, i + ".signloc.y")), Integer.parseInt(save.getStringFile(path, i + ".signloc.z")));
			Location chestLoc = new Location(Bukkit.getWorld(save.getStringFile(path, i + ".chestloc.world")), Integer.parseInt(save.getStringFile(path, i + ".chestloc.x")), Integer.parseInt(save.getStringFile(path, i + ".chestloc.y")), Integer.parseInt(save.getStringFile(path, i + ".chestloc.z")));
			Block sign = signLoc.getBlock();
			Block chest = chestLoc.getBlock();
			
			createdChestShop.add(new ChestShop(sign, chest, Integer.parseInt(save.getStringFile(path, i + ".ammount")), Integer.parseInt(save.getStringFile(path, i + ".price")), save.getStringFile(path, i + ".owner")));
			
			i++;
		}
	}
	
	public ArrayList<ChestShop> getCreatedChestShop() {
		return createdChestShop;
	}

	public String getPath() {
		return path;
	}
	
}
