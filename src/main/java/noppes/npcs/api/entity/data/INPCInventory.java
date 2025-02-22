package noppes.npcs.api.entity.data;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.item.IItemStack;

public interface INPCInventory {
	
	ICustomDrop addDropItem(IItemStack item, double chance);

	IItemStack getArmor(int slot);

	ICustomDrop getDrop(int slot);

	ICustomDrop[] getDrops();

	int getExpMax();

	int getExpMin();

	int getExpRNG();

	IItemStack[] getItemsRNG(EntityLivingBase attacking);

	// void setDropItem(int p0, IItemStack p1, int p2); Changed

	// IItemStack getDropItem(int p0); Changed

	IItemStack[] getItemsRNGL(EntityLivingBase attacking);

	IItemStack getLeftHand();

	IItemStack getProjectile();

	IItemStack getRightHand();

	// IItemStack[] getItemsRNG(); Changed

	boolean getXPLootMode();

	boolean removeDrop(ICustomDrop drop);

	boolean removeDrop(int slot);

	void setArmor(int slot, IItemStack item);

	void setExp(int min, int max);

	void setLeftHand(IItemStack item);

	void setProjectile(IItemStack item);

	void setRightHand(IItemStack item);

	void setXPLootMode(boolean mode);
	
}
