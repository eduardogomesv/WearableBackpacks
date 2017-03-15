package net.mcft.copy.backpacks.client.config;

import java.util.Collections;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.config.EntryCategory;
import net.mcft.copy.backpacks.client.config.EntrySetting;

@SideOnly(Side.CLIENT)
public class BackpacksGuiConfig extends GuiConfig {
	
	public final String category;
	
	public BackpacksGuiConfig(GuiScreen parent)
		{ this(parent, Configuration.CATEGORY_GENERAL, WearableBackpacks.MOD_ID,
		       false, false, WearableBackpacks.MOD_NAME, ""); }
	
	public BackpacksGuiConfig(GuiScreen parentScreen, String category, String modID, 
	                          boolean allRequireWorldRestart, boolean allRequireMcRestart,
	                          String title, String titleLine2) {
		super(parentScreen, Collections.emptyList(), modID,
		      allRequireWorldRestart, allRequireMcRestart,
		      title, titleLine2);
		this.category = category;
		initGui();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		if (!(entryList instanceof Entries))
			entryList = new Entries(this);
	}
	
	/** Custom GuiConfigEntries class which generates entries directly from the
	 *  parent BackpacksGuiConfig category without going through IConfigElement.
	 *  Also provides patches to allow for custom height entry slots. */
	// TODO: Currently only works if there's only one custom sized entry at the end of the list.
	private static class Entries extends GuiConfigEntries {
		
		public Entries(BackpacksGuiConfig parent) {
			super(parent, parent.mc);
			
			listEntries = WearableBackpacks.CONFIG.getSettings(parent.category).stream()
				.map(setting -> EntrySetting.Create(owningScreen, this, setting))
				.collect(Collectors.toList());
			
			// If this is the general category, add category elements
			// leading to config sub-screens for all other categories.
			if (parent.category.equals(Configuration.CATEGORY_GENERAL))
				for (String cat : WearableBackpacks.CONFIG.getCategoryNames())
					if (!cat.equals(Configuration.CATEGORY_GENERAL))
						listEntries.add(new EntryCategory(owningScreen, this, cat));
			
			super.initGui();
		}
		
		private int getHeightForSlot(int slot) {
			IConfigEntry entry = getListEntry(slot);
			return ((entry instanceof EntrySetting) ?
				((EntrySetting<?>)entry).getSlotHeight() : getSlotHeight());
		}
		
		@Override
		public int getSlotIndexFromScreenCoords(int posX, int posY) {
			if ((posX < left + width / 2 - getListWidth() / 2) || (posX > getScrollBarX())) return -1;
			int y = posY - top - headerPadding + (int)amountScrolled - 4;
			for (int slot = 0; slot < getSize(); slot++) {
				int slotHeight = getHeightForSlot(slot);
				if (y < slotHeight) return slot;
				y -= slotHeight;
			}
			return -1;
		}
		
		@Override
		protected int getContentHeight() {
			int height = headerPadding;
			for (int slot = 0; slot < getSize(); slot++)
				height += getHeightForSlot(slot);
			return height;
		}
		
	}
	
}
