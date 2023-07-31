## Firmament
<small><i>Powered by NEU</i></small>

> Firmament will soon enter its early public alpha stage. While there is still much to be done in terms of usability,
> a lot of features are already ready.
> 
> In the meantime please do not ask for features to be added, [instead check the TODO list](TODO.txt) (although bug
> reports are welcome).

[This project is currently being renamed.](#renaming)

### Currently working features

- Item List of all SkyBlock Items
- Grouping Items that belong together like minions
- Recipe Viewer for Crafting Recipes
- Recipe Viewer for Forge Recipes
- Image Preview in chat
- A storage overview (not a full on overlay that allows you to interact with all pages, but that is planned)
- A crafting overlay when clicking the "Move Item" plus in a crafting recipe
- Cursor position saver
- Slot locking
- Support for custom texture packs (loads item models from `firmskyblock:<skyblock id>` before the vanilla model gets loaded)
- Fairy soul highlighter
- A hud editor powered by [Jarvis](https://github.com/romangraef/jarvis)
- Fishing Helper for Fishing particles (currently not working if you sneak because of 1.20 messing up positioning)
- Basic Config Gui (/firm config). Still needs improvement, but for the basics it's enough.


### Building your own 

Use Java 17.

Running `./gradlew :build` will create a mod jar in `build/libs`

You will need the fabric api mod, the fabric kotlin language mod, the architectury mod and REI in your mods folder in order for this mod to work.

### Licensing and contribution policy

This mod is licensed under a GPL 3.0 or Later license. To read a full license report of all dependencies, execute
`./gradlew :printAllLicenses`.

Contributions are tentatively welcomed. The mod is still in an early stage and lots of things will change and/or are
not properly documented for other developers. If you would still like to try, make sure to add the proper 
[copyright header](HEADER) to your file, or update existing copyright headers with your name and the current year.
Pull requests are welcome through [GitHub](https://github.com/romangraef/Firmament) or via git send-email. Your code
will be publicly available under a GPL license, so make sure that you have the appropriate permissions to grant that
license, or if you are reusing code from somewhere else to properly credit the code and check if the original license
is compatible with ours. If you want, you can license a specific file you write under a less restrictive non copyleft
license with appropriate header, although it may at a later point be upgraded to a GPL licensed file, if another
contributor edits that file.

### Renaming

This method is in the process of being renamed to Firmament from NotEnoughUpdates1.19. As such some of the branding and
code references may still refer to NEU or NotEnoughUpdates. This mod is not related to NEU (although I am at this point
the most active and (by line count) second most prolific NEU dev (after only Moulberry himself)). This mod is also using
NEU data and as such can be referred to as "Firmament powered by NEU". This mod exists outside the Moulberryverse of
mods and is not integrated into the Moulberryverse of quality control and bureaucracy. Any references to Moulberry and
most references to NEU are due to be removed. This mod is not representing NEU in 1.19, although it does have some of
its features.
