package io.github.mojira.arisa.modules

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.syntax.function.partially1
import io.github.mojira.arisa.domain.Issue
import java.time.Instant

class AttachmentModule(
    private val extensionBlackList: List<String>
) : Module {

    override fun invoke(issue: Issue, lastRun: Instant): Either<ModuleError, ModuleResponse> = with(issue) {
        Either.fx {
            val endsWithBlacklistedExtensionAdapter = ::endsWithBlacklistedExtensions.partially1(extensionBlackList)
            val functions = attachments
                .filter { (name, _) -> endsWithBlacklistedExtensionAdapter(name) }
                .map { it.remove }
            assertNotEmpty(functions).bind()
            tryRunAll(functions).bind()
        }
    }

    private fun endsWithBlacklistedExtensions(extensionBlackList: List<String>, name: String) =
        extensionBlackList.any { name.endsWith(it) }
}
