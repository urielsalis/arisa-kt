package io.github.mojira.arisa

import arrow.core.Either
import arrow.core.left
import arrow.syntax.function.*
import com.uchuhimo.konf.Config
import io.github.mojira.arisa.domain.Issue
import io.github.mojira.arisa.infrastructure.config.Arisa.Credentials
import io.github.mojira.arisa.infrastructure.config.Arisa.Modules
import io.github.mojira.arisa.infrastructure.config.Arisa.Modules.ModuleConfigSpec
import io.github.mojira.arisa.infrastructure.getLanguage
import io.github.mojira.arisa.modules.*
import me.urielsalis.mccrashlib.CrashReader
import java.time.Instant
import java.time.temporal.ChronoUnit

val DEFAULT_JQL = { lastRun: Instant -> "updated > ${lastRun.toEpochMilli()}" }

class ModuleRegistry(private val config: Config) {
    data class Entry(
        val name: String,
        val config: ModuleConfigSpec,
        val getJql: (lastRun: Instant) -> String,
        val execute: (issue: Issue, lastRun: Instant) -> Pair<String, Either<ModuleError, ModuleResponse>>
    )

    private val modules = mutableListOf<Entry>()

    fun getModules(): List<Entry> {
        val onlyModules = modules
            .filter { config[it.config.only] }
        return if (onlyModules.isEmpty()) {
            modules
        } else {
            onlyModules
        }
    }

    private fun register(
        config: ModuleConfigSpec,
        module: Module,
        getJql: (lastRun: Instant) -> String = DEFAULT_JQL
    ) = { issue: Issue, lastRun: Instant ->
        config::class.simpleName!! to
                ({ lastRun pipe (issue pipe2 module::invoke) } pipe ::tryExecuteModule)
    } pipe (getJql pipe2 (config pipe3 (config::class.simpleName!! pipe4 ModuleRegistry::Entry))) pipe modules::add

    @Suppress("TooGenericExceptionCaught")
    private fun tryExecuteModule(executeModule: () -> Either<ModuleError, ModuleResponse>) = try {
        executeModule()
    } catch (e: Throwable) {
        FailedModuleResponse(listOf(e)).left()
    }

    init {
        register(
            Modules.Attachment,
            AttachmentModule(config[Modules.Attachment.extensionBlacklist], config[Modules.Attachment.comment])
        )

        register(Modules.CHK, CHKModule())

        register(
            Modules.ConfirmParent,
            ConfirmParentModule(
                config[Modules.ConfirmParent.confirmationStatusWhitelist],
                config[Modules.ConfirmParent.targetConfirmationStatus],
                config[Modules.ConfirmParent.linkedThreshold]
            )
        )

        register(
            Modules.MultiplePlatforms,
            MultiplePlatformsModule(
                config[Modules.MultiplePlatforms.platformWhitelist],
                config[Modules.MultiplePlatforms.targetPlatform],
                config[Modules.MultiplePlatforms.transferredPlatformBlacklist],
                config[Modules.MultiplePlatforms.keepPlatformTag]
            )
        )

        register(
            Modules.KeepPlatform,
            KeepPlatformModule(
                config[Modules.KeepPlatform.keepPlatformTag]
            )
        )

        register(
            Modules.Crash,
            CrashModule(
                config[Modules.Crash.crashExtensions],
                config[Modules.Crash.duplicates],
                CrashReader(),
                config[Modules.Crash.duplicateMessage],
                config[Modules.Crash.moddedMessage]
            )
        )

        register(
            Modules.MissingCrash,
            MissingCrashModule(
                config[Modules.MissingCrash.crashExtensions],
                CrashReader(),
                config[Modules.MissingCrash.message]
            )
        )

        register(Modules.Empty, EmptyModule(config[Modules.Empty.message]))

        register(
            Modules.DuplicateMessage,
            DuplicateMessageModule(
                config[Modules.DuplicateMessage.commentDelayMinutes],
                config[Modules.DuplicateMessage.message],
                config[Modules.DuplicateMessage.ticketMessages],
                config[Modules.DuplicateMessage.privateMessage],
                config[Modules.DuplicateMessage.resolutionMessages]
            )
        ) { lastRun ->
            val checkStart = lastRun
                .minus(config[Modules.DuplicateMessage.commentDelayMinutes], ChronoUnit.MINUTES)
            val checkEnd = Instant.now()
                .minus(config[Modules.DuplicateMessage.commentDelayMinutes], ChronoUnit.MINUTES)
            "updated > ${checkStart.toEpochMilli()} AND updated < ${checkEnd.toEpochMilli()}"
        }

        register(Modules.HideImpostors, HideImpostorsModule())

        register(
            Modules.RemoveSpam,
            RemoveSpamModule(
                config[Modules.RemoveSpam.patterns]
            )
        )

        register(
            Modules.KeepPrivate, KeepPrivateModule(
                config[Modules.KeepPrivate.tag],
                config[Modules.KeepPrivate.message]
            )
        )

        register(
            Modules.PrivateDuplicate, PrivateDuplicateModule(
                config[Modules.PrivateDuplicate.tag]
            )
        )

        register(Modules.TransferVersions, TransferVersionsModule())

        register(
            Modules.TransferLinks,
            TransferLinksModule()
        )

        register(
            Modules.Piracy, PiracyModule(
                config[Modules.Piracy.piracySignatures],
                config[Modules.Piracy.message]
            )
        )

        register(
            Modules.Privacy,
            PrivacyModule(
                config[Modules.Privacy.message],
                config[Modules.Privacy.commentNote],
                config[Modules.Privacy.allowedEmailRegex].map(String::toRegex)
            )
        )

        register(
            Modules.Language,
            LanguageModule(
                config[Modules.Language.allowedLanguages],
                config[Modules.Language.lengthThreshold],
                ::getLanguage.partially1(config[Credentials.dandelionToken])
            )
        )

        register(Modules.RemoveIdenticalLink, RemoveIdenticalLinkModule())

        register(
            Modules.RemoveNonStaffMeqs,
            RemoveNonStaffMeqsModule(config[Modules.RemoveNonStaffMeqs.removalReason])
        )

        register(
            Modules.RemoveTriagedMeqs,
            RemoveTriagedMeqsModule(
                config[Modules.RemoveTriagedMeqs.meqsTags],
                config[Modules.RemoveTriagedMeqs.removalReason]
            )
        )

        register(
            Modules.ReopenAwaiting,
            ReopenAwaitingModule(
                config[Modules.ReopenAwaiting.blacklistedRoles],
                config[Modules.ReopenAwaiting.blacklistedVisibilities],
                config[Modules.ReopenAwaiting.softARDays],
                config[Modules.ReopenAwaiting.keepARTag],
                config[Modules.ReopenAwaiting.message]
            )
        )

        register(Modules.ReplaceText, ReplaceTextModule())

        register(Modules.RevokeConfirmation, RevokeConfirmationModule())

        register(Modules.ResolveTrash, ResolveTrashModule())

        register(
            Modules.FutureVersion,
            FutureVersionModule(
                config[Modules.FutureVersion.message],
                config[Modules.FutureVersion.panel]
            )
        )

        register(
            Modules.RemoveVersion,
            RemoveVersionModule(
                config[Modules.RemoveVersion.message]
            )
        )

        register(
            Modules.Command,
            CommandModule(config[Modules.Command.commandPrefix])
        )

        register(
            Modules.UpdateLinked,
            UpdateLinkedModule(config[Modules.UpdateLinked.updateIntervalHours])
        ) { lastRun ->
            val now = Instant.now()
            val intervalStart = now.minus(config[Modules.UpdateLinked.updateIntervalHours], ChronoUnit.HOURS)
            val intervalEnd = intervalStart.minusMillis(now.toEpochMilli() - lastRun.toEpochMilli())
            return@register "updated > ${lastRun.toEpochMilli()} OR (updated < ${intervalStart.toEpochMilli()}" +
                    " AND updated > ${intervalEnd.toEpochMilli()})"
        }
    }
}
