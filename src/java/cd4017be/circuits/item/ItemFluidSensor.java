package cd4017be.circuits.item;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import cd4017be.api.circuits.ISensor;
import cd4017be.circuits.gui.GuiFluidSensor;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.ITankContainer;

public class ItemFluidSensor extends DefaultItem implements ISensor, IGuiItem {

	public ItemFluidSensor(String id) {
		super(id);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (hand != EnumHand.MAIN_HAND) return new ActionResult<ItemStack>(EnumActionResult.PASS, item);
		BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean b) {
		if (item.hasTagCompound()) {
			String[] states = I18n.translateToLocal("gui.cd4017be.fluidSensor.tip").split(",");
			boolean inv = item.stackTagCompound.getBoolean("inv");
			Fluid fluid = this.getFluid(item);
			if (states.length >= 3) {
				String s;
				if (fluid == null) s = states[inv ? 1 : 0];
				else s = (inv ? states[2] : "") + fluid.getLocalizedName(new FluidStack(fluid, 0));
				list.add(s);
			}
		}
		super.addInformation(item, player, list, b);
	}

	@Override
	public float measure(ItemStack sensor, World world, BlockPos pos, EnumFacing side) {
		if (!world.isBlockLoaded(pos) || sensor.stackTagCompound == null) return 0F;
		IFluidHandler acc = FluidUtil.getFluidHandler(world, pos, side);
		if (acc == null) return 0F;
		Fluid filter = this.getFluid(sensor);
		boolean inv = sensor.stackTagCompound.getBoolean("inv");
		int n = 0;
		for (IFluidTankProperties prop : acc.getTankProperties()) {
			FluidStack fluid = prop.getContents();
			if (!inv && filter == null) n += prop.getCapacity() - (fluid != null ? fluid.amount : 0);
			else if (fluid != null && (inv ^ fluid.getFluid() == filter)) n += fluid.amount;
		}
		return n;
	}

	private Fluid getFluid(ItemStack inv) {
		return inv.stackTagCompound != null ? FluidRegistry.getFluid(inv.stackTagCompound.getString("type")) : null;
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) {
		return new TileContainer(new GuiData(), player);
	}

	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) {
		return new GuiFluidSensor(new TileContainer(new GuiData(), player));
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer data) {
		if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
		byte cmd = data.readByte();
		if (cmd == 0) item.stackTagCompound.setBoolean("inv", !item.stackTagCompound.getBoolean("inv"));
		else if (cmd == 1) item.stackTagCompound.setString("type", data.readStringFromBuffer(32));
	}

	class GuiData extends ItemGuiData implements ITankContainer {

		private InventoryPlayer player;
		public GuiData() {super(ItemFluidSensor.this);}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			cont.addTankSlot(new TankSlot(this, 0, 62, 16, (byte)0x11));
			cont.addPlayerInventory(8, 50, false, true);
			player = cont.player.inventory;
		}

		@Override
		public int getTanks() {return 1;}

		@Override
		public FluidStack getTank(int i) {
			ItemStack item = player.mainInventory[player.currentItem];
			Fluid fluid = item != null ? getFluid(item) : null;
			return fluid != null ? new FluidStack(fluid, 0) : null;
		}

		@Override
		public int getCapacity(int i) {return 0;}

		@Override
		public void setTank(int i, FluidStack fluid) {}

	}
}