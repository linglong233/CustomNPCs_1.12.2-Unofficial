package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiEditText
extends SubGuiInterface {
	
	public boolean cancelled;
	public String[] hovers = new String[1]; // New
	public String[] text = new String[1]; // New

	public SubGuiEditText(int id, String text) {
		this(new String[] { text }); // Changed
		this.id = id;
	}

	public SubGuiEditText(int id, String[] texts) { // New
		this(texts);
		this.id = id;
	}

	public SubGuiEditText(String[] texts) { // Changed
		this.cancelled = true;
		this.text = new String[texts.length > 5 ? 5 : texts.length];
		this.hovers = new String[texts.length > 5 ? 5 : texts.length];
		for (int i = 0; i < texts.length && i < 5; i++) {
			this.text[i] = AdditionalMethods.deleteColor(texts[i]);
			this.hovers[i] = "";
		}
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.xSize = 176;
		this.ySize = 49 + this.text.length * 22;
	}

	@Override
	public void buttonEvent(GuiButton button) {
		if (button.id == 0) {
			this.cancelled = false;
			// Changed
			for (int i = 0; i < this.text.length; i++) {
				this.text[i] = this.getTextField(i).getText();
			}
		}
		this.close();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < this.hovers.length && i < hovers.length; i++) {
			if (this.hovers[i] == null || this.hovers[i].isEmpty() || this.getTextField(i) == null) {
				continue;
			}
			if (this.isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + 16 + i * 22, 164, 16)) {
				this.setHoverText(this.hovers[i]);
				break;
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.mc.renderEngine != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			this.mc.renderEngine.bindTexture(this.background);
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			if (this.xSize > 256) {
				this.drawTexturedModalRect(0, this.ySize - 1, 0, 218, 250, this.ySize);
				this.drawTexturedModalRect(250, this.ySize - 1, 256 - (this.xSize - 250), 218, this.xSize - 250,
						this.ySize);
			} else {
				this.drawTexturedModalRect(0, this.ySize - 1, 0, 218, this.xSize, 4);
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < this.text.length && i < 5; i++) { // Changed
			this.addTextField(new GuiNpcTextField(i, this.parent, this.guiLeft + 4, this.guiTop + 14 + i * 22, 168, 20,
					this.text[i]));
		}
		this.addButton(
				new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 22 + this.text.length * 22, 80, 20, "gui.done"));
		this.addButton(
				new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 22 + this.text.length * 22, 80, 20, "gui.cancel"));
	}

	@Override
	public void save() {
	}

	// New
	public void setHoverTexts(String[] hovers) {
		for (int i = 0; i < this.hovers.length && i < hovers.length; i++) {
			this.hovers[i] = hovers[i];
		}
	}

}
