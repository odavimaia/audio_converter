package br.com.davimaia.audiosuite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.nio.charset.StandardCharsets

class PythonRepository {

    private fun findScriptsDir(): Pair<File, String> {
        val logBuilder = StringBuilder()
        val composeResourcesPath = System.getProperty("compose.application.resources.dir")

        if (composeResourcesPath != null) {
            val dir = File(composeResourcesPath)
            if (File(dir, "downloader.py").exists()) {
                return Pair(dir, "Encontrado em app/resources")
            }
            val subDir = File(dir, "python_core")
            if (subDir.exists() && File(subDir, "downloader.py").exists()) {
                return Pair(subDir, "Encontrado em app/resources/python_core")
            }
        }

        val userDir = System.getProperty("user.dir")
        val devCandidates = listOf(
            File(userDir, "python_core"),
            File(userDir, "../python_core")
        )

        for (dir in devCandidates) {
            if (dir.exists() && File(dir, "downloader.py").exists()) {
                return Pair(dir, "Encontrado no ambiente Dev")
            }
        }

        return Pair(File(userDir), "Pasta não encontrada")
    }

    fun downloadAudio(url: String): Flow<String> = runScript("downloader.py", url)

    fun convertAudio(): Flow<String> = runScript("universal_converter.py")

    fun convertCustomPath(path: String): Flow<String> = runScript("universal_converter.py", path)

    private fun runScript(scriptName: String, vararg args: String): Flow<String> = flow {
        val (targetDir, searchLog) = findScriptsDir()

        if (!targetDir.exists() || !File(targetDir, scriptName).exists()) {
            emit("❌ ERRO FATAL: O arquivo '$scriptName' não foi encontrado.")
            return@flow
        }

        val embeddedPython = File(targetDir, "python.exe")
        val pythonCommand = if (embeddedPython.exists()) embeddedPython.absolutePath else "python"

        val command = mutableListOf(pythonCommand, "-u", scriptName)
        command.addAll(args)

        val builder = ProcessBuilder(command)
        builder.directory(targetDir)
        builder.redirectErrorStream(true)
        builder.environment()["PYTHONIOENCODING"] = "utf-8"

        try {
            val process = builder.start()
            process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    emit(line)
                    line = reader.readLine()
                }
            }
            process.waitFor()
        } catch (e: Exception) {
            emit("❌ Erro de Execução: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}