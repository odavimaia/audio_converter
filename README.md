# 🎵 Universal Car Audio Converter

Um utilitário Python robusto desenvolvido para resolver problemas de compatibilidade em sistemas de som automotivo (Head Units OEM/Legacy) que não suportam codecs modernos ou metadados complexos.

O script converte, normaliza e sanitiza bibliotecas de música automaticamente, garantindo que seus arquivos toquem em qualquer lugar.

## 🚀 Funcionalidades

- **🔄 Conversão Universal:** Suporte para `m4a`, `ogg`, `flac`, `wav`, `wma`, `aac`, `opus`.
- **🛠️ Auto-Configuração (Novo):** Verifica se o `FFmpeg` está instalado. Se não estiver, oferece instalação automática via `winget` (Windows) ou `brew` (macOS).
- **🎚️ Padronização de Áudio:**
  - Saída: MP3 320kbps (CBR) @ 44.1kHz.
  - **Stereo Force:** Faz downmix automático de 5.1/7.1 para 2 canais (evita erros de leitura em players simples).
- **🧹 Sanitização de Arquivos:** Remove acentos, emojis e caracteres especiais dos nomes para garantir compatibilidade com displays LCD antigos (FAT32 safe).
- **🗑️ Limpeza Inteligente:** Exclui o arquivo original *apenas* após verificar a integridade do novo arquivo MP3 gerado.

## 📋 Pré-requisitos

- **Python 3.8+** instalado no sistema.

> **Nota:** O script depende do **FFmpeg**. O script tentará instalá-lo automaticamente se não encontrar. Caso a automação falhe, instale manualmente:

| SO | Comando Manual |
|----|----------------|
| **Windows** | `winget install Gyan.FFmpeg` |
| **macOS** | `brew install ffmpeg` |
| **Linux** | `sudo apt install ffmpeg` |

## 🔧 Como Usar

1. **Posicione o Script:**
   Coloque o arquivo `converter_universal.py` na pasta raiz onde estão suas músicas (ele varre subpastas recursivamente).

2. **Execute:**
   Abra o terminal na pasta e rode:
   ```bash
   python converter_universal.py
3. **Interação:**
   O script verificará o ambiente.
   
   Se faltar o FFmpeg, ele perguntará: Deseja tentar instalar o FFmpeg agora? (S/N).
   
   Após a confirmação, ele processará todos os arquivos de áudio da pasta.


## 🛡️ Segurança e Logs
O script possui travas de segurança: arquivos gerados com menos de 10KB são considerados falhas e o original não é deletado.

Logs de erro são exibidos no console caso algum arquivo esteja corrompido.

## ⚠️ Isenção de Responsabilidade
Este software realiza operações de exclusão de arquivos (substituição destrutiva). Embora possua verificações de integridade, recomenda-se ter um backup dos arquivos originais antes de executar em grandes bibliotecas.   

- **🔊 Normalização EBU R128:** Aplica normalização inteligente de loudness (-14 LUFS). Isso garante que todas as músicas tenham o mesmo volume percebido, eliminando a necessidade de ficar ajustando o botão de volume do carro entre as faixas.