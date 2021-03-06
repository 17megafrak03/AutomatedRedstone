package multiblock;

import java.util.ArrayList;
import java.util.HashMap;

import cd4017be.lib.TickRegistry;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.templates.SharedNetwork;

/**
 * 
 * @author CD4017BE
 */
public class SharedInteger extends SharedNetwork<IntegerComp, SharedInteger> implements IUpdatable {

	private static final short AllIn = 0x5555, AllOut = (short)0xaaaa;
	public ArrayList<IntegerComp> inputs = new ArrayList<IntegerComp>();
	public ArrayList<IntegerComp> outputs = new ArrayList<IntegerComp>();
	public int outputState = 0;
	public boolean updateState;

	protected SharedInteger(HashMap<Long, IntegerComp> comps) {
		super(comps);
	}

	public SharedInteger(IntegerComp comp) {
		super(comp);
		if ((comp.rsIO & AllIn) != 0) inputs.add(comp);
		if ((comp.rsIO & AllOut) != 0) outputs.add(comp);
		outputState = comp.inputState;
	}

	@Override
	public void onMerged(SharedInteger network) {
		super.onMerged(network);
		outputState &= network.outputState;
		markStateDirty();
		inputs.addAll(network.inputs);
		outputs.addAll(network.outputs);
	}

	@Override
	public void remove(IntegerComp comp) {
		super.remove(comp);
		if ((comp.rsIO & AllIn) != 0) {
			inputs.remove(comp);
			markStateDirty();
		}
		if ((comp.rsIO & AllOut) != 0) 
			outputs.remove(comp);
	}

	@Override
	public SharedInteger onSplit(HashMap<Long, IntegerComp> comps) {
		SharedInteger si = new SharedInteger(comps);
		si.outputState = outputState;
		for (IntegerComp c : comps.values()) {
			if ((c.rsIO & AllIn) != 0) {
				this.inputs.remove(c);
				si.inputs.add(c);
				markStateDirty();
			}
			if ((c.rsIO & AllOut) != 0) {
				this.outputs.remove(c);
				si.outputs.add(c);
			}
		}
		si.markStateDirty();
		return si;
	}

	public void setIO(IntegerComp c, short con) {
		if ((con & AllIn) != 0 && (c.rsIO & AllIn) == 0) inputs.add(c);
		else if ((con & AllIn) == 0 && (c.rsIO & AllIn) != 0) inputs.remove(c);
		if ((con & AllOut) != 0 && (c.rsIO & AllOut) == 0) outputs.add(c);
		else if ((con & AllOut) == 0 && (c.rsIO & AllOut) != 0) outputs.remove(c);
		c.rsIO = con;
		markStateDirty();
	}

	@Override
	protected void updatePhysics() {
		if (updateState) { 
			int newState = 0;
			for (IntegerComp c : inputs) newState |= c.inputState;
			if (newState != outputState) {
				outputState = newState;
				for (IntegerComp c : outputs) c.onStateChange();
			}
			updateState = false;
		}
	}

	@Override
	public void markDirty() {
		if (!update) {
			update = true;
			TickRegistry.instance.updates.add(this);
		}
	}

	public void markStateDirty() {
		if (!updateState) {
			updateState = true;
			TickRegistry.instance.updates.add(this);
		}
	}

	@Override
	public void process() {
		if (update) {
			if (core == null)
				for (IntegerComp c : components.values()) { core = c; break; }
			reassembleNetwork();
			update = false;
		}
		updatePhysics();
	}

}
