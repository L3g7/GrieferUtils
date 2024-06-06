/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.event_bus;

/**
 * The priority of an event listener.
 * Listeners with a higher priority will be called sooner than listeners with a lower priority.
 * @see EventListener
 */
public enum Priority {

	HIGHEST, HIGH, NORMAL, LOW, LOWEST

}
