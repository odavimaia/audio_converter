import os
import sys
import subprocess
import shutil
import spotipy
from spotipy.oauth2 import SpotifyClientCredentials

# --- VACINA DO WINDOWS (Fix Emojis) ---
# Isso impede o erro "UnicodeEncodeError"
if sys.platform.startswith('win'):
    try:
        sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
        sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
    except Exception:
        pass
# --------------------------------------

# === CONFIGURAÇÕES DO SPOTIFY ===
# Cole suas chaves aqui:
SPOTIPY_CLIENT_ID = 'SPOTIPY_CLIENT_ID'
SPOTIPY_CLIENT_SECRET = 'SPOTIPY_CLIENT_SECRET'

# --- MUDANÇA AQUI: Usa a pasta de Músicas do Windows ---
USER_MUSIC = os.path.join(os.path.expanduser("~"), "Music")
OUTPUT_FOLDER = os.path.join(USER_MUSIC, "AudioSuite_Downloads")
# -------------------------------------------------------

def install_ffmpeg_check():
    if shutil.which("ffmpeg") is None:
        print("❌ FFmpeg não encontrado. O script precisa dele.")
        sys.exit(1)

def get_spotify_tracks(playlist_url):
    print("🔓 Conectando na API do Spotify...")
    try:
        # CORREÇÃO: Removemos o redirect_uri que estava causando erro
        auth_manager = SpotifyClientCredentials(client_id=SPOTIPY_CLIENT_ID, 
                                                client_secret=SPOTIPY_CLIENT_SECRET)
        sp = spotipy.Spotify(auth_manager=auth_manager)
        
        results = sp.playlist_tracks(playlist_url)
        tracks = results['items']
        
        while results['next']:
            results = sp.next(results)
            tracks.extend(results['items'])
            
        search_terms = []
        for item in tracks:
            track = item['track']
            if track:
                artist = track['artists'][0]['name']
                name = track['name']
                search_terms.append(f"{artist} - {name} lyrics audio")
        
        return search_terms

    except Exception as e:
        print(f"❌ Erro na API do Spotify: {e}")
        return []

def download_with_ytdlp(query, is_direct_url=False):
    target = query if is_direct_url else f"ytsearch1:{query}"
    print(f"⬇️  Processando: {query}")
    
    # --- MUDANÇA AQUI ---
    # Em vez de chamar 'yt-dlp' direto, chamamos o Python para rodar o módulo.
    # Isso corrige o erro de "arquivo não encontrado".
    command = [
        sys.executable, '-m', 'yt_dlp', 
        target,
        '-x', 
        '--audio-format', 'mp3', 
        '--audio-quality', '0',
        '-o', f'{OUTPUT_FOLDER}/%(title)s.%(ext)s',
        '--no-playlist', 
        '--quiet', 
        '--no-warnings', 
        '--ignore-errors'
    ]
    # --------------------

    if is_direct_url and "list=" in query:
        command.remove('--no-playlist')

    try:
        subprocess.run(command, check=True)
    except subprocess.CalledProcessError:
        print(f"⚠️  Falha ao baixar: {query}")

def run_converter():
    print("\n🚀 Preparando Normalizador...")
    converter_script = "universal_converter.py"
    
    if not os.path.exists(converter_script):
        print(f"⚠️  Script '{converter_script}' não encontrado nesta pasta.")
        return

    # Copia e executa
    target_script = os.path.join(OUTPUT_FOLDER, converter_script)
    shutil.copy(converter_script, target_script)
    subprocess.run([sys.executable, converter_script], cwd=OUTPUT_FOLDER)
    
    # Tenta remover o script copiado (pode falhar se o script limpar a si mesmo, então ignoramos erro)
    try:
        if os.path.exists(target_script): os.remove(target_script)
    except:
        pass

def main():
    install_ffmpeg_check()
    if not os.path.exists(OUTPUT_FOLDER): os.makedirs(OUTPUT_FOLDER)

    # Lógica Híbrida: CLI vs GUI
    # Se tiver argumentos (ex: python downloader.py "https://youtube...")
    if len(sys.argv) > 1:
        url = sys.argv[1]
        print(f"🔗 URL recebida via argumento: {url}")
        auto_mode = True # Modo automático (sem perguntas no final)
    else:
        # Modo Manual (pergunta pro usuário)
        print("🎵 MUSIC DOWNLOADER PRO")
        print("---------------------------------------------------")
        url = input("Cole a URL (Spotify Playlist ou YouTube): ").strip()
        auto_mode = False

    # Validação e Download
    if "spotify.com" in url:
        tracks = get_spotify_tracks(url) # Certifique-se que essa função existe no seu código
        if not tracks: return
        print(f"🔍 Encontradas {len(tracks)} músicas. Baixando...")
        for i, t in enumerate(tracks):
            print(f"[{i+1}/{len(tracks)}]", end=" ")
            download_with_ytdlp(t)

    elif "youtu" in url:
        download_with_ytdlp(url, is_direct_url=True)
    else:
        print("❌ URL inválida.")
        return

    print("---------------------------------------------------")
    print("✅ Downloads finalizados.")

    # Se for modo automático (GUI), a gente não pergunta nada, só encerra.
    # O Kotlin vai chamar o conversor depois separadamente.
    if auto_mode:
        return

    # Modo manual continua com a pergunta
    print("📜 (Role para cima para ver o histórico de downloads)")
    resp = input("\nDeseja iniciar a conversão/normalização agora? (S/N): ").strip().lower()
    
    if resp in ('s', 'sim', 'y'):
        run_converter()
    else:
        print("\nOk! Os arquivos originais estão na pasta 'Downloads_Music'.")
        input("Pressione Enter para sair...")

if __name__ == "__main__":
    main()