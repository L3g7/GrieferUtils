/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

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
	public final int noteId;

	public NoteBlockPlayEvent(BlockPos pos, int noteId) {
		this.pos = pos;
		this.noteId = noteId;
	}

	public Note getNote() {
		return Note.fromId(noteId);
	}

	public Octave getOctave() {
		return Octave.fromId(noteId);
	}

	public int getVanillaNoteId() {
		return noteId;
	}

	public enum Note {

		F_SHARP,
		G,
		G_SHARP,
		A,
		A_SHARP,
		B,
		C,
		C_SHARP,
		D,
		D_SHARP,
		E,
		F;

		static Note fromId(int id)
		{
			return values()[id % 12];
		}

	}

	public enum Octave {

		LOW,
		MID,
		HIGH; // only valid for F_SHARP

		static Octave fromId(int id)	{
			return id < 12 ? LOW : id == 24 ? HIGH : MID;
		}

	}

	@Mixin(BlockNote.class)
	private static class MixinBlockNote {

		@Inject(method = "onBlockEventReceived", at = @At("HEAD"))
		public void injectOnBlockEventReceived(World worldIn, BlockPos pos, IBlockState state, int eventID, int eventParam, CallbackInfoReturnable<Boolean> cir) {
			new NoteBlockPlayEvent(pos, eventParam).fire();
		}

	}

}
