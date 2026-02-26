/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.griefer_pass;

import dev.l3g7.griefer_utils.core.api.BugReporter;

import java.util.regex.Matcher;

public class InstantlyCompletableQuest extends AbstractQuest {

	@Override
	public void init(int index, Matcher matcher, String displayText, boolean approximate, int maxAmount, int maxCompletions) {
		super.init(index, matcher, displayText, approximate, maxAmount, maxCompletions);
		if (maxAmount != 1)
			BugReporter.reportError(new Throwable("InstantlyCompletableQuest with maxAmount " + maxAmount + ": " + matcher.group()));
	}
}
