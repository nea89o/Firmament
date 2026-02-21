# SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
#
# SPDX-License-Identifier: GPL-3.0-or-later

# Keep annotations and Kotlin metadata (needed for reflection and Kotlin interop)
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions, SourceFile, LineNumberTable
-keep class kotlin.Metadata { *; }

# Keep Fabric entry points (referenced as strings in fabric.mod.json)
-keep class moe.nea.firmament.Firmament { public static void on*(...); }
-keep class moe.nea.firmament.init.EarlyRiser { *; }
-keep class moe.nea.firmament.compat.rei.FirmamentReiPlugin { *; }
-keep class moe.nea.firmament.compat.rei.FirmamentReiCommonPlugin { *; }
-keep class moe.nea.firmament.compat.modmenu.FirmamentModMenuPlugin { *; }
-keep class moe.nea.firmament.compat.jade.FirmamentJadePlugin { *; }
-keep class moe.nea.firmament.jarvis.JarvisIntegration { *; }

# Keep mixin infrastructure (referenced by string in firmament.mixins.json)
-keep class moe.nea.firmament.init.MixinPlugin { *; }
-keep @org.spongepowered.asm.mixin.Mixin class * { *; }
-keepclassmembers class * {
    @org.spongepowered.asm.mixin.injection.* <methods>;
    @org.spongepowered.asm.mixin.Shadow <methods>;
    @org.spongepowered.asm.mixin.Shadow <fields>;
    @org.spongepowered.asm.mixin.Overwrite <methods>;
}

# Keep public API and shaded deps unchanged
-keep class moe.nea.firmament.api.** { *; }
-keep class moe.nea.firmament.deps.** { *; }

# Keep AutoService service files consistent
-adaptresourcefilenames META-INF/services/**
-adaptresourcefilecontents META-INF/services/**

# Safety flags
-dontoptimize
-dontwarn **
