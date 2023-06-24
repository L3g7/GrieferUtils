package dev.l3g7.griefer_utils.event.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ShiftAirCheckEvent extends Event {

	public double boundingBoxOffset;

	public ShiftAirCheckEvent(double boundingBoxOffset) {
		this.boundingBoxOffset = boundingBoxOffset;
	}

}
