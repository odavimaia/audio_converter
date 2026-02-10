# 🎵 Universal Audio Converter for Car Audio Systems

Este script Python foi desenvolvido para resolver problemas de compatibilidade em sistemas de som automotivo (Head Units) legados ou OEM que não suportam codecs modernos (AAC/M4A) ou sistemas de arquivos complexos.

## 🚀 Funcionalidades

- **Conversão Universal:** Aceita `m4a`, `ogg`, `flac`, `wav`, `wma`, `aac`, `opus`.
- **Saída Padronizada:** Gera MP3 320kbps (CBR) @ 44.1kHz.
- **Stereo Force:** Garante downmix para 2 canais (evita erro de leitura em arquivos 5.1).
- **Sanitização de Nomes:** Remove acentos, espaços e caracteres especiais (UTF-8 safe) para garantir leitura em displays antigos.
- **Limpeza Automática:** Remove o arquivo original somente após validar a integridade do MP3 gerado.

## 📋 Pré-requisitos

1. **Python 3.x** instalado.
2. **FFmpeg** instalado e configurado no PATH do sistema.

### Instalação do FFmpeg
- **Mac:** `brew install ffmpeg`
- **Linux:** `sudo apt install ffmpeg`
- **Windows:** `winget install ffmpeg`

## 🔧 Como Usar

1. Coloque o arquivo `universal_converter.py` na pasta raiz onde estão suas músicas (ele varre subpastas recursivamente).
2. Execute o script:
   ```bash
   python universal_converter.py
3. Aguarde o processamento. O script irá converter os arquivos e limpar os originais automaticamente.

## ⚠️ Isenção de Responsabilidade
Este script realiza operações de exclusão de arquivos. Embora possua travas de segurança (verificação de tamanho > 10KB), use com cautela e tenha backup dos seus arquivos originais.