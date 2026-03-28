import os
import sys
import subprocess
import shutil
import io

# --- VACINA DO WINDOWS (Fix Emojis) ---
if sys.platform.startswith('win'):
    try:
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
    except Exception:
        pass
# --------------------------------------

USER_MUSIC = os.path.join(os.path.expanduser("~"), "Music")
OUTPUT_FOLDER = os.path.join(USER_MUSIC, "AudioSuitePro")

def install_ffmpeg_check():
    if shutil.which("ffmpeg") is None:
        print("❌ ERRO FATAL: FFmpeg não encontrado.")
        sys.exit(1)

def download_with_ytdlp(query, is_direct_url=False):
    target = query if is_direct_url else f"ytsearch1:{query}"
    os.makedirs(OUTPUT_FOLDER, exist_ok=True)

    # Template customizado para o Kotlin ler: [download] [1/5] 50%
    progress_tpl = "[download] [%(info.playlist_index|1)s/%(info.n_entries|1)s] %(progress._percent_str)s"

    command = [
        sys.executable, '-m', 'yt_dlp',
        target,
        '-x',
        '--audio-format', 'mp3',
        '--audio-quality', '0',
        '-P', OUTPUT_FOLDER,
        '--newline',
        '--progress',
        '--no-warnings',
        '--ignore-errors',
        '--progress-template', progress_tpl
    ]

    if is_direct_url and ("list=" in query or "playlist" in query.lower()):
        template_album = '%(playlist,album,playlist_title|Álbum Desconhecido)s/%(playlist_index)02d - %(title)s.%(ext)s'
        command.extend(['--yes-playlist', '-o', template_album])
    else:
        template_solto = '%(title)s.%(ext)s'
        command.extend(['--no-playlist', '-o', template_solto])

    try:
        process = subprocess.Popen(
            command, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.STDOUT, 
            text=True, 
            encoding='utf-8',
            bufsize=1
        )
        
        for line in process.stdout:
            print(line, end='', flush=True)
            
        process.wait()
    except Exception as e:
        print(f"⚠️ Erro ao processar download: {e}", flush=True)

def main():
    install_ffmpeg_check()
    if len(sys.argv) > 1:
        url = sys.argv[1]
        download_with_ytdlp(url, is_direct_url=True)
        print("✅ Downloads finalizados.", flush=True)
    else:
        print("🎵 AUDIO SUITE PRO - DOWNLOADER", flush=True)
        url = input("Cole a URL do YouTube: ").strip()
        if url:
            download_with_ytdlp(url, is_direct_url=True)
            print("✅ Downloads finalizados.", flush=True)

if __name__ == "__main__":
    main()
