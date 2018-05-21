package vazkii.playerschoice;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import vazkii.playerschoice.ModSettings.ModConfig;

public class GuiChooseMods extends GuiScreen {

	private GuiSlotModSettings settingsList;
	
	private int select = 0;
	private boolean didOk = false;
	
	@Override
	public void initGui() {
		super.initGui();
		
		select = 0;
		settingsList = new GuiSlotModSettings(this);
		
		buttonList.add(new GuiButton(0, 20, height - 35, 120, 20, I18n.format("gui.done")));
		buttonList.add(new GuiButton(1, 190, height - 35, 200, 20, I18n.format("playerschoice.website")));
		buttonList.add(new GuiButton(2, width / 2 - 100, height / 2 + 40, I18n.format("playerschoice.ok")));
		
		setupButtons();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		if(didOk) {
			settingsList.drawScreen(mouseX, mouseY, partialTicks);
			
			drawRect(0, 0, 160, 30, 0xDD000000);
			drawRect(0, height - 40, 160, height, 0xDD000000);
			drawCenteredString(mc.fontRenderer, I18n.format("playerschoice.mod_options"), 80, 10, 0xFFFFFFFF);
			
			if(select < PlayersChoice.instance.settings.mods.length) 
				renderModInfo(PlayersChoice.instance.settings.mods[select]);
		} else {
			GlStateManager.scale(2F, 2F, 2F);
			drawCenteredString(mc.fontRenderer, I18n.format("playerschoice.name"), width / 4, height / 4 - 40, 0x22FFFF);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			
			List<String> strings = mc.fontRenderer.listFormattedStringToWidth(I18n.format("playerschoice.info"), 200);
			for(int i = 0; i < strings.size(); i++)
				drawCenteredString(mc.fontRenderer, strings.get(i), width / 2, height / 2 - 40 + i * 10, 0xFFFFFF);
		}
		
		if(didChanges())
			drawCenteredString(mc.fontRenderer, I18n.format("playerschoice.needs_restart"), 80, height - 12, 0xFFFF00);
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void renderModInfo(ModConfig config) {
		GlStateManager.scale(2F, 2F, 2F);
		mc.fontRenderer.drawStringWithShadow(config.name, 90, 10, 0xFFFFFF);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		
		mc.fontRenderer.drawStringWithShadow(I18n.format("playerschoice.subtitle_" + config.enabled), 180, 40, 0x999999);
		
		String desc = config.desc.replaceAll("\\&", "\u00A7");
		List<String> strings = mc.fontRenderer.listFormattedStringToWidth(desc, Math.min(500, width - 200));
		for(int i = 0; i < strings.size(); i++)
			mc.fontRenderer.drawStringWithShadow(strings.get(i), 180, 60 + i * 10, 0xFFFFFF);
	}
	
	@Override
	public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
		
		super.handleMouseInput();
		
		if(didOk)
			settingsList.handleMouseInput(mouseX, mouseY);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		// NO-OP
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		
		switch(button.id) {
		case 0:
			PlayersChoice.instance.settings.commit();
			if(!didChanges())
				mc.displayGuiScreen(null);
			else FMLCommonHandler.instance().exitJava(0, false);
			
			break;
		case 1:
			try {
				Class<?> oclass = Class.forName("java.awt.Desktop");
				Object object = oclass.getMethod("getDesktop").invoke(null);
				oclass.getMethod("browse", URI.class).invoke(object, new URI(PlayersChoice.instance.settings.mods[select].website));
			}
			catch(Throwable e) {
				e.printStackTrace();
			}
			break;
		case 2:
			didOk = true;
			setupButtons();
		} 
	}
	
	private boolean didChanges() {
		for(ModConfig config : PlayersChoice.instance.settings.mods)
			if(config.enabled != config.base)
				return true;
		
		return false;
	}
	
	public void setSelected(int select) {
		this.select = select;
		setupButtons();
	}
	
	public int getSelect() {
		return select;
	}
	
	private void setupButtons() {
		ModConfig config = PlayersChoice.instance.settings.mods[select];
		buttonList.get(0).visible = didOk;
		buttonList.get(1).visible = didOk && config.website != null && !config.website.isEmpty();
		buttonList.get(2).visible = !didOk;
	}
	
}

