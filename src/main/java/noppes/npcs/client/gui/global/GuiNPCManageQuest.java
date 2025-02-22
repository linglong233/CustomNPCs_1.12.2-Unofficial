package noppes.npcs.client.gui.global;

import java.util.HashMap;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

//Changed
public class GuiNPCManageQuest
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {
	
	public static GuiScreen Instance;
	private HashMap<String, QuestCategory> categoryData;
	char chr = Character.toChars(0x00A7)[0];
	private HashMap<String, Quest> questData;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private String selectedCategory = "";
	private String selectedQuest = "";

	public GuiNPCManageQuest(EntityNPCInterface npc) {
		super(npc);
		this.categoryData = new HashMap<String, QuestCategory>();
		this.questData = new HashMap<String, Quest>();
		GuiNPCManageQuest.Instance = this;
		Client.sendData(EnumPacketServer.QuestCategoryGet);
	}

	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 1) {
			this.setSubGui(new SubGuiEditText(1, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
		}
		if (button.id == 2) {
			if (!this.categoryData.containsKey(this.selectedCategory)) { return; }
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.categoryData.get(this.selectedCategory).title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
		if (button.id == 3) {
			if (!this.categoryData.containsKey(this.selectedCategory)) { return; }
			this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
		}
		if (button.id == 11) {
			this.setSubGui(new SubGuiEditText(11, AdditionalMethods.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
		}
		if (button.id == 12) {
			if (!this.questData.containsKey(this.selectedQuest)) { return; }
			GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.questData.get(this.selectedQuest).getTitle(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
			this.displayGuiScreen((GuiScreen) guiyesno);
		}
		if (button.id == 13) {
			if (!this.questData.containsKey(this.selectedQuest)) { return; }
			this.setSubGui(new GuiQuestEdit(this.questData.get(this.selectedQuest)));
		}
	}

	public void close() {
		super.close();
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) {
			return;
		}
		if (id == 2) {
			Client.sendData(EnumPacketServer.QuestCategoryRemove, this.categoryData.get(this.selectedCategory).id);
			this.selectedCategory = "";
			this.selectedQuest = "";
		}
		if (id == 12) {
			Client.sendData(EnumPacketServer.QuestRemove, this.questData.get(this.selectedQuest).id);
			this.selectedQuest = "";
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		HashMap<String, QuestCategory> categoryData = new HashMap<String, QuestCategory>();
		HashMap<String, Quest> questData = new HashMap<String, Quest>();
		for (QuestCategory category : QuestController.instance.categories.values()) {
			categoryData.put(category.title, category);
		}
		this.categoryData = categoryData;
		if (this.selectedCategory.isEmpty() && categoryData.size() > 0) {
			for (String key : categoryData.keySet()) {
				this.selectedCategory = key;
				break;
			}
		}
		if (!this.selectedCategory.isEmpty()) {
			if (this.categoryData.containsKey(this.selectedCategory)) {
				for (Quest quest : this.categoryData.get(this.selectedCategory).quests.values()) {
					boolean b = quest.isSetUp();
					questData.put(chr + "7ID:" + quest.id+"-\"" + chr + "r" + quest.getTitle() + chr + "7\""
							+ chr + (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText()) +
							chr + (b ? "2)" : "c)"), quest);
				}
			} else {
				this.selectedCategory = "";
				this.selectedQuest = "";
			}
		}
		this.questData = questData;
		if (this.selectedQuest.isEmpty() && questData.size() > 0) {
			for (String key : questData.keySet()) {
				this.selectedQuest = key;
				break;
			}
		}

		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 8, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "quest.quests", this.guiLeft + 175, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(3, "quest.quests", this.guiLeft + 356, this.guiTop + 8));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 356, this.guiTop + 18, 58, 20, "selectServer.edit",
				!this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 356, this.guiTop + 41, 58, 20, "gui.remove",
				!this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(11, this.guiLeft + 356, this.guiTop + 64, 58, 20, "gui.add",
				!this.selectedCategory.isEmpty()));
		this.addLabel(new GuiNpcLabel(2, "gui.categories", this.guiLeft + 356, this.guiTop + 110));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 356, this.guiTop + 120, 58, 20, "selectServer.edit",
				!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 356, this.guiTop + 143, 58, 20, "gui.remove",
				!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 356, this.guiTop + 166, 58, 20, "gui.add"));

		if (this.scrollCategories == null) {
			(this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, 200);
		}
		this.scrollCategories.setList(Lists.newArrayList(categoryData.keySet()));
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		if (!this.selectedCategory.isEmpty()) {
			this.scrollCategories.setSelected(this.selectedCategory);
		}
		this.addScroll(this.scrollCategories);

		if (this.scrollQuests == null) {
			(this.scrollQuests = new GuiCustomScroll(this, 1)).setSize(170, 200);
		}
		this.scrollQuests.setList(Lists.newArrayList(questData.keySet()));
		this.scrollQuests.guiLeft = this.guiLeft + 175;
		this.scrollQuests.guiTop = this.guiTop + 14;
		if (!this.selectedQuest.isEmpty()) {
			this.scrollQuests.setSelected(this.selectedQuest);
		}
		this.addScroll(this.scrollQuests);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if (guiCustomScroll.getSelected() == null) {
			return;
		}
		if (guiCustomScroll.id == 0) {
			if (this.selectedCategory.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedCategory = guiCustomScroll.getSelected();
			this.selectedQuest = "";
			this.scrollQuests.selected = -1;
		}
		if (guiCustomScroll.id == 1) {
			if (this.selectedQuest.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedQuest = guiCustomScroll.getSelected();
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (!this.selectedQuest.isEmpty() && scroll.id == 1) {
			this.setSubGui(new GuiQuestEdit(this.questData.get(this.selectedQuest)));
		}
		if (!this.selectedCategory.isEmpty() && scroll.id == 0) {
			this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		if (subgui.id == 1) { // new
			QuestCategory category = new QuestCategory();
			category.title = ((SubGuiEditText) subgui).text[0];
			this.selectedCategory = category.title;
			while (QuestController.instance.containsCategoryName(category)) {
				StringBuilder sb = new StringBuilder();
				QuestCategory questCategory = category;
				questCategory.title = sb.append(questCategory.title).append("_").toString();
				this.selectedCategory = questCategory.title;
			}
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui.id == 3) { // rename
			if (((SubGuiEditText) subgui).text[0].isEmpty()) { return; }
			QuestCategory category = this.categoryData.get(this.selectedCategory);
			category.title = ((SubGuiEditText) subgui).text[0];
			while (QuestController.instance.containsCategoryName(category)) { category.title += "_"; }
			this.selectedCategory = category.title;
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui.id == 11) {
			if (((SubGuiEditText) subgui).text[0].isEmpty()) {
				return;
			}
			Quest quest = new Quest(this.categoryData.get(this.selectedCategory));
			quest.setName(((SubGuiEditText) subgui).text[0]);
			while (QuestController.instance.containsQuestName(this.categoryData.get(this.selectedCategory), quest)) {
				quest.setName(quest.getName() + "_");
			}
			this.selectedQuest = "" + quest.getTitle();
			Client.sendData(EnumPacketServer.QuestSave, this.categoryData.get(this.selectedCategory).id,
					quest.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui instanceof GuiQuestEdit) {
			this.initGui();
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
		}
		super.keyTyped(c, i);
	}
}
