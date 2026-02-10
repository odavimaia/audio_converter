import os
import subprocess
import shutil
import sys
import unicodedata

# === CONFIGURAÇÕES DO PROJETO ===
# Formato final universal para som automotivo
TARGET_EXT = ".mp3"

# Lista expandida de formatos de origem para converter
# Adicione ou remova conforme necessário
SOURCE_EXTS = (
    '.m4a', '.m4b', '.m4p',  # Família MPEG-4 / AAC
    '.ogg', '.opus',         # Família Vorbis / Opus
    '.flac',                 # Lossless livre
    '.wav', '.aiff',         # Lossless não comprimido
    '.wma',                  # Windows Media Audio
    '.aac',                  # Raw AAC
    '.alac'                  # Apple Lossless
)

# Configuração de Qualidade MP3
BITRATE = "320k"       # Qualidade máxima (CBR)
SAMPLE_RATE = "44100"  # Hz (Padrão CD/Red Book)

def sanitize_filename(filename):
    """
    Limpa o nome do arquivo para garantir compatibilidade com sistemas
    de arquivos antigos (FAT32) e displays de carros simples.
    Remove acentos, espaços e caracteres especiais.
    """
    # Normaliza unicode (ex: 'ç' vira 'c')
    nfkd_form = unicodedata.normalize('NFKD', filename)
    filename = "".join([c for c in nfkd_form if not unicodedata.combining(c)])
    
    # Substitui espaços e remove caracteres problemáticos
    safe_name = filename.replace(" ", "_")
    
    # Remove qualquer coisa que não seja alfanumérico, sublinhado ou hífen
    # Isso é agressivo, mas garante que o som do carro leia.
    return "".join([c for c in safe_name if c.isalnum() or c in ('_', '-')])

def process_file(root, file):
    input_path = os.path.join(root, file)
    
    # Extrai nome base e extensão original
    base_name, ext = os.path.splitext(file)
    
    # Sanitiza o nome para o arquivo de saída
    safe_base_name = sanitize_filename(base_name)
    output_filename = safe_base_name + TARGET_EXT
    output_path = os.path.join(root, output_filename)

    # Verifica se é o mesmo arquivo (ex: mp3 -> mp3) para evitar loop
    if input_path == output_path:
        return

    print(f"🔄 Processando: {file}")

    try:
        # Comando FFmpeg Otimizado
        command = [
            'ffmpeg',
            '-i', input_path,        # Entrada
            '-vn',                   # Remove vídeo/capa (evita crash em som antigo)
            '-ar', SAMPLE_RATE,      # Força 44.1kHz
            '-ac', '2',              # Força Stereo (alguns sons não tocam 5.1 ou Mono)
            '-b:a', BITRATE,         # Bitrate Constante (CBR)
            '-map_metadata', '0',    # Tenta preservar tags
            '-id3v2_version', '3',   # ID3v2.3 (mais compatível que v2.4)
            '-y',                    # Sobrescrever sem perguntar
            '-hide_banner',          # Menos lixo no log
            '-loglevel', 'error',    # Só mostra erros reais
            output_path
        ]
        
        subprocess.run(command, check=True)

        # === VALIDAÇÃO DE INTEGRIDADE ===
        # Só deleta o original se o MP3 novo existir e tiver tamanho válido (>10KB)
        if os.path.exists(output_path) and os.path.getsize(output_path) > 10000:
            os.remove(input_path)
            print(f"   ✅ Convertido e original removido.")
        else:
            print(f"   ⚠️  ERRO: Arquivo MP3 parece corrompido ou vazio. Original mantido.")

    except subprocess.CalledProcessError as e:
        print(f"   ❌ Falha na conversão: {e}")
        # Tenta limpar o arquivo mp3 corrompido se ele foi criado
        if os.path.exists(output_path):
            os.remove(output_path)

def main():
    current_dir = os.getcwd()
    
    # Checagem de dependência
    if not shutil.which("ffmpeg"):
        print("❌ ERRO CRÍTICO: FFmpeg não encontrado.")
        print("Instale o FFmpeg e adicione ao PATH do sistema.")
        sys.exit(1)

    print(f"📂 Iniciando Varredura Universal em: {current_dir}")
    print(f"🎯 Alvo: Converter {SOURCE_EXTS} para MP3 ({BITRATE})")
    print("---------------------------------------------------")

    count = 0
    # Walk recursivo (entra em subpastas)
    for root, dirs, files in os.walk(current_dir):
        for file in files:
            if file.lower().endswith(SOURCE_EXTS):
                process_file(root, file)
                count += 1

    print("---------------------------------------------------")
    if count == 0:
        print("Nenhum arquivo de áudio compatível encontrado para conversão.")
    else:
        print(f"🏁 Operação finalizada. {count} arquivos processados.")

if __name__ == "__main__":
    main()