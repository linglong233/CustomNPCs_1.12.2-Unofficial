package noppes.npcs.api.wrapper;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.util.ObfuscationHelper;

@SuppressWarnings("rawtypes")
public class VillagerWrapper<T extends EntityVillager>
extends EntityLivingWrapper<T>
implements IVillager {
	
	public VillagerWrapper(T entity) {
		super(entity);
	}

	public String getCareer() {
		return this.entity.getProfessionForge()
				.getCareer(ObfuscationHelper.getValue(EntityVillager.class, this.entity, 13))
				.getName();
	}

	@SuppressWarnings("deprecation")
	public int getProfession() {
		return this.entity.getProfession();
	}

	@Override
	public MerchantRecipeList getRecipes(IPlayer player) {
		return ((EntityVillager) this.entity).getRecipes(player.getMCEntity());
	}

	@Override
	public int getType() {
		return 9;
	}

	@Override
	public IInventory getVillagerInventory() {
		return ((EntityVillager) this.entity).getVillagerInventory();
	}

	@Override
	public boolean typeOf(int type) {
		return type == 9 || super.typeOf(type);
	}

}
