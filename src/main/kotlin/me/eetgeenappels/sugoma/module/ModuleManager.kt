package me.eetgeenappels.sugoma.module

import me.eetgeenappels.sugoma.module.modules.combat.AutoCrystal
import me.eetgeenappels.sugoma.module.modules.combat.AutoEZ
import me.eetgeenappels.sugoma.module.modules.combat.AutoTotem
import me.eetgeenappels.sugoma.module.modules.combat.KillAura
import me.eetgeenappels.sugoma.module.modules.player.NoFall
import me.eetgeenappels.sugoma.module.modules.player.Sprint
import me.eetgeenappels.sugoma.module.modules.render.ClickGuiModule
import me.eetgeenappels.sugoma.module.modules.render.Fulbright
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.module.modules.world.Scaffold
import java.io.*
import java.util.stream.Collectors
import kotlin.Exception
import kotlin.RuntimeException
import kotlin.String

class ModuleManager {
    var modules: MutableList<Module> = ArrayList()

    init {

        // render
        modules.add(ClickGuiModule())
        modules.add(Fulbright())
        // combat
        modules.add(KillAura())
        modules.add(AutoTotem())
        modules.add(AutoCrystal())
        modules.add(AutoEZ())
        // world
        modules.add(Scaffold())
        // player
        modules.add(Sprint())
        modules.add(NoFall())
        load()
    }

    fun getModule(name: String?): Module? {
        for (module in modules) if (module.name.equals(name, ignoreCase = true)) return module
        return null
    }

    val moduleList: List<Module>
        get() = modules

    fun getModulesByCategory(category: Category): List<Module> {
        val modules: MutableList<Module> = ArrayList()
        for (module in this.modules) {
            if (module.category == category) modules.add(module)
        }
        return modules
    }

    // serialize and save settings
    fun save() {
        // create folder "Sugoma"
        File("Sugoma").mkdir()
        // create file "Sugoma/config.txt"
        val settingsFile = File("Sugoma/config.txt")
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        // write settings to file
        try {
            val writer = BufferedWriter(FileWriter(settingsFile))
            writer.write(serialize())
            writer.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    // load settings from file
    private fun load() {
        // create file "Sugoma/config.txt"
        val settingsFile = File("Sugoma/config.txt")

        // check if the directory "Sugoma" exists
        if (!File("Sugoma").exists()) {
            // if not, create it
            File("Sugoma").mkdir()
        }
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile()
                save()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        // read settings from file and deserialize
        try {
            val reader = BufferedReader(FileReader(settingsFile))
            deserialize(reader.lines().collect(Collectors.joining("\n")))
            reader.close()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // serialize settings
    private fun serialize(): String {
        // format "MODULE_NAME SETTING_NAME VALUE"
        val builder = StringBuilder()
        for (module in modules) {
            // add if module is enabled
            builder.append(module.name).append(" ").append("Toggled").append(" ").append(module.toggled)
                .append("\n")
            builder.append(module.name).append(" ").append("Bind").append(" ").append(module.key).append("\n")
            for (setting in module.settings) {
                when (setting) {
                    is ToggleSetting -> builder.append(module.name).append(" ").append(setting.name)
                        .append(" ").append(
                            setting.value
                        ).append("\n")

                    is SliderSetting -> builder.append(module.name).append(" ")
                        .append(setting.name).append(" ").append(
                            setting.value
                        ).append("\n")

                    is ModeSetting -> {
                        builder.append(module.name).append(" ").append(setting.name).append(" ")
                            .append(setting.currentModeIndex).append("\n")
                    }
                }
            }
        }
        builder.append("Malware Toggled false")
        return builder.toString()
    }

    private fun deserialize(string: String) {

        // deserialization
        // parse text line by line
        val lines = string.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) {
            // spit line into module_name setting_name value
            val split = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (split[0].equals("Malware", ignoreCase = true)) continue

            // get module
            val module = getModule(split[0])

            // check if split[1] is "Toggled"
            if (split[1].equals("Toggled", ignoreCase = true)) {
                module!!.toggled = split[2].toBoolean()
                continue
            }

            // check if split[1] is "Bind"
            if (split[1].equals("Bind", ignoreCase = true)) {
                module!!.key = split[2].toInt()
                continue
            }

            // get setting
            val setting = module!!.getSetting(split[1])
            // set value
            if (setting is ToggleSetting) setting.value = split[2].toBoolean()
            if (setting is SliderSetting) setting.value = split[2].toDouble().toFloat()
            if (setting is ModeSetting) setting.currentModeIndex = split[2].toInt()
        }
    }
}
