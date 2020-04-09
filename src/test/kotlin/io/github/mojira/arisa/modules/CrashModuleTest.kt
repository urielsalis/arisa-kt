package io.github.mojira.arisa.modules

import arrow.core.left
import arrow.core.right
import io.github.mojira.arisa.infrastructure.config.CrashDupeConfig
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.rcarz.jiraclient.Attachment
import net.rcarz.jiraclient.Issue
import java.util.Calendar
import java.util.Calendar.DAY_OF_YEAR
import java.util.Date

private val ISSUE = mockk<Issue>()
const val EXAMPLE_CRASH = "---- Minecraft Crash Report ----\n" +
        "// Daisy, daisy...\n" +
        "\n" +
        "Time: 6/28/17 11:44 AM\n" +
        "Description: Initializing game\n" +
        "\n" +
        "org.lwjgl.LWJGLException: Pixel format not accelerated\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 1.8.9\n" +
        "\tJava Version: 1.8.0_131, Oracle Corporation\n" +
        "Is Modded: Probably not. Jar signature remains and client brand is untouched.\n"

const val SERVER_UNMODDED_CRASH = "---- Minecraft Crash Report ----\n" +
        "// Oh - I know what I did wrong!\n" +
        "\n" +
        "Time: 6/5/18 9:20 PM\n" +
        "Description: Exception generating new chunk\n" +
        "\n" +
        "java.util.concurrent.ExecutionException: java.lang.RuntimeException: We are asking a region for a chunk out of bound | -174 8\n" +
        "\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 1.13-pre1\n" +
        "\tOperating System: Linux (amd64) version 4.4.0-98-generic\n" +
        "\tJava Version: 1.8.0_151, Oracle Corporation\n" +
        "\tJava VM Version: Java HotSpot(TM) 64-Bit Server VM (mixed mode), Oracle Corporation\n" +
        "\tMemory: 614132352 bytes (585 MB) / 988282880 bytes (942 MB) up to 3340763136 bytes (3186 MB)\n" +
        "\tJVM Flags: 2 total; -Xmx3584M -XX:MaxPermSize=256M\n" +
        "\tProfiler Position: N/A (disabled)\n" +
        "\tPlayer Count: 1 / 20; [so['CENSORED'/351, l='Vanilla', x=8.50, y=72.00, z=121.50]]\n" +
        "\tData Packs: vanilla\n" +
        "\tIs Modded: Unknown (can't tell)\n" +
        "\tType: Dedicated Server (map_server.txt)\n"

const val SERVER_MODDED_CRASH = "---- Minecraft Crash Report ----\n" +
        "// Oh - I know what I did wrong!\n" +
        "\n" +
        "Time: 6/5/18 9:20 PM\n" +
        "Description: Exception generating new chunk\n" +
        "\n" +
        "java.util.concurrent.ExecutionException: java.lang.RuntimeException: We are asking a region for a chunk out of bound | -174 8\n" +
        "\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 1.13-pre1\n" +
        "\tOperating System: Linux (amd64) version 4.4.0-98-generic\n" +
        "\tJava Version: 1.8.0_151, Oracle Corporation\n" +
        "\tJava VM Version: Java HotSpot(TM) 64-Bit Server VM (mixed mode), Oracle Corporation\n" +
        "\tMemory: 614132352 bytes (585 MB) / 988282880 bytes (942 MB) up to 3340763136 bytes (3186 MB)\n" +
        "\tJVM Flags: 2 total; -Xmx3584M -XX:MaxPermSize=256M\n" +
        "\tProfiler Position: N/A (disabled)\n" +
        "\tPlayer Count: 1 / 20; [so['CENSORED'/351, l='Vanilla', x=8.50, y=72.00, z=121.50]]\n" +
        "\tData Packs: vanilla\n" +
        "\tIs Modded: Definitely; Server brand changed to 'fabric'\n" +
        "\tType: Dedicated Server (map_server.txt)\n"

const val SERVER_MODDED_CRASH_2 = "---- Minecraft Crash Report ----\n" +
        "// Surprise! Haha. Well, this is awkward.\n" +
        "\n" +
        "Time: 04.10.19 17:39\n" +
        "Description: Exception ticking world\n" +
        "\n" +
        "java.lang.NoSuchFieldError: DO_DAYLIGHT_CYCLE\n" +
        "\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 1.14.4\n" +
        "\tMinecraft Version ID: 1.14.4\n" +
        "\tOperating System: Windows 10 (amd64) version 10.0\n" +
        "\tJava Version: 1.8.0_51, Oracle Corporation\n" +
        "\tJava VM Version: Java HotSpot(TM) 64-Bit Server VM (mixed mode), Oracle Corporation\n" +
        "\tMemory: 1192941208 bytes (1137 MB) / 1845493760 bytes (1760 MB) up to 2147483648 bytes (2048 MB)\n" +
        "\tCPUs: 4\n" +
        "\tJVM Flags: 9 total; -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump -Xss1M -Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M\n" +
        "\tPlayer Count: 0 / 8; []\n" +
        "\tData Packs: vanilla\n" +
        "\tType: Integrated Server (map_client.txt)\n" +
        "\tIs Modded: Very likely; Jar signature invalidated\n"

const val EXAMPLE_CRASH_2 = "---- Minecraft Crash Report ----\n" +
        "// I feel sad now :(\n" +
        "\n" +
        "Time: 12/04/18 18:49\n" +
        "Description: Initializing game\n" +
        "\n" +
        "java.lang.IllegalStateException: GLFW error 65542: WGL: The driver does not appear to support OpenGL\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 18w15a\n" +
        "\tJava Version: 1.8.0_25, Oracle Corporation\n" +
        "\tIs Modded: Probably not. Jar signature remains and client brand is untouched.\n"

const val MODDED_CRASH = "---- Minecraft Crash Report ----\n" +
        "// You're mean.\n" +
        "\n" +
        "Time: 06.11.19 10:54\n" +
        "Description: mouseClicked event handler\n" +
        "\n" +
        "java.lang.NullPointerException: mouseClicked event handler\n" +
        "\tat com.replaymod.simplepathing.ReplayModSimplePathing.onReplayClosing(ReplayModSimplePathing.java:129)\n" +
        "-- System Details --\n" +
        "Details:\n" +
        "\tMinecraft Version: 1.14.4\n" +
        "\tMinecraft Version ID: 1.14.4\n" +
        "\tOperating System: Windows 10 (amd64) version 10.0\n" +
        "\tJava Version: 1.8.0_51, Oracle Corporation\n" +
        "\tJava VM Version: Java HotSpot(TM) 64-Bit Server VM (mixed mode), Oracle Corporation\n" +
        "\tMemory: 1012922624 bytes (965 MB) / 1845493760 bytes (1760 MB) up to 2147483648 bytes (2048 MB)\n" +
        "\tFabric Mods: \n" +
        "....\n" +
        "\tIs Modded: Definitely; Client brand changed to 'fabric'\n"

const val JAVA_CRASH = "#\n" +
        "# A fatal error has been detected by the Java Runtime Environment:\n" +
        "#\n" +
        "#  EXCEPTION_ACCESS_VIOLATION (0xc0000005) at pc=0x000000000c1c1c82, pid=2768, tid=2780\n" +
        "#\n" +
        "# JRE version: 7.0_25-b17\n" +
        "# Java VM: Java HotSpot(TM) 64-Bit Server VM (23.25-b01 mixed mode windows-amd64 compressed oops)\n" +
        "# Problematic frame:\n" +
        "# C  [ig75icd64.dll+0x1c82]\n" +
        "#\n" +
        "# Failed to write core dump. Minidumps are not enabled by default on client versions of Windows\n" +
        "#\n" +
        "# If you would like to submit a bug report, please visit:\n" +
        "#   http://bugreport.sun.com/bugreport/crash.jsp\n" +
        "# The crash happened outside the Java Virtual Machine in native code.\n" +
        "# See problematic frame for where to report the bug.\n" +
        "#"

class CrashModuleTest : StringSpec({
    "should return OperationNotNeededModuleResponse when issue does not contain any valid crash report" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), "Help\nmy\ngame\nis\nsuper\nbroken!!\n!!!", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when issue body does not contain any recent crash" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )

        val calendar = Calendar.getInstance()
        calendar.add(DAY_OF_YEAR, -42)
        val time = calendar.time

        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when crash configurations are empty and report is not modded" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )

        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when crash configuration has an invalid type and crash is not modded" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("hytale", "The game has not yet been released", "HT-1")),
            10
        )

        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when reported crash is not configured and not modded" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Unexpected loophole in Redstone implementation", "MC-108")),
            10
        )

        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when issue does not contain any recent crash as attachment" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )

        val calendar = Calendar.getInstance()
        calendar.add(DAY_OF_YEAR, -42)
        val time = calendar.time
        val attachment = mockAttachment("crash.txt", time, EXAMPLE_CRASH.toByteArray())

        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)
        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when attached crash is not configured and not modded" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Unexpected loophole in Redstone implementation", "MC-108")),
            10
        )

        val attachment = mockAttachment("crash.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when attached crash does have a wrong mime type" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )

        val attachment = mockAttachment("crash.png", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should return OperationNotNeededModuleResponse when attached crash is not modded (Unknown)" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(),
            10
        )

        val attachment = mockAttachment("crash.txt", Calendar.getInstance().time, SERVER_UNMODDED_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft(OperationNotNeededModuleResponse)
    }

    "should resolve as invalid when reported server crash is modded" {
        var resolvedAsInvalid = false

        val module = CrashModule(
            { resolvedAsInvalid = true; Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), SERVER_MODDED_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsInvalid.shouldBeTrue()
    }

    "should resolve as invalid when reported server crash is very likely modded" {
        var resolvedAsInvalid = false

        val module = CrashModule(
            { resolvedAsInvalid = true; Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), SERVER_MODDED_CRASH_2, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsInvalid.shouldBeTrue()
    }

    "should resolve as invalid when reported crash is modded" {
        var resolvedAsInvalid = false

        val module = CrashModule(
            { resolvedAsInvalid = true; Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), MODDED_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsInvalid.shouldBeTrue()
    }

    "should resolve as invalid when attached crash is modded" {
        var resolvedAsInvalid = false

        val module = CrashModule(
            { resolvedAsInvalid = true; Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )

        val attachment = mockAttachment("crash.txt", Calendar.getInstance().time, MODDED_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsInvalid.shouldBeTrue()
    }

    "should resolve as dupe when reported crash is in configured list" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should resolve as dupe when attached crash is in configured list" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val attachment = mockAttachment("crash.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(attachment), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should resolve as dupe when the configured crash is a java crash" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("java", "ig75icd64\\.dll", "MC-32606")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), JAVA_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should resolve as dupe when the configured crash uses regex" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("java", "ig[0-9]{1,2}icd[0-9]{2}\\.dll", "MC-32606")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), JAVA_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should link to configured ticket when resolving as dupe" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, it -> it.shouldBe("MC-297").right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
    }

    "should prefer crash that is not modded, if modded crash appears first" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val modded = mockAttachment("crash_modded.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val dupe = mockAttachment("crash_dupe.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(modded, dupe), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should prefer crash that is not modded, if duped crash appears first" {
        var resolvedAsDupe = false

        val module = CrashModule(
            { Unit.right() },
            { resolvedAsDupe = true; Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val modded = mockAttachment("crash_modded.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val dupe = mockAttachment("crash_dupe.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(dupe, modded), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
        resolvedAsDupe.shouldBeTrue()
    }

    "should prefer more recent crash, if less recent crash appears first " {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, it -> it.shouldBe("MC-297").right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(
                CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297"),
                CrashDupeConfig("minecraft", "WGL: The driver does not appear to support OpenGL", "MC-128302")
            ),
            10
        )
        val calendar = Calendar.getInstance()
        calendar.add(DAY_OF_YEAR, -1)
        val yesterday = calendar.time

        val fromNow = mockAttachment("recent.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val fromYesterday = mockAttachment("crash_dupe.txt", yesterday, EXAMPLE_CRASH_2.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(fromYesterday, fromNow), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
    }

    "should prefer more recent crash, if more recent crash appears first " {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, it -> it.shouldBe("MC-297").right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(
                CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297"),
                CrashDupeConfig("minecraft", "WGL: The driver does not appear to support OpenGL", "MC-128302")
            ),
            10
        )
        val calendar = Calendar.getInstance()
        calendar.add(DAY_OF_YEAR, -1)
        val yesterday = calendar.time

        val fromNow = mockAttachment("recent.txt", Calendar.getInstance().time, EXAMPLE_CRASH.toByteArray())
        val fromYesterday = mockAttachment("crash_dupe.txt", yesterday, EXAMPLE_CRASH_2.toByteArray())
        val request = CrashModuleRequest(ISSUE, listOf(fromNow, fromYesterday), "", Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeRight(ModuleResponse)
    }

    "should return FailedModuleResponse when resolving as invalid fails" {
        val module = CrashModule(
            { RuntimeException().left() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), MODDED_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }

    "should return FailedModuleResponse when sending as modded message fails" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { RuntimeException().left() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            emptyList(),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), MODDED_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }

    "should return FailedModuleResponse when resolving as duplicate fails" {
        val module = CrashModule(
            { Unit.right() },
            { RuntimeException().left() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }

    "should return FailedModuleResponse when linking a duplicate fails" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> RuntimeException().left() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }

    "should return FailedModuleResponse when sending duplicate message fails" {
        val module = CrashModule(
            { Unit.right() },
            { Unit.right() },
            { _, _ -> Unit.right() },
            { Unit.right() },
            { _, _ -> RuntimeException().left() },
            listOf("txt"),
            listOf(CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297")),
            10
        )
        val request = CrashModuleRequest(ISSUE, emptyList(), EXAMPLE_CRASH, Calendar.getInstance().time)

        val result = module(request)

        result.shouldBeLeft()
        result.a should { it is FailedModuleResponse }
        (result.a as FailedModuleResponse).exceptions.size shouldBe 1
    }
})

private fun mockAttachment(name: String, created: Date, content: ByteArray): Attachment {
    val attachment = mockk<Attachment>()
    every { attachment.fileName } returns name
    every { attachment.createdDate } returns created
    every { attachment.download() } returns content
    return attachment
}
