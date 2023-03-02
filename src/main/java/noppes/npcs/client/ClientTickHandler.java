package noppes.npcs.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiQuestLog;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.entity.EntityNPCInterface;

public class ClientTickHandler {
	
	private boolean otherContainer;
	private World prevWorld;

	public ClientTickHandler() {
		this.otherContainer = false;
	}

	@SubscribeEvent
	public void LivingUpdate(LivingUpdateEvent event) {
		if (!event.getEntity().world.isRemote || !(event.getEntity() instanceof EntityNPCInterface)) { return; }
		int dimID = Minecraft.getMinecraft().world.provider.getDimension();
		if (ClientProxy.notVisibleNPC.containsKey(dimID) && ClientProxy.notVisibleNPC.get(dimID).contains(event.getEntity().getUniqueID())) {
			Minecraft.getMinecraft().world.removeEntity(event.getEntity());
		}
	}
	
	@SubscribeEvent
	public void cnpcMouseInput(MouseEvent event) {
		if (event.getButton() == -1) {
			return;
		}
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
		if (Minecraft.getMinecraft().currentScreen==null) {
			boolean isCtrlPressed = ClientProxy.playerData.game.hasOrKeysPressed(157, 29);
			boolean isShiftPressed = ClientProxy.playerData.game.hasOrKeysPressed(54, 42);
			boolean isAltPressed = ClientProxy.playerData.game.hasOrKeysPressed(184, 56);
			boolean isMetaPressed = ClientProxy.playerData.game.hasOrKeysPressed(220, 219);
			if (event.isButtonstate()) { ClientProxy.playerData.game.mousePress.add(event.getButton()); }
			else { ClientProxy.playerData.game.mousePress.remove(event.getButton()); }
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, event.getButton(), event.isButtonstate(), isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.IsMoved, ClientProxy.playerData.game.hasOrKeysPressed(ClientProxy.frontButton.getKeyCode(), ClientProxy.backButton.getKeyCode(), ClientProxy.leftButton.getKeyCode(), ClientProxy.rightButton.getKeyCode()));
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_cnpcMouseInput");
	}

	@SubscribeEvent
	public void npcOnLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		if (event.getHand() != EnumHand.MAIN_HAND) {
			return;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.LeftClick, new Object[0]);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void npcOnClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_npcOnClientTick");
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && mc.player.openContainer instanceof ContainerPlayer) {
			if (this.otherContainer) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion, new Object[0]);
				this.otherContainer = false;
			}
		} else {
			this.otherContainer = true;
		}
		++CustomNpcs.ticks;
		++RenderNPCInterface.LastTextureTick;
		if (this.prevWorld != mc.world) {
			this.prevWorld = mc.world;
			MusicController.Instance.stopMusic();
		}
		if (CustomNpcs.ticks % 10 == 0) {
			MarcetController.getInstance().updateTime();
		}
		if (mc.currentScreen!=null) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.MousesPressed, -1);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, -1);
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_npcOnClientTick");
	}

	@SubscribeEvent
	public void npcOnKey(InputEvent.KeyInputEvent event) {
		CustomNpcs.debugData.startDebug("Client", "Players", "ClientTickHandler_npcOnKey");
		if (CustomNpcs.SceneButtonsEnabled) {
			if (ClientProxy.Scene1.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 1);
			}
			if (ClientProxy.Scene2.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 2);
			}
			if (ClientProxy.Scene3.isPressed()) {
				Client.sendData(EnumPacketServer.SceneStart, 3);
			}
			if (ClientProxy.SceneReset.isPressed()) {
				Client.sendData(EnumPacketServer.SceneReset, new Object[0]);
			}
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (ClientProxy.QuestLog.isPressed()) {
			if (mc.currentScreen == null) {
				NoppesUtil.openGUI((EntityPlayer) mc.player, new GuiQuestLog((EntityPlayer) mc.player));
			} else if (mc.currentScreen instanceof GuiQuestLog) {
				mc.setIngameFocus();
			}
		}
		if (mc.currentScreen==null) {
			boolean isCtrlPressed = ClientProxy.playerData.game.hasOrKeysPressed(157, 29);
			boolean isShiftPressed = ClientProxy.playerData.game.hasOrKeysPressed(54, 42);
			boolean isAltPressed = ClientProxy.playerData.game.hasOrKeysPressed(184, 56);
			boolean isMetaPressed = ClientProxy.playerData.game.hasOrKeysPressed(220, 219);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyPressed, Keyboard.getEventKey(), Keyboard.getEventKeyState(), isCtrlPressed, isShiftPressed, isAltPressed, isMetaPressed);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.IsMoved, ClientProxy.playerData.game.hasOrKeysPressed(ClientProxy.frontButton.getKeyCode(), ClientProxy.backButton.getKeyCode(), ClientProxy.leftButton.getKeyCode(), ClientProxy.rightButton.getKeyCode()));
		}
		CustomNpcs.debugData.endDebug("Client", "Players", "ClientTickHandler_npcOnKey");
	}

	@SubscribeEvent
	public void testingCode(LivingEvent.LivingJumpEvent event) {
		//EntityLivingBase entity = event.getEntityLiving();
		//if (!(entity instanceof EntityPlayerMP) || !CustomNpcs.VerboseDebug) { return; }
		//IPlayer player = (IPlayer) NpcAPI.Instance().getIEntity(((EntityPlayerMP) entity));
		//player.giveItem(CustomNpcs.MODID+":npcscripted", 0, 1);
		//System.out.println("CNPCs: "+player);
		
		//NpcAPI.Instance().executeCommand(player.getWorld(), "gamemode  "+player.getName()+" 1");
	}
	
}
