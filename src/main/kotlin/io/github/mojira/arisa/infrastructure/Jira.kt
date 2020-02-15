package io.github.mojira.arisa.infrastructure

import arrow.core.Either
import com.uchuhimo.konf.Config
import io.github.mojira.arisa.infrastructure.config.Arisa
import kotlinx.coroutines.runBlocking
import net.rcarz.jiraclient.Attachment
import net.rcarz.jiraclient.BasicCredentials
import net.rcarz.jiraclient.Issue
import net.rcarz.jiraclient.JiraClient
import java.time.Instant

typealias IssueId = String

fun updateCHK(config: Config, jiraClient: JiraClient, issueId: IssueId): Either<Throwable, Unit> = runBlocking {
    Either.catch {
        jiraClient
            .getIssue(issueId)
            .update()
            .field(config[Arisa.CustomFields.chkField], Instant.now().toString())
            .execute()
    }
}

fun deleteAttachment(jiraClient: JiraClient, attachment: Attachment): Either<Throwable, Unit> = runBlocking {
    Either.catch {
        jiraClient.restClient.delete(Attachment.getBaseUri() + attachment.id)
        Unit
    }
}

fun connectToJira(username: String, password: String, url: String) =
    JiraClient(url, BasicCredentials(username, password))

fun reopenIssue(issue: Issue) = runBlocking {
    Either.catch {
        issue.transition().execute("Reopen Issue")
    }
}
