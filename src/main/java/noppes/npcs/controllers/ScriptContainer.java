package noppes.npcs.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.PotionType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.ParticleType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.constants.SideType;
import noppes.npcs.api.constants.TacticalType;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.api.wrapper.DataObject;
import noppes.npcs.constants.EnumScriptType;

public class ScriptContainer {

	public static ScriptContainer Current;
	private static HashMap<String, Object> Data = Maps.<String, Object>newHashMap();
	private static Method luaCall;
	private static Method luaCoerce;
	public TreeMap<Long, String> console;
	public ScriptEngine engine;
	public boolean errored;
	public String fullscript;
	private IScriptHandler handler;
	private boolean init;
	public long lastCreated;
	public String script;
	public List<String> scripts;
	private HashSet<String> unknownFunctions;
	
	static {
		FillMap(AnimationType.class);
		FillMap(EntityType.class);
		FillMap(RoleType.class);
		FillMap(JobType.class);
		FillMap(SideType.class);
		FillMap(TacticalType.class);
		FillMap(PotionType.class);
		FillMap(ParticleType.class);
		FillMap(ScriptController.Instance.constants); // New
		ScriptContainer.Data.put("API", NpcAPI.Instance()); // New
		ScriptContainer.Data.put("PosZero", new BlockPosWrapper(BlockPos.ORIGIN));
	}

	private static void FillMap(Class<?> c) {
		try {
			ScriptContainer.Data.put(c.getSimpleName(), c.newInstance());
		} catch (Exception ex) {
		}
		Field[] declaredFields2;
		declaredFields2 = c.getDeclaredFields();
		for (Field field : declaredFields2) {
			try {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == Integer.TYPE) {
					ScriptContainer.Data.put(c.getSimpleName() + "_" + field.getName(), field.getInt(null));
				}
			} catch (Exception ex2) {
			}
		}
	}

	private static void FillMap(NBTTagCompound c) { // New
		if (!c.hasKey("Constants", 10)) { return; }
		for (String key : c.getCompoundTag("Constants").getKeySet()) {
			NBTBase tag = c.getCompoundTag("Constants").getTag(key);
			Object value = tag;
			switch (tag.getId()) {
			case 1: {
				value = ((NBTTagByte) tag).getByte();
				break;
			}
			case 2: {
				value = ((NBTTagShort) tag).getShort();
				break;
			}
			case 3: {
				value = ((NBTTagInt) tag).getInt();
				break;
			}
			case 4: {
				value = ((NBTTagLong) tag).getLong();
				break;
			}
			case 5: {
				value = ((NBTTagFloat) tag).getFloat();
				break;
			}
			case 6: {
				value = ((NBTTagDouble) tag).getDouble();
				break;
			}
			case 7: {
				value = ((NBTTagByteArray) tag).getByteArray();
				break;
			}
			case 8: {
				value = ((NBTTagString) tag).getString();
				break;
			}
			case 11: {
				value = ((NBTTagIntArray) tag).getIntArray();
				break;
			}
			case 12: {
				Field data = null;
				try {
					data = tag.getClass().getField("data");
				} catch (NoSuchFieldException | SecurityException e1) {
				}

				if (data != null) {
					data.setAccessible(true);
					try {
						value = data.get(tag);
					} catch (IllegalArgumentException | IllegalAccessException e) {
					}
				} else {
					value = new long[0];
				}
				break;
			}
			}
			ScriptContainer.Data.put(key, value);
		}
	}

	public ScriptContainer(IScriptHandler handler) {
		this.fullscript = "";
		this.script = "";
		this.console = new TreeMap<Long, String>();
		this.errored = false;
		this.scripts = new ArrayList<String>();
		this.unknownFunctions = new HashSet<String>();
		this.lastCreated = 0L;
		this.engine = null;
		this.handler = null;
		this.init = false;
		this.handler = handler;
	}

	public void appandConsole(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		long time = System.currentTimeMillis();
		if (this.console.containsKey(time)) {
			message = this.console.get(time) + "\n" + message;
		}
		this.console.put(time, message);
		while (this.console.size() > 40) {
			this.console.remove(this.console.firstKey());
		}
	}

	public String getFullCode() {
		if (!this.init) {
			this.fullscript = this.script;
			if (!this.fullscript.isEmpty()) {
				this.fullscript += "\n";
			}
			for (String loc : this.scripts) {
				String code = ScriptController.Instance.scripts.get(loc);
				if (code != null && !code.isEmpty()) {
					this.fullscript = this.fullscript + code + "\n";
				}
			}
			this.unknownFunctions = new HashSet<String>();
		}
		return this.fullscript;
	}

	public boolean hasCode() {
		return !this.getFullCode().isEmpty();
	}

	public boolean isValid() {
		return this.init && !this.errored;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.script = compound.getString("Script");
		this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
		this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
		this.lastCreated = 0L;
	}

	public void run(EnumScriptType type, Event event) {
		Object key = event instanceof BlockEvent ? "Block"
				: event instanceof PlayerEvent ? "Player"
						: event instanceof ItemEvent ? "Item"
								: event instanceof NpcEvent ? "Npc" : null;
		CustomNpcs.debugData.startDebug("Server", "In "+key, "ScriptContainer_run_" + type.function);
		this.run(type.function, event);
		CustomNpcs.debugData.endDebug("Server", key, "ScriptContainer_run_" + type.function);
	}

	public void run(String type, Object event) {
		if (this.engine==null) { this.setEngine(this.handler.getLanguage()); }
		if (this.errored || !this.hasCode() || this.unknownFunctions.contains(type) || !CustomNpcs.EnableScripting) {
			return;
		}
		if (this.engine == null) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastCreated) {
			this.lastCreated = ScriptController.Instance.lastLoaded;
			this.init = false;
		}
		synchronized ("lock") {
			ScriptContainer.Current = this;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			this.engine.getContext().setWriter(pw);
			this.engine.getContext().setErrorWriter(pw);
			try {
				if (!this.init) {
					this.engine.eval(this.getFullCode());
					this.init = true;
				}
				if (this.engine.getFactory().getLanguageName().equals("lua")) {
					Object ob = this.engine.get(type);
					if (ob != null) {
						if (ScriptContainer.luaCoerce == null) {
							ScriptContainer.luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua").getMethod("coerce", Object.class);
							ScriptContainer.luaCall = ob.getClass().getMethod("call", Class.forName("org.luaj.vm2.LuaValue"));
						}
						ScriptContainer.luaCall.invoke(ob, ScriptContainer.luaCoerce.invoke(null, event));
					} else {
						this.unknownFunctions.add(type);
					}
				} else {
					((Invocable) this.engine).invokeFunction(type, event);
				}
			} catch (NoSuchMethodException e2) {
				this.unknownFunctions.add(type);
			} catch (Throwable e) {
				this.errored = true;
				e.printStackTrace(pw);
				NoppesUtilServer.NotifyOPs(this.handler.noticeString() + " script errored", new Object[0]);
			} finally {
				this.appandConsole(sw.getBuffer().toString().trim());
				pw.close();
				ScriptContainer.Current = null;
			}
		}
	}

	/*
	 * Old private class Dump implements Function<Object, String> {
	 * @Override 
	 * public String apply(Object o) { if (o == null) { return "null"; }
	 * StringBuilder builder = new StringBuilder();
	 * builder.append(o + ":" + NoppesStringUtils.newLine());
	 * for (Field field : o.getClass().getFields()) {
	 * try { builder.append(field.getName() + " - " +
	 * field.getType().getSimpleName() + ", "); } catch (IllegalArgumentException
	 * ex) {} } for (Method method : o.getClass().getMethods()) { try { String s =
	 * method.getName() + "("; for (Class<?> c : method.getParameterTypes()) { s = s
	 * + c.getSimpleName() + ", "; } if (s.endsWith(", ")) { s = s.substring(0,
	 * s.length() - 2); } builder.append(s + "), "); } catch
	 * (IllegalArgumentException ex2) {} } return builder.toString(); } }
	 */

	public void setEngine(String scriptLanguage) {
		this.engine = ScriptController.Instance.getEngineByName(scriptLanguage);
		if (this.engine == null) {
			this.errored = true;
			return;
		}
		// New
		if (!ScriptContainer.Data.containsKey("dump")) {
			for (int i=0; i<ScriptController.Instance.constants.getTagList("Functions", 8).tagCount(); i++) {
				String body = ScriptController.Instance.constants.getTagList("Functions", 8).getStringTagAt(i);
				if (body.toLowerCase().indexOf("function ")!=0) { continue; }
				try {
					String key = body.substring(body.indexOf(" ")+1, body.indexOf("("));
					ScriptContainer.Data.put(key, this.engine.eval(body));
					ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).removeTag("EvalIsError");
				}
				catch (Exception e) {
					ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).setBoolean("EvalIsError", true);
				}
			}
			for (String key : ScriptController.Instance.constants.getCompoundTag("Constants").getKeySet()) {
				NBTBase tag = ScriptController.Instance.constants.getCompoundTag("Constants").getTag(key);
				if (tag.getId()==8) {
					try {
						ScriptContainer.Data.put(key, this.engine.eval(((NBTTagString) tag).getString()));
					}
					catch (Exception e) { }
				}
			}
			ScriptContainer.Data.put("dump", new Dump());
			ScriptContainer.Data.put("log", new Log());
		}
		for (Map.Entry<String, Object> entry : ScriptContainer.Data.entrySet()) {
			this.engine.put(entry.getKey(), entry.getValue());
		}
		this.init = false;
	}

	public boolean varIsConstant(String name) {
		return ScriptContainer.Data.containsKey(name);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("Script", this.script);
		compound.setTag("Console", NBTTags.NBTLongStringMap(this.console));
		compound.setTag("ScriptList", NBTTags.nbtStringList(this.scripts));
		return compound;
	}
	
	// New
	public class Dump implements Function<Object, IDataObject> {

		@Override
		public IDataObject apply(Object o) {
			return new DataObject(o);
		}

	}

	public class Log implements Function<Object, Void> {
		@Override
		public Void apply(Object o) {
			ScriptContainer.this.appandConsole(o + "");
			LogWriter.info(o + "");
			return null;
		}
	}

	public static void reloadConstants() {
		ScriptContainer.Data.remove("dump");
	}

}
