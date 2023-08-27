// SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
//
// SPDX-License-Identifier: CC0-1.0

import moe.nea.licenseextractificator.LicenseExtension

fun LicenseExtension.addExtraLicenseMatchers() {
    solo {
        name = "Firmament"
        description = "A Hypixel SkyBlock mod"
        developer("Linnea Gräf") {
            webPresence = "https://nea.moe/"
        }
        spdxLicense.`GPL-3-0-or-later`()
        webPresence = "https://git.nea.moe/nea/Firmament/"
    }
    match {
        if (group == "net.minecraft") useLicense {
            name = "Minecraft"
            description = "Minecraft - The critically acclaimed video game"
            license("All Rights Reserved", "https://www.minecraft.net/en-us/eula")
            developer("Mojang") {
                webPresence = "https://mojang.com"
            }
            webPresence = "https://www.minecraft.net/en-us"
        }
        if (module == "architectury") useLicense {
            name = "Architectury API"
            description = "An intermediary api aimed at easing development of multiplatform mods."
            spdxLicense.`LGPL-3-0-or-later`()
            developer("Architectury") {
                webPresence = "https://docs.architectury.dev/"
            }
            webPresence = "https://github.com/architectury/architectury-api"
        }
        if (module.startsWith("RoughlyEnoughItems")) useLicense {
            name = module
            description = "Your recipe viewer mod for 1.13+."
            spdxLicense.MIT()
            developer("Shedaniel") {
                webPresence = "https://shedaniel.me/"
            }
            webPresence = "https://github.com/shedaniel/RoughlyEnoughItems"
        }
        if (module == "cloth-config") useLicense {
            name = "Cloth Config"
            description = "Client sided configuration API"
            spdxLicense.`LGPL-3-0-or-later`()
            developer("Shedaniel") {
                webPresence = "https://shedaniel.me/"
            }
            webPresence = "https://github.com/shedaniel/cloth-config"
        }
        if (module == "basic-math") useLicense {
            name = "Cloth BasicMath"
            description = "Basic Math Operations"
            spdxLicense.Unlicense()
            developer("Shedaniel") {
                webPresence = "https://shedaniel.me/"
            }
            webPresence = "https://github.com/shedaniel/cloth-basic-math"
        }
        if (module == "fabric-language-kotlin") useLicense {
            name = "Fabric Language Kotlin"
            description = "Kotlin Language Support for Fabric mods"
            webPresence = "https://github.com/FabricMC/fabric-language-kotlin"
            spdxLicense.`Apache-2-0`()
            developer("FabricMC") {
                webPresence = "https://fabricmc.net/"
            }
        }
        if (group == "com.mojang") useLicense {
            name = module
            description = "Mojang library packaged by Minecraft"
        }
    }
    module("net.fabricmc", "yarn") {
        name = "Yarn"
        description = "Libre Minecraft mappings, free to use for everyone. No exceptions."
        spdxLicense.`CC0-1-0`()
        developer("FabricMC") {
            webPresence = "https://fabricmc.net/"
        }
        webPresence = "https://github.com/FabricMC/yarn/"
    }
    module("com.mojang", "datafixerupper") {
        name = "DataFixerUpper"
        description =
            "A set of utilities designed for incremental building, merging and optimization of data transformations."
        spdxLicense.MIT()
        developer("Mojang") {
            webPresence = "https://mojang.com"
        }
        webPresence = "https://github.com/Mojang/DataFixerUpper"
    }
    module("com.mojang", "brigadier") {
        name = "Brigadier"
        description = "Brigadier is a command parser & dispatcher, designed and developed for Minecraft: Java Edition."
        spdxLicense.MIT()
        developer("Mojang") {
            webPresence = "https://mojang.com"
        }
        webPresence = "https://github.com/Mojang/brigadier"
    }
    module("net.fabricmc", "tiny-remapper") {
        name = "Tiny Remapper"
        description = "Tiny JAR remapping tool"
        spdxLicense.`LGPL-3-0-or-later`()
        webPresence = "https://github.com/FabricMC/tiny-remapper"
        developer("FabricMC") {
            webPresence = "https://fabricmc.net/"
        }
    }
    module("net.fabricmc", "sponge-mixin") {
        name = "Mixin"
        description = "Mixin is a trait/mixin framework for Java using ASM"
        spdxLicense.MIT()
        webPresence = "https://github.com/FabricMC/mixin"
        developer("FabricMC") {
            webPresence = "https://fabricmc.net/"
        }
        developer("SpongePowered") {
            webPresence = "https://spongepowered.org/"
        }
    }
    module("net.fabricmc", "tiny-mappings-parser") {
        name = "Tiny Mappings Parser"
        webPresence = "https://github.com/fabricMC/tiny-mappings-parser"
        description = "Library for parsing .tiny mapping files"
        developer("FabricMC") {
            webPresence = "https://fabricmc.net/"
        }
        spdxLicense.`Apache-2-0`()
    }
    module("net.fabricmc", "fabric-loader") {
        name = "Fabric Loader"
        description = " Fabric's mostly-version-independent mod loader."
        spdxLicense.`Apache-2-0`()
        developer("FabricMC") {
            webPresence = "https://fabricmc.net/"
        }
        webPresence = "https://github.com/FabricMC/fabric-loader/"
    }
}
