package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.quests.QuestObjective;

public class QuestData {
	
	public NBTTagCompound extraData = new NBTTagCompound();
	public boolean isCompleted = false;
	public Quest quest = null;

	public QuestData(Quest quest) {
		this.quest = quest;
		int pos = 0;
		for (QuestObjective task : quest.questInterface.tasks) {
			if (task.getEnumType() == EnumQuestTask.KILL || task.getEnumType() == EnumQuestTask.AREAKILL || task.getEnumType() == EnumQuestTask.MANUAL) {
				if (!this.extraData.hasKey("Targets", 9)) { this.extraData.setTag("Targets", new NBTTagList()); }
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("Slot", task.getTargetName());
				nbt.setInteger("Value", 0);
				nbt.setInteger("ObjectPos", pos);
				this.extraData.getTagList("Targets", 10).appendTag(nbt);
			} else if (task.getEnumType() == EnumQuestTask.CRAFT) {
				if (task.getItem().isEmpty()) { continue; }
				if (this.extraData.hasKey("Crafts", 9)) { this.extraData.setTag("Crafts", new NBTTagList()); }
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setTag("Item", task.getItemStack().writeToNBT(new NBTTagCompound()));
				nbt.setInteger("Value", 0);
				nbt.setInteger("ObjectPos", pos);
				this.extraData.getTagList("Crafts", 10).appendTag(nbt);
			} else if (task.getEnumType() == EnumQuestTask.LOCATION) {
				if (this.extraData.hasKey("Locations", 9)) { this.extraData.setTag("Locations", new NBTTagList()); }
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("Location", task.getTargetName());
				nbt.setBoolean("Found", false);
				nbt.setInteger("ObjectPos", pos);
				this.extraData.getTagList("Locations", 10).appendTag(nbt);
			}
			pos++;
		}
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		this.isCompleted = nbttagcompound.getBoolean("QuestCompleted");
		this.extraData = nbttagcompound.getCompoundTag("ExtraData");
	}

	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setBoolean("QuestCompleted", this.isCompleted);
		nbttagcompound.setTag("ExtraData", this.extraData);
	}

}
