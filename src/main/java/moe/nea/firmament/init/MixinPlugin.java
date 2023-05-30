/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.init;

import com.unascribed.lib39.core.api.AutoMixin;

import java.util.List;

// TODO: replace AutoMixin with KSP plugin?
public class MixinPlugin extends AutoMixin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Boolean.getBoolean("firmament.debug") && mixinClassName.contains("devenv.")) {
            return false;
        }
        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }

    @Override
    public List<String> getMixins() {
        var autoDiscoveredMixins = super.getMixins();
        autoDiscoveredMixins.removeIf(it -> !shouldApplyMixin(null, it));
        return autoDiscoveredMixins;
    }
}
