package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.ClientScriptData;

public class GuiScriptClient
extends GuiScriptInterface {
	
	private ClientScriptData script;

	public GuiScriptClient() {
		this.script = new ClientScriptData();
		this.handler = this.script;
		Client.sendData(EnumPacketServer.ScriptClientGet, new Object[0]);
		for (Class<?> cls : CustomNpcs.forgeClientEventNames.keySet()) {
			this.baseFuncNames.put(CustomNpcs.forgeClientEventNames.get(cls), cls);
		}
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptClientSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
	
}
