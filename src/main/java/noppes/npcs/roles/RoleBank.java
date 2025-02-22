package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleBank
extends RoleInterface {
	
	public int bankId;

	public RoleBank(EntityNPCInterface npc) {
		super(npc);
		this.bankId = -1;
		this.type = EnumNpcRole.BANK;
	}

	public Bank getBank() {
		Bank bank = BankController.getInstance().banks.get(this.bankId);
		if (bank != null) {
			return bank;
		}
		return BankController.getInstance().banks.values().iterator().next();
	}

	@Override
	public void interact(EntityPlayer player) {
		BankData data = PlayerDataController.instance.getBankData(player, this.bankId).getBankOrDefault(this.bankId);
		data.openBankGui(player, this.npc, this.bankId, 0);
		this.npc.say(player, this.npc.advanced.getInteractLine());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.bankId = compound.getInteger("RoleBankID");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("RoleBankID", this.bankId);
		return compound;
	}
}
