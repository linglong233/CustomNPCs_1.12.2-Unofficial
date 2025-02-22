package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumNpcJob;
import noppes.npcs.entity.EntityNPCInterface;

public class JobGuard
extends JobInterface {
	
	public List<String> targets;

	public JobGuard(EntityNPCInterface npc) {
		super(npc);
		this.targets = new ArrayList<String>();
		this.type = EnumNpcJob.GUARD;
	}

	public boolean isEntityApplicable(Entity entity) {
		return !(entity instanceof EntityPlayer) && !(entity instanceof EntityNPCInterface)
				&& this.targets.contains("entity." + EntityList.getEntityString(entity) + ".name");
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.targets = NBTTags.getStringList(compound.getTagList("GuardTargets", 10));
		if (compound.getBoolean("GuardAttackAnimals")) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = (Class<? extends Entity>) ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityAnimal.class.isAssignableFrom(cl) && !this.targets.contains(name)) {
					this.targets.add(name);
				}
			}
		}
		if (compound.getBoolean("GuardAttackMobs")) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = (Class<? extends Entity>) ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityMob.class.isAssignableFrom(cl) && !EntityCreeper.class.isAssignableFrom(cl)
						&& !this.targets.contains(name)) {
					this.targets.add(name);
				}
			}
		}
		if (compound.getBoolean("GuardAttackCreepers")) {
			for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
				Class<? extends Entity> cl = (Class<? extends Entity>) ent.getEntityClass();
				String name = "entity." + ent.getName() + ".name";
				if (EntityCreeper.class.isAssignableFrom(cl) && !this.targets.contains(name)) {
					this.targets.add(name);
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("GuardTargets", NBTTags.nbtStringList(this.targets));
		return compound;
	}
}
