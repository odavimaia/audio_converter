; Script do Inno Setup para Audio Suite Pro
#define MyAppName "Audio Suite Pro"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Davi Maia"
#define MyAppExeName "AudioSuitePro.exe"

[Setup]
; Identificação do App
AppId={{D3D14F30-8E30-4A30-9E30-AudioSuitePro}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}

; Configurações visuais
DisableProgramGroupPage=yes
; Aponta para o seu ícone .ico (ajuste o caminho se necessário)
SetupIconFile=composeApp\src\jvmMain\ComposeResources\drawable\app_icon.ico
UninstallDisplayIcon={app}\{#MyAppExeName}

; Onde salvar o instalador final
OutputDir=installer_output
OutputBaseFilename=AudioSuitePro_Setup_v1.0
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; AQUI É O SEGREDO: Pega tudo o que o Gradle gerou na pasta 'app'
; O asterisco (*) diz para pegar todos os arquivos e subpastas (incluindo python_core se estiver lá)
Source: "composeApp\build\compose\binaries\main\app\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "D:\Dev\AudioSuitePro\python_core\*"; DestDir: "{app}\python_core"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; Adicionamos "\AudioSuitePro" no caminho do Filename
Name: "{group}\{#MyAppName}"; Filename: "{app}\AudioSuitePro\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\AudioSuitePro\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; ESTA É A LINHA QUE CRIA A OPÇÃO DE INICIAR AO FINAL
Filename: "{app}\AudioSuitePro\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#MyAppName}}"; Flags: nowait postinstall skipifsilent