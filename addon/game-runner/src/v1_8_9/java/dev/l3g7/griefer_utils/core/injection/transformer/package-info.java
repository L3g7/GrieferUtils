/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.transformer;
/*
* All of these transformers could be replaced by Mixins, but that causes classloading issues.
* Schematica's classes are loaded too early by Mixin and it's way easier to keep the Transformers than to fix Mixin.
* */