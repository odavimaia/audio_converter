package br.com.davimaia.audiosuite

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

val DarkBg = Color(0xFF1E1E1E)
val CardBg = Color(0xFF2D2D2D)
val AccentColor = Color(0xFFBB86FC)
val SuccessColor = Color(0xFF4CAF50)
val ErrorBg = Color(0xFF2B1212)
val ErrorBorder = Color(0xFFB00020)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Audio Suite Pro",
        state = rememberWindowState(width = 900.dp, height = 800.dp),
        resizable = true,
    ) {
        MaterialTheme(colors = darkColors(background = DarkBg, surface = CardBg, primary = AccentColor)) {
            App()
        }
    }
}

@Composable
fun App() {
    val viewModel = remember { AppViewModel() }
    val animatedDownload by animateFloatAsState(targetValue = viewModel.downloadProgress)
    val animatedConversion by animateFloatAsState(targetValue = viewModel.conversionProgress)
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Icon(Icons.Default.MusicNote, null, tint = AccentColor, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Audio Suite Pro", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Downloader & Conversor Automático", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(40.dp))

                // Input de Download
                OutlinedTextField(
                    value = viewModel.urlInput,
                    onValueChange = { viewModel.urlInput = it },
                    label = { Text("URL do YouTube ou Spotify") },
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

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(40.dp))
                Divider(color = Color.DarkGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(40.dp))

                // === SEÇÃO DE FERRAMENTAS MANUAIS ===
                Text("Ferramentas Manuais", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    backgroundColor = CardBg,
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Conversor & Normalizador Local", fontWeight = FontWeight.Bold, color = AccentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Selecione um arquivo de áudio ou uma pasta inteira para padronizar o volume e converter.",
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

                // === ÁREA DE ERRO ===
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