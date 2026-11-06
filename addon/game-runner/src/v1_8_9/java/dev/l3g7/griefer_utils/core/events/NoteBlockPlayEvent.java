/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import com.google.common.base.Strings;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import net.minecraft.block.BlockNote;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class NoteBlockPlayEvent extends Event {

	public final BlockPos pos;
	public final String note;
	public final int noteId;

	public NoteBlockPlayEvent(BlockPos pos, int noteId) {
		this.pos = pos;
		this.noteId = noteId;

		String[] NOTES = new String[] { "Fis", "G", "Gis", "A", "Ais", "B", "C", "Cis", "D", "Dis", "E", "F" };
		String note = NOTES[noteId % 12];
		int octave = (noteId / 12) + 1;

		this.note = note + Strings.repeat("'", octave);
	}

	@Mixin(BlockNote.class)
	private static class MixinBlockNote {

		@Inject(method = "onBlockEventReceived", at = @At("HEAD"))
		public void injectOnBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam, CallbackInfoReturnable<Boolean> cir) {
			new NoteBlockPlayEvent(pos, eventParam).fire();
		}

	}

}