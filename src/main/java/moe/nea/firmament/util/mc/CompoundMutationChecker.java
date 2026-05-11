package moe.nea.firmament.util.mc;

import io.github.notenoughupdates.moulconfig.internal.InitUtil;
import kotlin.Unit;
import moe.nea.firmament.init.CompoundTagMutationFinalizationDetectorInjector;
import moe.nea.firmament.util.ErrorUtil;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class CompoundMutationChecker {
	private static final Logger log = LoggerFactory.getLogger(CompoundMutationChecker.class);

	public static void internalCheckForMutation(CompoundTag copy, CompoundTag original) {
		if (copy.equals(original)) return;
		log.info("Some code-path modified a not for mutation CompoundTag!\n\nOriginal:\n\n{}\n\nModifier:\n\n{}\n", original, copy);
	}

	public static class Holder {
		public static MethodHandle methodHandle = InitUtil.makeUnchecked(
			() -> MethodHandles.lookup()
				.findSetter(CompoundTag.class, CompoundTagMutationFinalizationDetectorInjector.MUTATION_FIELD_NAME, CompoundTag.class)
		);
	}


	public static CompoundTag disallowMutations(CompoundTag compoundTag) {
		if (ErrorUtil.aggressiveErrors) {
			var copy = compoundTag.copy();
			ErrorUtil._catch("Could not insert mutation check", () -> {
				Holder.methodHandle.invoke(copy, compoundTag);
				return Unit.INSTANCE;
			});
			return copy;
		}
		return compoundTag;
	}
}
