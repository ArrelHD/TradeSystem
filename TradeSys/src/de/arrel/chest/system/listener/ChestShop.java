package de.arrel.chest.system.listener;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ChestShop {
	
	private Block sign;
	private Block chest;
	private int ammount;
	private int price;
	private String owner;
	
	public ChestShop(Block sign, Block chest, int ammount, int price, String owner) {
		this.sign = sign;
		this.chest = chest;
		this.ammount = ammount;
		this.price = price;
		this.owner = owner;
	}

	public Block getSign() {
		return sign;
	}

	public Block getChest() {
		return chest;
	}

	public int getAmmount() {
		return ammount;
	}

	public int getPrice() {
		return price;
	}

	public String getOwner() {
		return owner;
	}

	
}
