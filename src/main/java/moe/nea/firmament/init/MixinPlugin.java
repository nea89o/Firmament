

package moe.nea.firmament.init;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MixinPlugin implements IMixinConfigPlugin {

    AutoDiscoveryPlugin autoDiscoveryPlugin = new AutoDiscoveryPlugin();
	public static List<MixinPlugin> instances = new ArrayList<>();
	public String mixinPackage;
    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
		instances.add(this);
        this.mixinPackage = mixinPackage;
        autoDiscoveryPlugin.setMixinPackage(mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Boolean.getBoolean("firmament.debug") && mixinClassName.contains("devenv.")) {
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return autoDiscoveryPlugin.getMixins().stream().filter(it -> this.shouldApplyMixin(null, it))
            .toList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    public List<String> appliedMixins = new ArrayList<>();

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        appliedMixins.add(mixinClassName);
    }
}
