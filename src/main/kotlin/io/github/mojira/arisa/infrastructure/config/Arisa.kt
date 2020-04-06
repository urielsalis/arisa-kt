package io.github.mojira.arisa.infrastructure.config

import com.uchuhimo.konf.ConfigSpec

object Arisa : ConfigSpec() {
    val shadow by optional(false)

    object Credentials : ConfigSpec() {
        val username by required<String>()
        val password by required<String>()
    }

    object Issues : ConfigSpec() {
        val projects by optional(listOf("MC", "MCTEST", "MCPE", "MCAPI", "MCL", "MCD", "MCE", "BDS"))
        val url by optional("https://bugs.mojang.com/")
        val checkInterval by optional(10L)
    }

    object CustomFields : ConfigSpec() {
        val chkField by optional("customfield_10701")
        val confirmationField by optional("customfield_10500")
        val mojangPriorityField by optional("customfield_12200")
        val triagedTimeField by optional("customfield_12201")
    }

    object Modules : ConfigSpec() {
        open class ModuleConfigSpec : ConfigSpec() {
            val whitelist by optional(listOf("MC", "MCTEST", "MCPE", "MCAPI", "MCL", "MCD", "MCE", "BDS"))
        }

        object Attachment : ModuleConfigSpec() {
            val extensionBlacklist by optional(listOf("jar", "exe", "com", "bat", "msi", "run", "lnk", "dmg"))
        }

        object Piracy : ModuleConfigSpec() {
            val piracyMessage by optional(
                "You are currently using a *pirated* version of Minecraft." +
                        " If you wish to purchase the full game, please visit the [Minecraft Store|https://minecraft.net/store].\r\n" +
                        "We will not provide support for pirated versions of the game, these versions are modified and may contain malware."
            )
            val piracySignatures by optional(
                listOf(
                    "Minecraft Launcher null",
                    "Bootstrap 0",
                    "Launcher: 1.0.10  (bootstrap 4)",
                    "Launcher: 1.0.10  (bootstrap 5)",
                    "Launcher 3.0.0",
                    "Launcher: 3.1.0",
                    "Launcher: 3.1.1",
                    "Launcher: 3.1.4",
                    "1.0.8",
                    "uuid sessionId",
                    "auth_access_token",
                    "windows-\${arch}",
                    "keicraft",
                    "keinett",
                    "nodus",
                    "iridium",
                    "mcdonalds",
                    "uranium",
                    "divinity",
                    "gemini",
                    "mineshafter",
                    "Team-NeO",
                    "DarkLBP",
                    "Launcher X",
                    "PHVL",
                    "Pre-Launcher v6",
                    "LauncherFEnix",
                    "TLauncher"
                    "Titan"
                )
            )
        }

        object RemoveTriagedMeqs : ModuleConfigSpec() {
            val meqsTags by optional(listOf("MEQS_WAI", "MEQS_WONTFIX"))
        }

        object FutureVersion : ModuleConfigSpec() {
            val futureVersionMessage by optional(
                "Please *do not* mark _unreleased versions_ as affected.\r\nYou don't have access to them yet."
            )
        }

        object CHK : ModuleConfigSpec()

        object ReopenAwaiting : ModuleConfigSpec()

        object RemoveNonStaffMeqs : ModuleConfigSpec()

        object Empty : ModuleConfigSpec() {
            val emptyMessage by Crash.optional(
                "We are unable to diagnose your issue due to the lack of proper debug information. " +
                        "Please review the [guidelines|https://help.minecraft.net/hc/en-us/articles/360039268071] before reporting issues.\r\n" +
                        "In case of a game crash, please also attach the crash log from " +
                        "{{[minecraft/crash-reports/crash-<DATE>-client.txt|https://minecrafthopper.net/help/guides/finding-minecraft-data-folder/]}}."
            )
        }

        object Crash : ModuleConfigSpec() {
            val maxAttachmentAge by optional(30)
            val crashExtensions by optional(listOf("txt", "log"))
            val duplicateMessage by optional(
                "Thank you for reporting this issue, however your report is a duplicate of {DUPLICATE} -- " +
                        "If you have not, please use the [search function|https://bugs.mojang.com/issues/] in the future, " +
                        "to see if your bug has already been submitted.\r\n" +
                        "For technical support, please use the " +
                        "[Minecraft Support Discord|https://discord.gg/58Sxm23]."
            )
            val moddedMessage by optional(
                "*Thank you for your report!*\nHowever, this issue is {color:#FF5722}*Invalid*{color}.\n\nYour game, launcher or server is modified.\nIf you can reproduce the issue in a vanilla environment, please recreate the issue.\n\n* Any non-standard client/server/launcher build needs to be taken up with the appropriate team, not Mojang.\n* Any plugin issues need to be addressed to the creator of the plugin or resource pack.\n* If you have problems on large servers, such as The Hive and Hypixel, please contact them first as they run modified server software.\n\n\n%quick_links%"
                {color:#bbb}-- "I am a bot. This action was performed automagically! Please report any issues in  [our discord](https://discordapp.com/invite/rpCyfKV) or https://www.reddit.com/r/Mojira/{color}
                )

            val duplicates by optional(
                listOf(
                    CrashDupeConfig("minecraft", "Pixel format not accelerated", "MC-297"),
                    CrashDupeConfig("minecraft", "No OpenGL context found in the current thread", "MC-297"),
                    CrashDupeConfig("minecraft", "Could not create context", "MC-297"),
                    CrashDupeConfig("minecraft", "WGL: The driver does not appear to support OpenGL", "MC-128302"),
                    CrashDupeConfig("minecraft", "failed to create a child event loop", "MC-34749"),
                    CrashDupeConfig("minecraft", "Failed to check session lock, aborting", "MC-10167"),
                    CrashDupeConfig("minecraft", "Maybe try a lowerresolution texturepack", "MC-29565"),
                    CrashDupeConfig("minecraft", "java\\.lang\\.OutOfMemoryError\\: Java heap space", "MC-12949"),
                    CrashDupeConfig("minecraft", "try a lowerresolution", "MC-29565"),
                    CrashDupeConfig("java", "ig[0-9]{1,2}icd[0-9]{2}\\.dll", "MC-32606")
                )
            )
        }
    }
}

data class CrashDupeConfig(
    val type: String,
    val exceptionDesc: String,
    val duplicates: String
)
