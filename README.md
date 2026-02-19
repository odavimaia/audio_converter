# 🎵 Audio Suite Pro

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-000000?style=for-the-badge&logo=jetpackcompose&logoColor=white)

## 📌 Visão Geral da Engenharia

O **Audio Suite Pro** é um estudo de caso prático focado em **Sistemas Distribuídos Locais** e **Interoperabilidade entre Linguagens**.

Em vez de ser um aplicativo monolítico, este projeto demonstra como construir uma aplicação Desktop robusta em Kotlin (JVM) que atua como orquestradora de processos externos escritos em Python, sem exigir que o usuário final configure ambientes ou instale dependências manualmente.

O aplicativo resolve o problema clássico de distribuir ferramentas CLI (Command Line Interface) poderosas — porém inacessíveis para o usuário comum — empacotando-as com uma Interface Gráfica (GUI) moderna, reativa e de fácil uso, construída com **Compose Multiplatform**.

### Principais Desafios Resolvidos:
* Orquestração de threads e leitura de fluxos de dados (stdout/stderr) em tempo real entre a JVM e o processo Python usando `ProcessBuilder`.
* Gerenciamento de estado reativo na UI (MVVM) baseado no log do terminal embutido.
* Solução de embutimento (*Embedding*) de um ambiente Python portátil e binários do FFmpeg diretamente no instalador do Windows.
* Processamento paralelo (Multithreading) no Python para normalização de áudio em lote, controlando dinamicamente o uso de CPU.

---

## ⚠️ Aviso Legal / Disclaimer

Este projeto é uma **Prova de Conceito (PoC)** educacional focada em arquitetura de software, desenvolvimento KMP (Kotlin Multiplatform) e integração entre processos do sistema operacional.

A funcionalidade de download atua apenas como um meio para testar a integração com a biblioteca `yt-dlp`. O uso deste software para obter conteúdo protegido por direitos autorais sem a devida permissão pode violar os Termos de Serviço de plataformas de streaming e as leis locais.

O desenvolvedor **não incentiva a pirataria** e não se responsabiliza pelo uso indevido da ferramenta. O projeto foi desenhado sob o conceito de *Format Shifting* e *Preservação Digital* para arquivamento pessoal e offline de mídias de domínio público ou *Creative Commons*.

## 🏗️ Arquitetura e Fluxo de Dados

O aplicativo segue os princípios do **Clean Architecture** e do padrão **MVVM (Model-View-ViewModel)**, garantindo a separação de responsabilidades e facilitando a manutenção e a testabilidade. A comunicação com os processos externos é feita de forma assíncrona, não bloqueando a UI Thread (foco em performance e responsividade).

### Diagrama de Execução (High-Level)

```text
[ Compose UI (Kotlin) ]
        │
        ▼ (Ações do Usuário)
[ AppViewModel ] ──(Gerencia Estado Reativo / Coroutines)──┐
        │                                                  │
        ▼ (Delega Execução)                                │ (Atualiza UI via StateFlow)
[ PythonRepository ]                                       │
        │                                                  │
        ▼ (Inicia ProcessBuilder c/ Redirecionamento IO)   │
[ Python Embeddable (Ambiente Isolado) ]                   │
        │                                                  │
        ├─► downloader.py (yt-dlp) ────────────────────────┤ (Emite Logs via stdout)
        │                                                  │
        └─► universal_converter.py (ThreadPool / FFmpeg) ──┘
```

### Padrões de Projeto e Técnicas Adotadas:
* **Inversão de Controle (IoC):** Os scripts Python não tomam decisões sobre diretórios. O Kotlin atua como o orquestrador mestre, injetando os parâmetros de execução via `sys.argv`.
* **Programação Reativa:** Uso de Kotlin `Flow` no repositório para capturar o *stream* de dados (`stdout`) do terminal em tempo real, convertido em estado visual pelo `ViewModel`.
* **Multithreading Inteligente:** O script de conversão calcula o número de *cores* físicos da máquina em tempo de execução e limita a criação de *workers* (máximo de 50% da CPU) para evitar travamentos no Sistema Operacional.

## 🛠️ Stack Tecnológica

* **Frontend / Core:** Kotlin Multiplatform (JVM), Compose Desktop, Coroutines & Flow (Assincronismo), Gradle KTS.
* **Backend Interno:** Python 3 (Ambiente Portátil/Embeddable), `yt-dlp`, FFmpeg (Normalização EBU R128).
* **Distribuição:** Inno Setup (Empacotamento automático e isolamento de dependências).

## 🚀 Como Instalar e Usar

A versão final empacotada pode ser baixada gratuitamente no **Itch.io**. O instalador já inclui o ambiente Python isolado e os binários do FFmpeg, não exigindo **nenhuma** configuração extra ou instalação prévia no sistema do usuário.

🔗 **[Baixar Audio Suite Pro no Itch.io](https://odavimaia.itch.io/audio-suite-pro)**

> **Nota sobre o Windows SmartScreen:** > Como este é um projeto de engenharia independente (sem um certificado comercial pago de assinatura de código), o Windows pode exibir uma tela azul de alerta ("O Windows protegeu o seu computador") na primeira execução do instalador. 
> Para prosseguir com segurança, clique em **"Mais informações"** e depois no botão **"Executar assim mesmo"**.

## 👨‍💻 Para Desenvolvedores (Build Local)

Se você deseja explorar a arquitetura KMP, clonar e rodar o projeto na sua máquina:

### 1. Pré-requisitos
* **JDK 17** ou superior.
* **Android Studio** (Koala ou mais recente) ou **IntelliJ IDEA**.
* (Opcional) Instalação global do `Python 3` e do `FFmpeg` no seu `PATH` se quiser rodar o ambiente de desenvolvimento sem precisar baixar o Python Embeddable.

### 2. Clonando e Executando
O projeto usa o **Gradle** para gerenciar as dependências do Kotlin Multiplatform e do Compose Desktop.

```bash
# Clone o repositório
git clone [https://github.com/SEU_USUARIO/AudioSuitePro.git](https://github.com/SEU_USUARIO/AudioSuitePro.git)

# Entre no diretório do projeto
cd AudioSuitePro

# Rode a aplicação em modo de desenvolvimento (JVM)
./gradlew run