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

# Update string constants that contain class names to their obfuscated equivalents.
# Needed for FirmamentAPI (Class.forName("moe.nea.firmament.impl.v1.FirmamentAPIImpl")).
-adaptclassstrings

# Keep all ServiceLoader service interfaces and their implementations.
# ProGuard cannot trace ServiceLoader's dynamic class loading, so implementations
# would otherwise be removed. The interfaces must also be kept (not just adapted via
# -adaptresourcefilenames) because ProGuard does not reliably rename extensionless
# META-INF/services files, so ServiceLoader would look for the obfuscated name and
# find nothing.
-keep interface moe.nea.firmament.events.subscription.SubscriptionList
-keep class * implements moe.nea.firmament.events.subscription.SubscriptionList { *; }
-keep interface moe.nea.firmament.gui.config.FirmamentConfigScreenProvider
-keep class * implements moe.nea.firmament.gui.config.FirmamentConfigScreenProvider { *; }
-keep interface moe.nea.firmament.util.HoveredItemStackProvider
-keep class * implements moe.nea.firmament.util.HoveredItemStackProvider { *; }
-keep interface moe.nea.firmament.util.compatloader.ICompatMetaGen
-keep class * implements moe.nea.firmament.util.compatloader.ICompatMetaGen { *; }
-keep interface moe.nea.firmament.util.data.IConfigProvider
-keep class * implements moe.nea.firmament.util.data.IConfigProvider { *; }

# Preserve method names across all Firmament classes to avoid breaking Minecraft interface
# implementations. The compileClasspath (ProGuard library JARs) uses Yarn-mapped Minecraft
# (e.g. location()), but the input JAR is post-remapJar and uses Intermediary names
# (e.g. method_56926()). ProGuard cannot match these names, so it freely removes/renames
# methods that implement Minecraft interfaces, causing AbstractMethodError at runtime.
#
# -dontshrink: prevents removal of interface implementation methods that ProGuard thinks
# are unreachable (because it can't match intermediary names to yarn library names).
# Firmament has minimal dead code anyway so this is acceptable.
-dontshrink
#
# -keepclassmembernames: prevents renaming of non-private methods so that intermediary
# method names (method_56926, etc.) are preserved for Minecraft's runtime dispatch.
-keepclassmembernames class moe.nea.firmament.** {
    public <methods>;
    protected <methods>;
}

# Keep enum members accessed via Class.getEnumConstants() / EnumSet / reflection.
# ProGuard removes values() and valueOf() from enums because it cannot trace that
# Class.getEnumConstants() calls values() reflectively. This causes a NPE when any
# code (e.g. ManagedConfig.choice()) calls enumClass.enumConstants.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep AutoService service files consistent
-adaptresourcefilenames META-INF/services/**
-adaptresourcefilecontents META-INF/services/**

# Move all obfuscated classes into a flat package to hide the moe/nea/firmament prefix.
# Kept classes (mixins, entry points, API, service impls) remain at their original names.
-repackageclasses 'a'
-allowaccessmodification

# Safety flags
-dontoptimize
-dontwarn **
