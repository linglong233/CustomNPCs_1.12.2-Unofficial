package noppes.npcs.api.wrapper;

import net.minecraft.entity.passive.EntityTameable;
import noppes.npcs.api.entity.IPixelmon;
import noppes.npcs.controllers.PixelmonHelper;

@SuppressWarnings("rawtypes")
public class PixelmonWrapper<T extends EntityTameable>
extends AnimalWrapper<T>
implements IPixelmon {
	
	public PixelmonWrapper(T entity) {
		super(entity);
	}

	@Override
	public Object getPokemonData() {
		return PixelmonHelper.getPokemonData(this.entity);
	}

	@Override
	public int getType() {
		return 8;
	}

	@Override
	public boolean typeOf(int type) {
		return type == 8 || super.typeOf(type);
	}
}
