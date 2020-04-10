package io.github.mojira.arisa.modules

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right
import arrow.syntax.function.partially1

class RemoveTriagedMeqsModule(
    private val meqsTags: List<String>,
    private val removalReason: String?
) : Module<RemoveTriagedMeqsModule.Request> {
    data class Comment(
        val body: String,
        val update: (String) -> Either<Throwable, Unit>
    )

    data class Request(
        val priority: String?,
        val triagedTime: String?,
        val comments: List<Comment>
    )

    override fun invoke(request: Request): Either<ModuleError, ModuleResponse> = Either.fx {
        assertTriaged(request.priority, request.triagedTime).bind()

        val updateMeqsComments = request.comments
            .filter{ hasMeqsTag(it.body) }
            .map { it.update.partially1(removeMeqsTags(it.body)) }
        assertNotEmpty(updateMeqsComments).bind()

        tryRunAll(updateMeqsComments).bind()
    }

    private fun hasMeqsTag(comment: String) =
        meqsTags.any { comment.contains(it) }

    private fun removeMeqsTags(comment: String): String {
        val regex = (
            "MEQS(" +
            meqsTags.joinToString("|") { it.replace("MEQS", "") } +
            ")"
        ).toRegex()
        return regex.replace(comment) { "MEQS_ARISA_REMOVED${it.groupValues[1]}${ if (removalReason != null) " Removal Reason: $removalReason" else "" }" }
    }

    private fun assertTriaged(priority: String?, triagedTime: String?) = when {
        priority == null && triagedTime == null -> OperationNotNeededModuleResponse.left()
        else -> Unit.right()
    }
}
