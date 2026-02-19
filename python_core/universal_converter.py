import os
import sys
import shutil
import subprocess
import platform
import unicodedata
import time
import concurrent.futures
import io

# --- VACINA DO WINDOWS (Fix Emojis) ---
if sys.platform.startswith('win'):
    try:
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
    except Exception:
        pass
# --------------------------------------

# === CONFIGURAÇÕES GLOBAIS ===
TARGET_EXT = ".mp3"
SOURCE_EXTS = ('.m4a', '.m4b', '.ogg', '.opus', '.flac', '.wav', '.wma', '.aac', '.mp3', '.mp4', '.webm')

BITRATE = "320k"
SAMPLE_RATE = "44100"
LOUDNESS_TARGET = "-14" # LUFS
TRUE_PEAK = "-1"        # dBTP

def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')

def install_ffmpeg():
    system = platform.system()
    print(f"\n⚙️  Sistema detectado: {system}")
    print("⏳ Tentando instalar FFmpeg automaticamente...")
    try:
        if system == "Windows":
            subprocess.run(["winget", "install", "-e", "--id", "Gyan.FFmpeg"], check=True)
        elif system == "Darwin":
            subprocess.run(["brew", "install", "ffmpeg"], check=True)
        elif system == "Linux":
            print("❌ No Linux, instale manualmente: sudo apt install ffmpeg")
            return False
        else:
            return False
        return shutil.which("ffmpeg") is not None
    except Exception as e:
        print(f"❌ Falha na instalação: {e}")
        return False

def check_dependencies():
    if shutil.which("ffmpeg"): return True
    clear_screen()
    print("⚠️ FFMPEG NÃO ENCONTRADO ⚠️")
    # Como rodará via Kotlin sem terminal interativo, evitamos o input() travando o app
    print("Por favor, garanta que o ffmpeg.exe está na pasta python_core.")
    sys.exit()

def sanitize_filename(filename):
    nfkd_form = unicodedata.normalize('NFKD', filename)
    filename = "".join([c for c in nfkd_form if not unicodedata.combining(c)])
    safe_name = filename.replace(" ", "_").replace("&", "e").replace("+", "_")
    return "".join([c for c in safe_name if c.isalnum() or c in ('_', '-')])

def process_file_task(input_path):
    """
    Refatorado para receber o caminho completo do arquivo.
    """
    root = os.path.dirname(input_path)
    filename = os.path.basename(input_path)

    name_no_ext = os.path.splitext(filename)[0]
    safe_name = sanitize_filename(name_no_ext) + TARGET_EXT
    output_path = os.path.join(root, safe_name)
    temp_path = os.path.join(root, f".temp_{safe_name}")

    if filename.startswith(".temp_"): return None

    try:
        audio_filter = f"loudnorm=I={LOUDNESS_TARGET}:TP={TRUE_PEAK}:LRA=11"
        command = [
            'ffmpeg', '-i', input_path, '-vn', '-ar', SAMPLE_RATE, '-ac', '2',
            '-b:a', BITRATE, '-af', audio_filter, '-map_metadata', '0',
            '-id3v2_version', '3', '-y', '-hide_banner', '-loglevel', 'error', temp_path
        ]

        subprocess.run(command, check=True)

        if os.path.exists(temp_path) and os.path.getsize(temp_path) > 50000:
            if os.path.exists(output_path): os.remove(output_path)
            os.rename(temp_path, output_path)

            if input_path != output_path and os.path.exists(input_path):
                os.remove(input_path)
                return f"✅ Convertido e Limpo: {filename}"
            return f"✅ Normalizado: {filename}"
        else:
            if os.path.exists(temp_path): os.remove(temp_path)
            return f"⚠️ Falha de integridade: {filename}"

    except Exception as e:
        if os.path.exists(temp_path): os.remove(temp_path)
        return f"❌ Erro em {filename}: {str(e)}"

def main():
    if not check_dependencies(): return

    # 1. PONTO DE ENTRADA REFATORADO (Recebe do Kotlin)
    if len(sys.argv) > 1:
        target_path = sys.argv[1]
    else:
        target_path = os.getcwd() # Fallback

    print(f"🚀 AUDIO CONVERTER PRO (Multithreaded)")
    print(f"📂 Alvo: {target_path}")
    print("---------------------------------------------------")

    tasks = []

    # 2. ROTEAMENTO: É arquivo único ou pasta?
    if os.path.isfile(target_path):
        if target_path.lower().endswith(SOURCE_EXTS):
            tasks.append(target_path)
        else:
            print("❌ Formato não suportado.")
            return
    elif os.path.isdir(target_path):
        for root, dirs, files in os.walk(target_path):
            for file in files:
                if file.lower().endswith(SOURCE_EXTS) and not file.startswith(".temp_"):
                    tasks.append(os.path.join(root, file))
    else:
        print("❌ Caminho inválido.")
        return

    total = len(tasks)
    if total == 0:
        print("Nenhum arquivo de áudio encontrado.")
        return

    print(f"🎯 {total} arquivos na fila. Iniciando...")

    start_time = time.time()
    processed_count = 0
    total_cores = os.cpu_count() or 2
    workers_limit = max(1, int(total_cores * 0.5))

    with concurrent.futures.ThreadPoolExecutor(max_workers=workers_limit) as executor:
        for result in executor.map(process_file_task, tasks):
            if result:
                processed_count += 1
                # Este print no formato [X/Y] é exatamente o que o AppViewModel.kt usa para mover a barra de progresso!
                print(f"[{processed_count}/{total}] {result}")

    print("---------------------------------------------------")
    print("✅ [1/1] Conversão Finalizada") # Gatilho de segurança pro Kotlin saber que acabou

if __name__ == "__main__":
    main()