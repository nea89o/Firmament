<!--
SPDX-FileCopyrightText: 2026 Linnea Gräf <nea@nea.moe>

SPDX-License-Identifier: CC0-1.0
-->

# How to create a release

There is a [GH workflow](../.github/workflows/build.yml) to automate most of the release process.

- Create a tag for the new version.
  - The tag should not start with `v`.
  - The tag should have a regular version triple. There is no semver here, we are all about `marketing.whatever.bullshit`. Last version bumps should be for small bugfix releases, but anything else is fair game.
  - The tag should end with `+mc1.2.3` minecraft version. Absolutely **NO** dashes in tag names, unlike branches.
  - If there are two releases with roughly the same features, just for different MC versions, feel free to tag them accordingly.
  - A full tag might look like `44.3.0+mc26.1`.
- Once the tag is pushed to GitHub (might need to re-push, GitHub is sometimes bad about workflow dispatches), GHA will run a build and create a draft release with [generated changelog](./generate-changelog.sh) and ping you in the confidential dev channel. 
- At this point you can edit the generated release notes and then push release it.
- An upload to [Modrinth](https://modrinth.com/mod/firmament/versions) should happen [automatically](../.github/workflows/publish-github-to-modrinth.yml).
- Send a message in [Discord](https://discord.com/channels/1088154030628417616/1108565050693783683).
  - Ping Firmament Notifications (or Greek Notifs for betas). 
  - TODO: will people trust it if a bot/webhook writes a message? Maybe, maybe not.
