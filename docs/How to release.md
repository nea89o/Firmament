<!--
SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>

SPDX-License-Identifier: CC0-1.0
-->

# How to create a release

There is a release script to automate some of these actions.

- Bump the version on gradle.properties
- Create a tag with that same version (without `v` prefix, please)
- Create a changelog based on
  `git log --pretty='- %s' --grep '[no changelog]' --invert-grep --fixed-strings oldversion..newversion | tac`, while
  filtering out commits that should not be in the changelog.
- Upload to [GitHub](https://github.com/romangraef/Firmament/releases/new)
- Upload to [Modrinth](https://modrinth.com/mod/firmament/versions)
- Send a message in [Discord](https://discord.com/channels/1088154030628417616/1108565050693783683)
- Send a message in [the thread](https://hypixel.net/threads/firmament-a-skyblock-mod-for-1-20-1-fabric.5446366/)
