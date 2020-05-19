package io.github.mojira.arisa.domain

import arrow.core.Either
import java.time.Instant

data class Issue (
    val key: String,
    val summary: String?,
    val status: String,
    val description: String?,
    val environment: String?,
    val securityLevel: String?,
    val reporter: User?,
    val resolution: String?,
    val created: Instant,
    val updated: Instant,
    val chkField: String?,
    val confirmationStatus: String?,
    val linkedField: Double?,
    val priority: String?,
    val triagedTime: String?,
    val linked: Double?,
    val project: Project,
    val affectedVersions: List<Version>,
    val attachments: List<Attachment>,
    val comments: List<Comment>,
    val links: List<Link>,
    val changeLog: List<ChangeLogItem>,
    val reopen: () -> Either<Throwable, Unit>,
    val resolveAsAwaitingResponse: () -> Either<Throwable, Unit>,
    val resolveAsInvalid: () -> Either<Throwable, Unit>,
    val resolveAsDuplicate: () -> Either<Throwable, Unit>,
    val resolveAsIncomplete: () -> Either<Throwable, Unit>,
    val updateDescription: (description: String) -> Either<Throwable, Unit>,
    val updateCHK: () -> Either<Throwable, Unit>,
    val updateConfirmationStatus: (String) -> Either<Throwable, Unit>,
    val updateLinked: (Double) -> Either<Throwable, Unit>,
    val setPrivate: () -> Either<Throwable, Unit>,
    val createLink: (key: String, type: String) -> Either<Throwable, Unit>,
    val addAffectedVersion: (id: String) -> Either<Throwable, Unit>,
    val addEmptyComment: () -> Either<Throwable, Unit>,
    val addModdedComment: () -> Either<Throwable, Unit>,
    val addCrashDupeComment: (key: String) -> Either<Throwable, Unit>,
    val addFutureVersionComment: () -> Either<Throwable, Unit>,
    val addKeepPrivateComment: () -> Either<Throwable, Unit>,
    val addNotEnglishComment: (lang: String) -> Either<Throwable, Unit>,
    val addPiracyComment: () -> Either<Throwable, Unit>
)