package dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.internal;

import dev.l3g7.griefer_utils.features.chat.command_suggestions.brigadier.requirements.Requirement;

import java.util.List;

/**
 * A requirement comparing other requirements.
 */
public class NativeBoolOpRequirement {

	/**
	 * A requirement for all children to be true.
	 */
	public static class NativeAndRequirement extends Requirement {

		private final List<Requirement> children;

		public NativeAndRequirement(List<Requirement> children) {
			this.children = children;
		}

		@Override
		public boolean test() {
			for (Requirement child : children)
				if (!child.test())
					return false;

			return true;
		}

	}

	/**
	 * A requirement for any child to be true.
	 */
	public static class NativeOrRequirement extends Requirement {

		private final List<Requirement> children;

		public NativeOrRequirement(List<Requirement> children) {
			this.children = children;
		}

		@Override
		public boolean test() {
			for (Requirement child : children)
				if (child.test())
					return true;

			return false;
		}

	}

	/**
	 * A requirement for the child to be false.
	 */
	public static class NativeNotRequirement extends Requirement {

		private final Requirement child;

		public NativeNotRequirement(Requirement child) {
			this.child = child;
		}

		@Override
		public boolean test() {
			return !child.test();
		}

	}

}
