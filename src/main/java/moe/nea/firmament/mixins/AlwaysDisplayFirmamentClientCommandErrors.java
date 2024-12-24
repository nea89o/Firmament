package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientCommandInternals.class)
public class AlwaysDisplayFirmamentClientCommandErrors {
	@ModifyExpressionValue(method = "executeCommand", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/command/client/ClientCommandInternals;isIgnoredException(Lcom/mojang/brigadier/exceptions/CommandExceptionType;)Z"))
	private static boolean markFirmamentExceptionsAsNotIgnores(boolean original, @Local(argsOnly = true) String command) {
		if (command.startsWith("firm ") || command.equals("firm") || command.startsWith("firmament ") || command.equals("firmament")) {
			return false;
		}
		return original;
	}
}
