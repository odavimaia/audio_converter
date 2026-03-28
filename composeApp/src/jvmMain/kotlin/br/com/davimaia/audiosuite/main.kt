package br.com.davimaia.audiosuite

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.io.File
import java.security.MessageDigest

// Cores do Tema
val DarkBg = Color(0xFF1E1E1E)
val CardBg = Color(0xFF2D2D2D)
val AccentColor = Color(0xFFBB86FC)
val SuccessColor = Color(0xFF4CAF50)
val ErrorBg = Color(0xFF2B1212)
val ErrorBorder = Color(0xFFB00020)

// === SISTEMA DE LICENÇA ===
val licenseFile = File(System.getProperty("user.home"), "Music/AudioSuitePro/.license")

// 1. O SEU "SAL" SECRETO. Nunca mude isso depois de vender a primeira cópia!
// Se alguém tentar inventar uma chave, a matemática vai falhar porque não têm essa frase.
const val SECRET_SALT = "DaviMaia_AudioSuite_2026_SuperSecret_x99"

// 2. O Motor Matemático: Transforma qualquer texto em um código irreversível (SHA-256)
fun generateHash(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest((input + SECRET_SALT).toByteArray())
    // Pega os primeiros 12 caracteres hexadecimais em maiúsculo para ficar uma chave bonita
    return bytes.joinToString("") { "%02x".format(it) }.take(12).uppercase()
}

// 3. O Validador da Tela de Bloqueio
fun isValidKey(licenseInput: String): Boolean {
    // A chave DEVE ter o formato: "email@cliente.com-CODIGO"
    if (!licenseInput.contains("-")) return false

    val parts = licenseInput.split("-", limit = 2)
    val email = parts[0].trim()
    val providedHash = parts[1].trim()

    // O app tenta recriar o código usando o e-mail do cara. Se bater, é autêntico!
    val expectedHash = generateHash(email)
    return providedHash == expectedHash
}

// 4. Checa se o arquivo salvo no PC é válido
fun checkLicense(): Boolean {
    return if (licenseFile.exists()) {
        isValidKey(licenseFile.readText().trim())
    } else {
        false
    }
}

fun saveLicense(key: String) {
    licenseFile.parentFile.mkdirs()
    licenseFile.writeText(key.trim())
}
// ==========================

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Audio Suite Pro",
        state = rememberWindowState(
            width = 900.dp,
            height = 800.dp,
            position = WindowPosition(Alignment.Center)
        ),
        resizable = true,
    ) {
        MaterialTheme(colors = darkColors(background = DarkBg, surface = CardBg, primary = AccentColor)) {
            App()
        }
    }
}

@Composable
fun App() {
    var isUnlocked by remember { mutableStateOf(checkLicense()) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        if (isUnlocked) {
            MainContent()
        } else {
            LockScreen(onUnlock = { isUnlocked = true })
        }
    }
}

@Composable
fun LockScreen(onUnlock: () -> Unit) {
    var inputKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            backgroundColor = CardBg,
            elevation = 8.dp,
            modifier = Modifier.width(400.dp).padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentColor, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Audio Suite Pro", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Ativação de Licença", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it
                        showError = false
                    },
                    label = { Text("Insira sua Chave VIP") },
                    isError = showError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = AccentColor,
                        cursorColor = AccentColor,
                        textColor = Color.White
                    )
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Chave inválida. Verifique os caracteres.", color = ErrorBorder, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // MUDANÇA AQUI: Usa a nova validação matemática
                        if (isValidKey(inputKey)) {
                            saveLicense(inputKey)
                            onUnlock()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor)
                ) {
                    Text("ATIVAR SOFTWARE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val viewModel = remember { AppViewModel() }

    // Controle das Abas
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Downloader Integrado", "Conversor Local")

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Fixo no Topo
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.MusicNote, null, tint = AccentColor, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Audio Suite Pro", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Navegação das Abas
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = CardBg,
            contentColor = AccentColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, color = if (selectedTab == index) AccentColor else Color.Gray) }
                )
            }
        }

        // Conteúdo Dinâmico com Scroll independente
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> DownloaderTab(viewModel)
                    1 -> ConversorTab(viewModel)
                }

                // Área de Erro Global (aparece independente da aba se houver erro)
                if (viewModel.errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(backgroundColor = ErrorBg, border = BorderStroke(1.dp, ErrorBorder), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("❌ DIAGNÓSTICO DE ERRO:", color = ErrorBorder, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            SelectionContainer {
                                Text(text = viewModel.errorMessage, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DownloaderTab(viewModel: AppViewModel) {
    val animatedDownload by animateFloatAsState(targetValue = viewModel.downloadProgress)
    val animatedConversion by animateFloatAsState(targetValue = viewModel.conversionProgress)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Baixar e Processar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Cole o link do YouTube para baixar e normalizar automaticamente.", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.urlInput,
            onValueChange = { viewModel.urlInput = it },
            label = { Text("URL do YouTube") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !viewModel.isRunning,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = AccentColor,
                cursorColor = AccentColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProgressSection("1. Download", viewModel.downloadStatus, animatedDownload)
        Spacer(modifier = Modifier.height(24.dp))
        ProgressSection("2. Conversão & Normalização", viewModel.conversionStatus, animatedConversion)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startProcess() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !viewModel.isRunning && viewModel.urlInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(backgroundColor = AccentColor)
        ) {
            Icon(Icons.Default.Download, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (viewModel.isRunning) "PROCESSANDO..." else "INICIAR DOWNLOAD")
        }
    }
}

@Composable
fun ConversorTab(viewModel: AppViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Ferramentas Manuais", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Trate arquivos de áudio que já estão no seu computador.", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            backgroundColor = CardBg,
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Conversor & Normalizador", fontWeight = FontWeight.Bold, color = AccentColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Selecione um arquivo de áudio ou uma pasta inteira para padronizar o volume e converter para MP3.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.selectFileOrDirectory() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray),
                        modifier = Modifier.weight(0.3f)
                    ) {
                        Text("📂 Escolher", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.manualPath,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.7f).background(Color.Black.copy(alpha=0.3f)).padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(viewModel.manualStatus, fontSize = 12.sp, color = if(viewModel.manualStatus.contains("✅")) SuccessColor else AccentColor)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.startManualConversion() },
                    enabled = !viewModel.isManualProcessing && viewModel.manualPath != "Nenhum arquivo selecionado",
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = SuccessColor)
                ) {
                    Text(if (viewModel.isManualProcessing) "PROCESSANDO..." else "CONVERTER SELEÇÃO")
                }
            }
        }
    }
}

@Composable
fun ProgressSection(title: String, status: String, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(status, fontSize = 12.sp, color = if (status.contains("✅")) SuccessColor else Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.Black, shape = RoundedCornerShape(4.dp)),
            color = if (progress >= 1f) SuccessColor else AccentColor,
            backgroundColor = Color.Black
        )
    }
}