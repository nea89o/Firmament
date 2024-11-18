package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import moe.nea.firmament.util.ErrorUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

@Mixin(CommandDispatcher.class)
public class RedirectWithoutSubCommands<S> {
	@Inject(
		method = "parseNodes",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/context/CommandContextBuilder;withCommand(Lcom/mojang/brigadier/Command;)Lcom/mojang/brigadier/context/CommandContextBuilder;",
			shift = At.Shift.AFTER
		)
	)
	private void injectCommandForRedirects(
		CommandNode<S> node, StringReader originalReader, CommandContextBuilder<S> contextSoFar, CallbackInfoReturnable<ParseResults<S>> cir,
		@Local(index = 10) CommandContextBuilder<S> context,
		@Local(index = 9) CommandNode<S> child
	) {
		var p = child;
		var set = new HashSet<>();
		if (context.getCommand() == null && p.getRedirect() != null) {
			p = p.getRedirect();
			context.withCommand(p.getCommand());
			if (!set.add(p)) {
				ErrorUtil.INSTANCE.softError("Redirect circle detected in " + p);
			}
		}
	}
}
