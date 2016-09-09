package cd4017be.circuits.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiItemSensor extends GuiMachine {

	private final InventoryPlayer inv;

	public GuiItemSensor(TileContainer cont) {
		super(cont);
		this.MAIN_TEX = new ResourceLocation("circuits", "textures/gui/itemSensor.png");
		this.inv = cont.player.inventory;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new Button(0, 79, 15, 18, 18, 0).texture(176, 0).setTooltip("itemSensor.neg"));
		guiComps.add(new Button(1, 97, 15, 18, 9, 0).texture(194, 0).setTooltip("itemSensor.meta"));
		guiComps.add(new Button(2, 97, 24, 18, 9, 0).texture(194, 18).setTooltip("itemSensor.nbt"));
		guiComps.add(new Button(3, 115, 15, 18, 18, 0).texture(212, 0).setTooltip("itemSensor.ore"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.itemSensor.name"), guiLeft + xSize / 2, guiTop + 4, 0xff404040);
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		byte mode = item != null && item.stackTagCompound != null ? item.stackTagCompound.getByte("mode") : 0;
		return (int)mode >> id & 1;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.getPos());
		ItemStack item = inv.mainInventory[inv.currentItem];
		byte mode = item != null && item.stackTagCompound != null ? item.stackTagCompound.getByte("mode") : 0;
		dos.writeByte(mode ^ 1 << id);
		BlockGuiHandler.sendPacketToServer(dos);
	}

}