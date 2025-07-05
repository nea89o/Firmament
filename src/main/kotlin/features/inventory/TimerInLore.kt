package moe.nea.firmament.features.inventory

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import net.minecraft.text.Text
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.aqua
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.timestamp
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString

object TimerInLore {
	object TConfig : ManagedConfig("lore-timers", Category.INVENTORY) {
		val showTimers by toggle("show") { true }
		val showCreationTimestamp by toggle("show-creation") { true }
		val timerFormat by choice("format") { TimerFormat.SOCIALIST }
	}

	enum class TimerFormat(val formatter: DateTimeFormatter) : StringIdentifiable {
		RFC(DateTimeFormatter.RFC_1123_DATE_TIME),
		LOCAL(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
		SOCIALIST(
			{
				appendText(ChronoField.DAY_OF_WEEK, TextStyle.SHORT)
				appendLiteral(" ")
				appendValue(ChronoField.DAY_OF_MONTH, 2)
				appendLiteral(".")
				appendValue(ChronoField.MONTH_OF_YEAR, 2)
				appendLiteral(".")
				appendValue(ChronoField.YEAR, 4)
				appendLiteral(" ")
				appendValue(ChronoField.HOUR_OF_DAY, 2)
				appendLiteral(":")
				appendValue(ChronoField.MINUTE_OF_HOUR, 2)
				appendLiteral(":")
				appendValue(ChronoField.SECOND_OF_MINUTE, 2)
			}),
		AMERICAN("EEEE, MMM d h:mm a yyyy"),
		RFCPrecise(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss.SSS Z")),
		;

		constructor(block: DateTimeFormatterBuilder.() -> Unit)
			: this(DateTimeFormatterBuilder().also(block).toFormatter())

		constructor(format: String) : this(DateTimeFormatter.ofPattern(format))

		override fun asString(): String {
			return name
		}
	}

	enum class CountdownTypes(
		val match: String,
		val label: String, // TODO: convert to a string
		val isRelative: Boolean = false,
	) {
		STARTING("Starting in:", "Starts at"),
		STARTS("Starts in:", "Starts at"),
		INTEREST("Interest in:", "Interest at"),
		UNTILINTEREST("Until interest:", "Interest at"),
		ENDS("Ends in:", "Ends at"),
		REMAINING("Remaining:", "Ends at"),
		DURATION("Duration:", "Finishes at"),
		TIMELEFT("Time left:", "Ends at"),
		EVENTTIMELEFT("Event lasts for", "Ends at", isRelative = true),
		SHENSUCKS("Auction ends in:", "Auction ends at"),
		ENDS_PET_LEVELING(
			"Ends:",
			"Finishes at"
		),
		CALENDARDETAILS(" (§e", "Starts at"),
		COMMUNITYPROJECTS("Contribute again", "Come back at"),
		CHOCOLATEFACTORY("Next Charge", "Available at"),
		STONKSAUCTION("Auction ends in", "Ends at"),
		LIZSTONKREDEMPTION("Resets in:", "Resets at"),
		TIMEREMAININGS("Time Remaining:", "Ends at"),
		COOLDOWN("Cooldown:", "Come back at"),
		ONCOOLDOWN("On cooldown:", "Available at"),
		EVENTENDING("Event ends in:", "Ends at");
	}

	val regex =
		"(?i)(?:(?<years>[0-9]+) ?(y|years?) )?(?:(?<days>[0-9]+) ?(d|days?))? ?(?:(?<hours>[0-9]+) ?(h|hours?))? ?(?:(?<minutes>[0-9]+) ?(m|minutes?))? ?(?:(?<seconds>[0-9]+) ?(s|seconds?))?\\b".toRegex()

	@Subscribe
	fun creationInLore(event: ItemTooltipEvent) {
		if (!TConfig.showCreationTimestamp) return
		val timestamp = event.stack.timestamp ?: return
		val formattedTimestamp = TConfig.timerFormat.formatter.format(ZonedDateTime.ofInstant(timestamp, ZoneId.systemDefault()))
		event.lines.add(tr("firmament.lore.creationtimestamp", "Created at: $formattedTimestamp").grey())
	}

	@Subscribe
	fun modifyLore(event: ItemTooltipEvent) {
		if (!TConfig.showTimers) return
		var lastTimer: ZonedDateTime? = null
		for (i in event.lines.indices) {
			val line = event.lines[i].unformattedString
			val countdownType = CountdownTypes.entries.find { it.match in line } ?: continue
			if (countdownType == CountdownTypes.CALENDARDETAILS
				&& !event.stack.displayNameAccordingToNbt.unformattedString.startsWith("Day ")
			) continue

			val countdownMatch = regex.findAll(line).filter { it.value.isNotBlank() }.lastOrNull() ?: continue
			val (years, days, hours, minutes, seconds) =
				listOf("years", "days", "hours", "minutes", "seconds")
					.map {
						countdownMatch.groups[it]?.value?.toLong() ?: 0L
					}
			if (years + days + hours + minutes + seconds == 0L) continue
			var baseLine = ZonedDateTime.now(SBData.hypixelTimeZone)
			if (countdownType.isRelative) {
				if (lastTimer == null) {
					event.lines.add(
						i + 1,
						tr(
							"firmament.loretimer.missingrelative",
							"Found a relative countdown with no baseline (Firmament)"
						).grey()
					)
					continue
				}
				baseLine = lastTimer
			}
			val timer =
				baseLine.plusYears(years).plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds)
			lastTimer = timer
			val localTimer = timer.withZoneSameInstant(ZoneId.systemDefault())
			// TODO: install approximate time stabilization algorithm
			event.lines.add(
				i + 1,
				Text.literal("${countdownType.label}: ")
					.grey()
					.append(Text.literal(TConfig.timerFormat.formatter.format(localTimer)).aqua())
			)
		}
	}

}
