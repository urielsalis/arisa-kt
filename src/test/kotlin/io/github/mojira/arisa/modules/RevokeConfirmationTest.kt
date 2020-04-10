package io.github.mojira.arisa.modules

import arrow.core.left
import arrow.core.right
import io.github.mojira.arisa.modules.RevokeConfirmationModule.ChangeLogItem
import io.github.mojira.arisa.modules.RevokeConfirmationModule.Request
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class RevokeConfirmationTest : StringSpec({
    "should return OperationNotNeededModuleResponse when Ticket is unconfirmed and confirmation was never changed" {
        val module = RevokeConfirmationModule()
        val request = Request("Unconfirmed", emptyList()) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when Ticket is confirmed and was changed by staff" {
        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Confirmation Status", "Confirmed", listOf("staff"))
        val request = Request("Confirmed", listOf(changeLogItem)) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when Ticket is confirmed and was changed by helper" {
        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Confirmation Status", "Confirmed", listOf("helper"))
        val request = Request("Confirmed", listOf(changeLogItem)) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when Ticket is confirmed and was changed by global-moderator" {
        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Confirmation Status", "Confirmed", listOf("global-moderators"))
        val request = Request("Confirmed", listOf(changeLogItem)) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when Ticket is confirmed and groups are unknown" {
        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Confirmation Status", "Confirmed", null)
        val request = Request("Confirmed", listOf(changeLogItem)) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when multiple volunteers changed the confirmation status" {
        val module = RevokeConfirmationModule()
        val volunteerChange = ChangeLogItem("Confirmation Status", "Confirmed", listOf("staff"))
        val userChange = ChangeLogItem("Confirmation Status", "Unconfirmed", listOf("helper"))
        val request = Request("Unconfirmed", listOf(volunteerChange, userChange)) { Unit.right() }

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should set to Unconfirmed when ticket was created Confirmed" {
        var changedConfirmation = ""

        val module = RevokeConfirmationModule()
        val request = Request("Confirmed", emptyList()) { changedConfirmation = it; Unit.right() }

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        changedConfirmation.shouldBe("Unconfirmed")
    }

    "should set to Unconfirmed when there is a changelog entry unrelated to confirmation status" {
        var changedConfirmation = ""

        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Totally Not Confirmation Status", "Confirmed", listOf("staff"))
        val request = Request("Confirmed", listOf(changeLogItem)) { changedConfirmation = it; Unit.right() }

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        changedConfirmation.shouldBe("Unconfirmed")
    }

    "should set to Unconfirmed when ticket was Confirmed by a non-volunteer" {
        var changedConfirmation = ""

        val module = RevokeConfirmationModule()
        val changeLogItem = ChangeLogItem("Confirmation Status", "Confirmed", emptyList())
        val request = Request("Confirmed", listOf(changeLogItem)) { it -> changedConfirmation = it; Unit.right() }

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        changedConfirmation.shouldBe("Unconfirmed")
    }

    "should set back to status set by volunteer, when regular user changes confirmation status" {
        var changedConfirmation = ""

        val module = RevokeConfirmationModule()
        val volunteerChange = ChangeLogItem("Confirmation Status", "Confirmed", listOf("staff"))
        val userChange = ChangeLogItem("Confirmation Status", "Unconfirmed", listOf("users"))
        val request = Request("Unconfirmed", listOf(volunteerChange, userChange)) { changedConfirmation = it; Unit.right() }

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        changedConfirmation.shouldBe("Confirmed")
    }

    "should return FailedModuleResponse when changing confirmation status fails" {
        val module = RevokeConfirmationModule()
        val request = Request("Confirmed", emptyList()) { RuntimeException().left() }

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }
})
