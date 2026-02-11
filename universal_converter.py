import os
import sys
import shutil
import subprocess
import platform
import unicodedata
import time
import concurrent.futures

# === CONFIGURAÇÕES GLOBAIS ===
TARGET_EXT = ".mp3"
# Extensões que serão caçadas (incluindo MP3 para normalizar volume)
SOURCE_EXTS = ('.m4a', '.m4b', '.ogg', '.opus', '.flac', '.wav', '.wma', '.aac', '.mp3')

# Configurações de Áudio
BITRATE = "320k"
SAMPLE_RATE = "44100"

# Normalização (Padrão Spotify/Carro)
LOUDNESS_TARGET = "-14" # LUFS
TRUE_PEAK = "-1"        # dBTP

def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')

def install_ffmpeg():
    """Tenta instalar o FFmpeg automaticamente baseado no SO."""
    system = platform.system()
    print(f"\n⚙️  Sistema detectado: {system}")
    print("⏳ Tentando instalar FFmpeg automaticamente...")

    try:
        if system == "Windows":
            subprocess.run(["winget", "install", "-e", "--id", "Gyan.FFmpeg"], check=True)
        elif system == "Darwin": # macOS
            subprocess.run(["brew", "install", "ffmpeg"], check=True)
        elif system == "Linux":
            print("❌ No Linux, instale manualmente: sudo apt install ffmpeg")
            return False
        else:
            return False
        
        return shutil.which("ffmpeg") is not None
    except Exception as e:
        print(f"❌ Falha na instalação automática: {e}")
        return False

def check_dependencies():
    """Verifica e garante que o FFmpeg existe."""
    if shutil.which("ffmpeg"):
        return True

    clear_screen()
    print("⚠️  FFMPEG NÃO ENCONTRADO ⚠️")
    print("---------------------------------------------------")
    print("Este script precisa do FFmpeg para processar áudio.")
    
    resp = input("Deseja tentar instalar agora? (S/N): ").strip().lower()
    if resp in ('s', 'sim', 'y'):
        if install_ffmpeg():
            print("\n🎉 Instalado com sucesso! Continuando...")
            time.sleep(2)
            return True
    
    print("\n🚫 Instalação cancelada ou falhou. Encerrando.")
    sys.exit()

def sanitize_filename(filename):
    """
    Remove acentos e caracteres especiais.
    Retorna apenas alfanuméricos, underscores e hifens.
    """
    nfkd_form = unicodedata.normalize('NFKD', filename)
    filename = "".join([c for c in nfkd_form if not unicodedata.combining(c)])
    # Substitui espaços por _ e remove símbolos perigosos
    safe_name = filename.replace(" ", "_").replace("&", "e").replace("+", "_")
    return "".join([c for c in safe_name if c.isalnum() or c in ('_', '-')])

def process_file_task(args):
    """
    Função Worker executada em paralelo.
    Recebe uma tupla (root, filename) e retorna status string.
    """
    root, filename = args
    input_path = os.path.join(root, filename)
    
    # Prepara nomes
    name_no_ext = os.path.splitext(filename)[0]
    safe_name = sanitize_filename(name_no_ext) + TARGET_EXT
    output_path = os.path.join(root, safe_name)
    temp_path = os.path.join(root, f".temp_{safe_name}") # Arquivo oculto temporário

    # Evita processar arquivos temporários ou loops infinitos
    if filename.startswith(".temp_"):
        return None

    try:
        # Filtro de Normalização EBU R128 + Downmix Stereo
        audio_filter = f"loudnorm=I={LOUDNESS_TARGET}:TP={TRUE_PEAK}:LRA=11"

        command = [
            'ffmpeg',
            '-i', input_path,
            '-vn',                   # Sem vídeo
            '-ar', SAMPLE_RATE,      # 44.1kHz
            '-ac', '2',              # Stereo Force
            '-b:a', BITRATE,         # 320k
            '-af', audio_filter,     # Normalização
            '-map_metadata', '0',    # Copia tags
            '-id3v2_version', '3',   # Compatibilidade ID3v2.3
            '-y',                    # Overwrite
            '-hide_banner',
            '-loglevel', 'error',
            temp_path
        ]
        
        # Executa conversão
        subprocess.run(command, check=True)

        # Validação de Integridade (> 50KB)
        if os.path.exists(temp_path) and os.path.getsize(temp_path) > 50000:
            # Substituição Atômica
            if os.path.exists(output_path):
                os.remove(output_path) # Remove antigo se existir (mesmo nome)
            
            os.rename(temp_path, output_path) # Move temp para final

            # Limpeza do arquivo original (se for diferente do final)
            if input_path != output_path and os.path.exists(input_path):
                os.remove(input_path)
                return f"✅ Convertido e Limpo: {filename}"
            
            return f"✅ Normalizado: {filename}"
        else:
            if os.path.exists(temp_path): os.remove(temp_path)
            return f"⚠️ Falha de integridade (muito pequeno): {filename}"

    except Exception as e:
        if os.path.exists(temp_path): os.remove(temp_path)
        return f"❌ Erro em {filename}: {str(e)}"

def main():
    if not check_dependencies(): return

    clear_screen()
    current_dir = os.getcwd()
    
    print(f"🚀 AUDIO CONVERTER PRO (Multithreaded)")
    print(f"📂 Diretório: {current_dir}")
    print(f"🎚️  Alvo: MP3 320k | {LOUDNESS_TARGET} LUFS (Normalizado)")
    print("---------------------------------------------------")

    # 1. Fase de Escaneamento
    print("🔍 Escaneando diretórios...")
    tasks = []
    for root, dirs, files in os.walk(current_dir):
        for file in files:
            if file.lower().endswith(SOURCE_EXTS) and not file.startswith(".temp_"):
                tasks.append((root, file))

    total = len(tasks)
    if total == 0:
        print("Nenhum arquivo de áudio encontrado.")
        return

    print(f"🎯 {total} arquivos na fila. Iniciando processamento paralelo...")
    print("---------------------------------------------------")

    # 2. Fase de Processamento (ThreadPool)
    start_time = time.time()
    processed_count = 0

    # Usa número de CPUs da máquina automaticamente
    with concurrent.futures.ThreadPoolExecutor() as executor:
        # Submete tarefas e processa conforme completam
        for result in executor.map(process_file_task, tasks):
            if result:
                processed_count += 1
                print(f"[{processed_count}/{total}] {result}")

    duration = time.time() - start_time
    print("---------------------------------------------------")
    print(f"🏁 Concluído em {duration:.2f} segundos.")
    print(f"🔥 Média: {duration/total:.2f}s por música.")
    
    input("\nPressione [ENTER] para sair...")

if __name__ == "__main__":
    main()