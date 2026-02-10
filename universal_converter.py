import os
import subprocess
import shutil
import sys
import platform
import unicodedata
import time

# === CONFIGURAÇÕES ===
TARGET_EXT = ".mp3"
SOURCE_EXTS = ('.m4a', '.m4b', '.ogg', '.opus', '.flac', '.wav', '.wma', '.aac')
BITRATE = "320k"
SAMPLE_RATE = "44100"

def clear_screen():
    """Limpa o terminal para uma UX mais limpa."""
    os.system('cls' if os.name == 'nt' else 'clear')

def install_ffmpeg():
    """Tenta instalar o FFmpeg baseado no SO."""
    system = platform.system()
    
    print(f"\n⚙️  Detectado sistema: {system}")
    print("⏳ Tentando instalar FFmpeg automaticamente...")

    try:
        if system == "Windows":
            # Usa Winget (Padrão no Windows 10/11 modernos)
            subprocess.run(["winget", "install", "-e", "--id", "Gyan.FFmpeg"], check=True)
        elif system == "Darwin": # macOS
            # Usa Homebrew
            subprocess.run(["brew", "install", "ffmpeg"], check=True)
        elif system == "Linux":
            # Linux é complexo (apt, dnf, pacman). Melhor pedir manual.
            print("❌ No Linux, a instalação varia por distribuição.")
            print("👉 Execute: sudo apt install ffmpeg (Ubuntu/Debian) ou equivalente.")
            return False
        else:
            print("❌ Sistema operacional não suportado para instalação automática.")
            return False
        
        print("\n✅ Instalação parece ter ocorrido. Verificando...")
        return shutil.which("ffmpeg") is not None

    except Exception as e:
        print(f"\n❌ Falha na instalação automática: {e}")
        print("👉 Por favor, instale manualmente e reinicie este script.")
        return False

def check_dependencies():
    """Verifica se o FFmpeg existe e interage com o usuário se não."""
    if shutil.which("ffmpeg"):
        return True

    clear_screen()
    print("⚠️  BIBLIOTECA FFMPEG NÃO ENCONTRADA ⚠️")
    print("---------------------------------------------------")
    print("O script precisa do 'ffmpeg' para converter os áudios.")
    print("Sem ele, não é possível transformar os arquivos para o som do carro.")
    print("---------------------------------------------------")
    
    while True:
        resp = input("Deseja tentar instalar o FFmpeg agora? (S/N): ").strip().lower()
        
        if resp in ('s', 'sim', 'y', 'yes'):
            if install_ffmpeg():
                print("\n🎉 FFmpeg instalado com sucesso! Continuando...")
                time.sleep(2)
                return True
            else:
                input("\nInstallation failed. Pressione Enter para sair...")
                sys.exit()
        
        elif resp in ('n', 'nao', 'no'):
            print("\n---------------------------------------------------")
            print("🙏 Obrigado por usar o script.")
            print("Entendo. O script será encerrado agora.")
            print("---------------------------------------------------")
            input("Pressione [ENTER] para fechar o terminal...")
            sys.exit() # Encerra o script graciosamente

def sanitize_filename(filename):
    nfkd_form = unicodedata.normalize('NFKD', filename)
    filename = "".join([c for c in nfkd_form if not unicodedata.combining(c)])
    return "".join([c for c in filename.replace(" ", "_") if c.isalnum() or c in ('_', '-')])

def process_file(root, file):
    input_path = os.path.join(root, file)
    safe_base_name = sanitize_filename(os.path.splitext(file)[0])
    output_path = os.path.join(root, safe_base_name + TARGET_EXT)

    if input_path == output_path: return

    print(f"🔄 Processando: {file}")
    try:
        subprocess.run([
            'ffmpeg', '-i', input_path, '-vn', '-ar', SAMPLE_RATE, '-ac', '2',
            '-b:a', BITRATE, '-map_metadata', '0', '-id3v2_version', '3',
            '-y', '-hide_banner', '-loglevel', 'error', output_path
        ], check=True)

        if os.path.exists(output_path) and os.path.getsize(output_path) > 10000:
            os.remove(input_path)
            print(f"   ✅ Sucesso! Original removido.")
        else:
            print(f"   ⚠️  Erro de integridade. Original mantido.")

    except subprocess.CalledProcessError:
        print(f"   ❌ Erro no arquivo.")
        if os.path.exists(output_path): os.remove(output_path)

def main():
    # 1. Verifica Dependências antes de tudo
    check_dependencies()
    
    # 2. Inicia o fluxo principal
    clear_screen()
    current_dir = os.getcwd()
    print(f"🚀 Iniciando Conversor Universal Automotivo")
    print(f"📂 Diretório: {current_dir}")
    print("---------------------------------------------------")

    count = 0
    for root, dirs, files in os.walk(current_dir):
        for file in files:
            if file.lower().endswith(SOURCE_EXTS):
                process_file(root, file)
                count += 1

    print("---------------------------------------------------")
    if count == 0:
        print("Nenhum arquivo de áudio compatível encontrado.")
    else:
        print(f"🏁 Finalizado. {count} arquivos processados.")
    
    input("\nPressione [ENTER] para sair...")

if __name__ == "__main__":
    main()