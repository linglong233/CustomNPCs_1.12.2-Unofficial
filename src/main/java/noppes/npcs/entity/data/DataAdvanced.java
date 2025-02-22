package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.constants.EnumNpcJob;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.ValueUtil;

public class DataAdvanced
implements INPCAdvanced {
	
	public boolean attackOtherFactions,  defendFaction, disablePitch, orderedLines;
	public JobInterface jobInterface;
	public RoleInterface roleInterface;
	private String angrySound, deathSound, hurtSound, idleSound, stepSound;
	private EntityNPCInterface npc;
	public Lines interactLines, npcInteractLines, worldLines, attackLines, killedLines, killLines;
	public FactionOptions factions;
	public EntityNPCInterface spawner;
	public DataScenes scenes;

	public DataAdvanced(EntityNPCInterface npc) {
		this.interactLines = new Lines();
		this.worldLines = new Lines();
		this.attackLines = new Lines();
		this.killedLines = new Lines();
		this.killLines = new Lines();
		this.npcInteractLines = new Lines();
		this.orderedLines = false;
		this.idleSound = "";
		this.angrySound = "";
		this.hurtSound = "minecraft:entity.player.hurt";
		this.deathSound = "minecraft:entity.player.hurt";
		this.stepSound = "";
		this.factions = new FactionOptions();
		this.jobInterface = new JobInterface(this.npc);
		this.roleInterface = new RoleInterface(this.npc);
		this.attackOtherFactions = false;
		this.defendFaction = false;
		this.disablePitch = false;
		this.npc = npc;
		this.scenes = new DataScenes(npc);
	}

	public Line getAttackLine() {
		return this.attackLines.getLine(!this.orderedLines);
	}

	public Line getInteractLine() {
		return this.interactLines.getLine(!this.orderedLines);
	}

	public Line getKilledLine() {
		return this.killedLines.getLine(!this.orderedLines);
	}

	public Line getKillLine() {
		return this.killLines.getLine(!this.orderedLines);
	}

	@Override
	public String getLine(int type, int slot) {
		Line line = this.getLines(type).lines.get(slot);
		if (line == null) {
			return null;
		}
		return line.getText();
	}

	@Override
	public int getLineCount(int type) {
		return this.getLines(type).lines.size();
	}

	private Lines getLines(int type) {
		if (type == 0) {
			return this.interactLines;
		}
		if (type == 1) {
			return this.attackLines;
		}
		if (type == 2) {
			return this.worldLines;
		}
		if (type == 3) {
			return this.killedLines;
		}
		if (type == 4) {
			return this.killLines;
		}
		if (type == 5) {
			return this.npcInteractLines;
		}
		return null;
	}

	public Line getNPCInteractLine() {
		return this.npcInteractLines.getLine(!this.orderedLines);
	}

	@Override
	public String getSound(int type) {
		String sound = null;
		switch(type) {
			case 0: { sound = this.idleSound; break; }
			case 1: { sound = this.angrySound; break; }
			case 2: { sound = this.hurtSound; break; }
			case 3: { sound = this.deathSound; break; }
			case 4: { sound = this.stepSound; break; }
			default: { break; }
		}
		if (sound != null && sound.isEmpty()) { sound = null; }
		return sound;
	}

	public Line getWorldLine() {
		return this.worldLines.getLine(!this.orderedLines);
	}

	public boolean hasWorldLines() {
		return !this.worldLines.isEmpty();
	}

	public void playSound(int type, float volume, float pitch) {
		String sound = this.getSound(type);
		if (sound == null) {
			return;
		}
		BlockPos pos = this.npc.getPosition();
		Server.sendRangedData(this.npc, 16, EnumPacketClient.PLAY_SOUND, sound, pos.getX(), pos.getY(), pos.getZ(),
				volume, pitch);
	}

	public void setJob(int i) {
		i %= EnumNpcJob.values().length;
		if (i < EnumNpcJob.values().length && !EnumNpcJob.values()[i].isClass(this.jobInterface)) {
			if (i==9) { i = 0; }
			else if (i>9) { i--; }
			EnumNpcJob.values()[i].setToNpc(this.npc);
		}
		if (!this.npc.world.isRemote) { this.jobInterface.reset(); }
	}
	
	public void setRole(int i) {
		if (8 <= i) { i -= 2; }
		i %= EnumNpcRole.values().length;
		if (i < EnumNpcRole.values().length && !EnumNpcRole.values()[i].isClass(this.roleInterface)) {
			EnumNpcRole.values()[i].setToNpc(this.npc);
		}
	}

	@Override
	public void setLine(int type, int slot, String text, String sound) {
		slot = ValueUtil.correctInt(slot, 0, 7);
		Lines lines = this.getLines(type);
		if (text == null || text.isEmpty()) {
			lines.lines.remove(slot);
		} else {
			Line line = lines.lines.get(slot);
			if (line == null) {
				lines.lines.put(slot, line = new Line());
			}
			line.setText(text);
			line.setSound(sound);
		}
	}

	@Override
	public void setSound(int type, String sound) {
		if (sound == null) { sound = ""; }
		switch(type) {
			case 0: this.idleSound = sound; break;
			case 1: this.angrySound = sound; break;
			case 2: this.hurtSound = sound; break;
			case 3: this.deathSound = sound; break;
			case 4: this.stepSound = sound; break;
		}
	}


	public void readToNBT(NBTTagCompound compound) {
		if (!compound.hasKey("NpcInteractLines", 10)) { return; }
		this.interactLines.readNBT(compound.getCompoundTag("NpcInteractLines"));
		this.worldLines.readNBT(compound.getCompoundTag("NpcLines"));
		this.attackLines.readNBT(compound.getCompoundTag("NpcAttackLines"));
		this.killedLines.readNBT(compound.getCompoundTag("NpcKilledLines"));
		this.killLines.readNBT(compound.getCompoundTag("NpcKillLines"));
		this.npcInteractLines.readNBT(compound.getCompoundTag("NpcInteractNPCLines"));
		this.orderedLines = compound.getBoolean("OrderedLines");
		this.idleSound = compound.getString("NpcIdleSound");
		this.angrySound = compound.getString("NpcAngrySound");
		this.hurtSound = compound.getString("NpcHurtSound");
		this.deathSound = compound.getString("NpcDeathSound");
		this.stepSound = compound.getString("NpcStepSound");
		this.npc.setFaction(compound.getInteger("FactionID"));
		this.npc.faction = this.npc.getFaction();
		this.attackOtherFactions = compound.getBoolean("AttackOtherFactions");
		this.defendFaction = compound.getBoolean("DefendFaction");
		this.disablePitch = compound.getBoolean("DisablePitch");
		this.factions.readFromNBT(compound.getCompoundTag("FactionPoints"));
		this.scenes.readFromNBT(compound.getCompoundTag("NpcScenes"));
		// New
		if (compound.hasKey("Role", 3) && compound.hasKey("NpcJob", 3)) { // OLD
			this.setRole(compound.getInteger("Role"));
			this.setJob(compound.getInteger("NpcJob"));
		}
		if (compound.hasKey("Role", 10) && compound.hasKey("Job", 10)) { // New
			this.setRole(compound.getCompoundTag("Role").getInteger("Type"));
			this.setJob(compound.getCompoundTag("Job").getInteger("Type"));
			this.roleInterface.readFromNBT(compound.getCompoundTag("Role"));
			this.jobInterface.readFromNBT(compound.getCompoundTag("Job"));
		}
		
		if (this.roleInterface instanceof RoleTrader && compound.hasKey("MarketID", 3)) {
			((RoleTrader) this.roleInterface).marcet = compound.getInteger("MarketID");
		}
		if (compound.hasKey("NPCDialogOptions", 11)) {
			this.npc.dialogs = compound.getIntArray("NPCDialogOptions"); // new
		} else if (compound.hasKey("NPCDialogOptions", 9)) {
			// Old
			this.npc.dialogs = new int[compound.getTagList("NPCDialogOptions", 10).tagCount()];
			for (int i = 0; i < compound.getTagList("NPCDialogOptions", 10).tagCount(); ++i) {
				NBTTagCompound nbttagcompound = compound.getTagList("NPCDialogOptions", 10).getCompoundTagAt(i);
				DialogOption option = new DialogOption();
				option.readNBT(nbttagcompound.getCompoundTag("NPCDialog"));
				this.npc.dialogs[i] = option.dialogId;
			}
		}
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("NpcLines", this.worldLines.writeToNBT());
		compound.setTag("NpcKilledLines", this.killedLines.writeToNBT());
		compound.setTag("NpcInteractLines", this.interactLines.writeToNBT());
		compound.setTag("NpcAttackLines", this.attackLines.writeToNBT());
		compound.setTag("NpcKillLines", this.killLines.writeToNBT());
		compound.setTag("NpcInteractNPCLines", this.npcInteractLines.writeToNBT());
		compound.setBoolean("OrderedLines", this.orderedLines);
		compound.setString("NpcIdleSound", this.idleSound);
		compound.setString("NpcAngrySound", this.angrySound);
		compound.setString("NpcHurtSound", this.hurtSound);
		compound.setString("NpcDeathSound", this.deathSound);
		compound.setString("NpcStepSound", this.stepSound);
		compound.setInteger("FactionID", this.npc.getFaction().id);
		compound.setBoolean("AttackOtherFactions", this.attackOtherFactions);
		compound.setBoolean("DefendFaction", this.defendFaction);
		compound.setBoolean("DisablePitch", this.disablePitch);
		compound.setTag("FactionPoints", this.factions.writeToNBT(new NBTTagCompound()));
		compound.setIntArray("NPCDialogOptions", this.npc.dialogs);
		compound.setTag("NpcScenes", this.scenes.writeToNBT(new NBTTagCompound()));

		NBTTagCompound roleNbt = new NBTTagCompound();
		NBTTagCompound jobNbt = new NBTTagCompound();
		this.jobInterface.writeToNBT(jobNbt);
		this.roleInterface.writeToNBT(roleNbt);
		compound.setTag("Role", roleNbt);
		compound.setTag("Job", jobNbt);
		return compound;
	}
}
