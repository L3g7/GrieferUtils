#
# This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
# Copyright (c) L3g7.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
set -u

# Accepted env vars: PRISM_BASE_ROOT, PRISM_BINARY

if [ -f /etc/os-release ]; then
	# Linux
	DEFAULT_PRISM_BASE_ROOT="$HOME/.local/share/PrismLauncher/instances"
	DEFAULT_PRISM_BINARY="prismlauncher"
else
	# Windows (or macOS, but macOS is currently unsupported)
	DEFAULT_PRISM_BASE_ROOT="$APPDATA/PrismLauncher/instances"
	DEFAULT_PRISM_BINARY="$LOCALAPPDATA/Programs/PrismLauncher/prismlauncher.exe"
fi

PRISM_BASE_ROOT=${PRISM_BASE_ROOT:=$DEFAULT_PRISM_BASE_ROOT}
ADDONS_DIR="$PRISM_BASE_ROOT"/"$PRISM_INSTANCE_NAME"/minecraft/LabyMod/addons-1.8
PRISM_BINARY=${PRISM_BINARY:=$DEFAULT_PRISM_BINARY}

rm "$ADDONS_DIR"/griefer-utils-*
cp ./build/libs/griefer-utils* "$ADDONS_DIR"

ls "$ADDONS_DIR"/griefer-utils*

$PRISM_BINARY -l "$PRISM_INSTANCE_NAME"