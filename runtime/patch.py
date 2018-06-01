#!/usr/bin/env python2

import commands

cmd = commands.Commands()
cmd.apply_patch_dir("./patches/", "src/minecraft_server")
