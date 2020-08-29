package io.github.mojira.arisa.modules

import arrow.core.Either
import arrow.core.extensions.fx
import io.github.mojira.arisa.domain.Issue
import java.time.Instant

class RemoveVersionModule : Module {
    override fun invoke(issue: Issue, lastRun: Instant): Either<ModuleError, ModuleResponse> = with(issue) {
        Either.fx {
            val addedVersions = getExtraVersionsLatelyAddedByNonVolunteers(lastRun)
            val removeAddedVersions = affectedVersions
                .filter { it.id in addedVersions }
                .map { it.remove }
            assertNotEmpty(removeAddedVersions).bind()
            removeAddedVersions.forEach(::run)
        }
    }

    private fun Issue.getExtraVersionsLatelyAddedByNonVolunteers(lastRun: Instant): List<String> =
        if (created.isAfter(lastRun)) {
            if (resolution == "Unresolved" || isVolunteer(reporter?.getGroups?.invoke())) {
                emptyList()
            } else {
                affectedVersions.map { ver -> ver.id }
            }
        } else {
            changeLog
                .asSequence()
                .filter { it.created.isAfter(lastRun) }
                .filter(::isResolved)
                .filter { it.field.toLowerCase() == "version" }
                .filterNot { isVolunteer(it.getAuthorGroups()) }
                .mapNotNull { it.changedTo }
                .toList()
        }
    
    private fun isResolved(item: io.github.mojira.arisa.domain.ChangeLogItem) =
        item.field == "Resolution" && item.changedTo != null &&
        item.changedToString != null && item.changedToString != "Unresolved"

    private fun isVolunteer(groups: List<String>?) =
        groups?.any { it == "helper" || it == "global-moderators" || it == "staff" } ?: false
}
