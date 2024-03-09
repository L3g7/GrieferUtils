### Building without LabyMod 3 compatibility
- run `build`
- run `post-processor:run`
### Building with LabyMod 3 compatibility
Since this branch is currently not compatible with LabyMod 3, GrieferUtils just loads a compatible jar.
- run `build`
- run `post-processor:run`
- build `v2` branch
- rename jar from `v2` to `griefer_utils_labymod_3.jar`
- move `griefer_utils_labymod_3.jar` into jar created by `post-processor:run`
