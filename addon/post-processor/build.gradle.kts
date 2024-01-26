/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

import java.util.Properties

plugins {
    id("java")
	id("application")
}

repositories {
    mavenCentral()
}

var props = Properties()
file("../gradle.properties").inputStream().use { props.load(it) }

application {
	mainClass.set("dev.l3g7.griefer_utils.post_processor.Main")
	applicationDefaultJvmArgs = listOf("-Dgriefer_utils.version=" + props.getProperty("version"), "-Dgriefer_utils.debug=" + props.getProperty("debug"))
}

dependencies {
	implementation("com.google.code.gson:gson:2.10.1")
}

java.targetCompatibility = JavaVersion.VERSION_17
