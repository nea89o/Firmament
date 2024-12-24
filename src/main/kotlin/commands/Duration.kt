package moe.nea.firmament.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import moe.nea.firmament.util.tr

object DurationArgumentType : ArgumentType<Duration> {
	val unknownTimeCode = DynamicCommandExceptionType { timeCode ->
		tr("firmament.command-argument.duration.error",
		   "Unknown time code '$timeCode'")
	}

	override fun parse(reader: StringReader): Duration {
		val start = reader.cursor
		val string = reader.readUnquotedString()
		val matcher = regex.matcher(string)
		var s = 0
		var time = 0.seconds
		fun createError(till: Int) {
			throw unknownTimeCode.createWithContext(
				reader.also { it.cursor = start + s },
				string.substring(s, till))
		}

		while (matcher.find()) {
			if (matcher.start() != s) {
				createError(matcher.start())
			}
			s = matcher.end()
			val amount = matcher.group("count").toDouble()
			val what = timeSuffixes[matcher.group("what").single()]!!
			time += amount.toDuration(what)
		}
		if (string.length != s) {
			createError(string.length)
		}
		return time
	}


	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val remaining = builder.remainingLowerCase.substringBefore(' ')
		if (remaining.isEmpty()) return super.listSuggestions(context, builder)
		if (remaining.last().isDigit()) {
			for (timeSuffix in timeSuffixes.keys) {
				builder.suggest(remaining + timeSuffix)
			}
		}
		return builder.buildFuture()
	}

	val timeSuffixes = mapOf(
		'm' to DurationUnit.MINUTES,
		's' to DurationUnit.SECONDS,
		'h' to DurationUnit.HOURS,
	)
	val regex = "(?<count>[0-9]+)(?<what>[${timeSuffixes.keys.joinToString("")}])".toPattern()

	override fun getExamples(): Collection<String> {
		return listOf("3m", "20s", "1h45m")
	}
}
