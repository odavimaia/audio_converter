package br.com.davimaia.audiosuite

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.swing.JFileChooser

class AppViewModel {
    private val repository = PythonRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    // Inputs FASE 1
    var urlInput by mutableStateOf("")
    var isRunning by mutableStateOf(false)
    var downloadProgress by mutableStateOf(0f)
    var conversionProgress by mutableStateOf(0f)
    var downloadStatus by mutableStateOf("Aguardando...")
    var conversionStatus by mutableStateOf("Aguardando...")
    var errorMessage by mutableStateOf("")

    // Inputs FASE MANUAL
    var manualPath by mutableStateOf("Nenhum arquivo selecionado")
    var isManualProcessing by mutableStateOf(false)
    var manualStatus by mutableStateOf("Aguardando seleção...")

    // Regex para capturar [índice/total] e a porcentagem
    private val downloadPattern = Pattern.compile("\\[download\\] \\[(\\d+)/(\\d+)\\]\\s+(\\d{1,3}(?:\\.\\d+)?)%")
    private val conversionPattern = Pattern.compile("\\[(\\d+)/(\\d+)\\]")

    fun startProcess() {
        if (urlInput.isBlank()) return

        isRunning = true
        downloadProgress = 0f
        conversionProgress = 0f
        downloadStatus = "Iniciando..."
        conversionStatus = "Aguardando download..."
        errorMessage = ""

        val fullLogBuffer = StringBuilder()

        scope.launch {
            try {
                // FASE 1: DOWNLOAD
                downloadStatus = "Baixando metadata..."
                var scriptRodouComSucesso = false

                repository.downloadAudio(urlInput).collect { line ->
                    fullLogBuffer.append(line).append("\n")

                    if (line.contains("%") || line.contains("Downloads finalizados")) {
                        scriptRodouComSucesso = true
                    }
                    if (line.contains("ERRO FATAL") || line.contains("Traceback")) {
                        downloadStatus = "❌ Erro Crítico"
                    }
                    parseDownloadLine(line)
                }

                if (scriptRodouComSucesso) {
                    downloadProgress = 1f
                    downloadStatus = "Download Concluído ✅"
                } else {
                    downloadStatus = "⚠️ Falhou"
                    errorMessage = "Erro no Download:\n\n$fullLogBuffer"
                    return@launch
                }

                // FASE 2: CONVERSÃO AUTOMÁTICA
                conversionStatus = "Normalizando arquivos..."
                repository.convertAudio().collect { line ->
                    fullLogBuffer.append(line).append("\n")
                    parseConversionLine(line)
                }
                conversionProgress = 1f
                conversionStatus = "Concluído ✅"
                urlInput = ""

            } catch (e: Exception) {
                downloadStatus = "Erro Interno"
                errorMessage = "Erro Java:\n${e.message}"
            } finally {
                isRunning = false
            }
        }
    }

    private fun parseDownloadLine(line: String) {
        if (line.contains("[download]")) {
            val matcher = downloadPattern.matcher(line)
            if (matcher.find()) {
                val currentIdx = matcher.group(1).toFloatOrNull() ?: 1f
                val totalEntries = matcher.group(2).toFloatOrNull() ?: 1f
                val percentCurrent = matcher.group(3).toFloatOrNull() ?: 0f

                // 1. PROGRESSO TOTAL DO DOWNLOAD (Geral da Playlist)
                downloadProgress = ((currentIdx - 1) + (percentCurrent / 100f)) / totalEntries
                downloadStatus = "Baixando ${currentIdx.toInt()}/${totalEntries.toInt()}: ${percentCurrent}%"

                // 2. PROGRESSO DA CONVERSÃO (Preenchimento prévio)
                // Faz a barra de baixo andar conforme arquivos terminam de baixar
                if (conversionProgress < 1f) {
                    conversionProgress = (currentIdx - 1) / totalEntries
                    conversionStatus = "Aguardando (${currentIdx.toInt() - 1}/${totalEntries.toInt()} prontos)"
                }
            }
        }
    }

    private fun parseConversionLine(line: String) {
        val matcher = conversionPattern.matcher(line)
        if (matcher.find()) {
            val current = matcher.group(1).toFloat()
            val total = matcher.group(2).toFloat()
            if (total > 0) {
                conversionProgress = current / total
                conversionStatus = "Processando ${current.toInt()}/${total.toInt()}"
            }
        }
    }

    fun selectFileOrDirectory() {
        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            manualPath = fileChooser.selectedFile.absolutePath
            manualStatus = "Pronto para converter"
        }
    }

    fun startManualConversion() {
        if (manualPath == "Nenhum arquivo selecionado") return
        isManualProcessing = true
        manualStatus = "Iniciando conversão..."
        scope.launch {
            try {
                repository.convertCustomPath(manualPath).collect { line ->
                    val matcher = conversionPattern.matcher(line)
                    if (matcher.find()) {
                        manualStatus = "Processando ${matcher.group(1)}/${matcher.group(2)}"
                    }
                }
                manualStatus = "Conversão Manual Concluída ✅"
            } catch (e: Exception) {
                manualStatus = "Erro Crítico"
            } finally {
                isManualProcessing = false
            }
        }
    }
}