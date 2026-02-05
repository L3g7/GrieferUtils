package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.features.Feature;

import java.util.List;

/**
 * {@link Feature} or {@link Feature.CategoryData}.
 */
public interface GUIEntry {
	String name();

	void addToParent(List<BaseSetting<?>> root);
}
