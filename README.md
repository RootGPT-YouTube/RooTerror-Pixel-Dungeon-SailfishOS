<p align="center">
  <img src="sailfish/grafica/icone/128x128/rooterror-pixel-dungeon.png" width="128" alt="RooTerror Pixel Dungeon icon">
</p>

# RooTerror Pixel Dungeon

A **native Sailfish OS port of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon)**,
the open-source traditional roguelike by **Evan Debenham**, itself based on **Pixel Dungeon** by
**Oleg Dolya (Watabou)**. It runs on its own small Java runtime, with OpenGL ES and Wayland touch input —
no Android layer needed. Part of the `RooT*` app family.

---

## [ENGLISH]

Prerequisite: Sailfish OS 5.1 or later · **aarch64** (armv7hl packages are built but not yet tested on a real 32-bit device)  
Telegram Group: https://t.me/+E7V-a7x4JbY1Njhk  
RooTerror Pixel Dungeon is tested on:  
- POCO M4 Pro 4G (Sailfish OS 5.2.0.22, community port)  

This application was developed using artificial intelligence technologies, specifically Claude Code Opus. Therefore, if the use of an application generated via a large-scale language model (LLM) is not comfortable for the user, it is recommended to avoid its installation and use. It is specified that any negative comment regarding this circumstance will not only be ignored but will result in the immediate blocking of the user.

I hereby disclaim any and all responsibility for the application, its functionality, and any consequences arising from its use. By choosing to use this application, the user acknowledges and accepts that they do so entirely at their own risk, and agrees that the developer shall not be held liable for any damages, losses, or adverse effects—whether direct, indirect, incidental, or consequential—resulting from the use or misuse of the application.

### Credits

RooTerror Pixel Dungeon **is Evan Debenham's game**: the game design, the code, the art and all the content come
from **[Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/)**; this port only adds what Sailfish OS needs.

- **Shattered Pixel Dungeon** — developed by **Evan Debenham** ([shatteredpixel.com](https://shatteredpixel.com),
  [source](https://github.com/00-Evan/shattered-pixel-dungeon), [Patreon](https://www.patreon.com/ShatteredPixel))
- **Pixel Dungeon** — developed by **Oleg Dolya (Watabou)** ([watabou.itch.io](https://watabou.itch.io/)),
  inspired by Brian Walker's Brogue
- From the game's credits: splash and dungeon art **Aleksandar Komitov**, composer **Lumine Haaristo**, item pixel
  art **PumpkinVolt**, sound effects **Celesti**, additional pixel art **Alastair Braun**, Pixel Dungeon music
  **Cube Code**, the volunteer translators on **Transifex**, and **libGDX**
- Sailfish OS port — **RootGPT**

The full credits, unchanged, are in the game's «About» screen.

### What it is

- **Shattered Pixel Dungeon 4.0.2**, the whole game: randomized dungeons, enemies and hundreds of
  items, all hero classes and challenges, in the languages of the original game.
- Made for Sailfish OS:
  - **touch** through Wayland, with the game's own gestures;
  - a **back dot** in the top-left corner, like Sailfish pages: it appears when you can go back or
    close a window, and turns dark on light backgrounds and white on dark ones;
  - the **volume keys** control the game's sound;
  - when the game goes to the **cover** it pauses, stops drawing and stops the music
    (music in the background can be turned back on in the game's settings);
  - **safe margins** for the camera cut-out and the rounded corners of the screen.
- Its own name, title and icon, as Evan asks of modified versions; **all the original credits are
  kept**, with the port's credits added at the top of «About». No update checker and no news feed
  pointing to the original game's servers.
- Saves are in `~/.local/share/com.github.RootGPT_YouTube/rooterror/`.

### Install

Copy the RPM for your phone (`aarch64` = 64-bit, `armv7hl` = 32-bit) to the phone and install it:

```sh
devel-su pkcon install-local rooterror-pixel-dungeon-<version>.aarch64.rpm
```

`pkcon` (like the file manager, Storeman and Chum) also installs `libaudioresource` from the Jolla
repository if it is missing; a plain `rpm -i` would stop on that dependency.
**Updating:** the same command with the new RPM.

The game and its Java runtime (~95 MB) are installed under `/home/rooterror` (the `/home`
partition, because the phones' root file system is small); only the launcher, the `.desktop` file
and the icons stay on the root file system.

### Building from source

The Sailfish OS part lives in [`sailfish/`](sailfish): the Gradle module (launcher class, platform
support, touch, window events) and [`sailfish/packaging/`](sailfish/packaging) (native launcher that
starts the JVM through JNI, Wayland touch library, RPM spec, `.desktop`, NOTICE). The game itself
(`core/`) has only small changes, each marked `//RooTerror`; `desktop/` is untouched.

You need the Sailfish SDK (`sfdk`, targets `SailfishOS-5.1.0.11-aarch64` and `-armv7hl`) and three
[Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17) downloads: x64 for the build
machine, aarch64 and arm (32-bit) for their `jmods`. Then:

```sh
JDK_HOST=/path/jdk-17-x64 JDK_AARCH64=/path/jdk-17-aarch64 JDK_ARM32=/path/jdk-17-arm \
  sailfish/packaging/prepara-pacchetto.sh          # both RPMs into sailfish/packaging/RPMS/
```

The scripts in [`sailfish/grafica/`](sailfish/grafica) redraw the ROOTERROR title, the icon's source
drawing and the hero avatar (Python + PySide6).

### License

RooTerror Pixel Dungeon is free software under the **GNU General Public License v3.0** (see
[LICENSE.txt](LICENSE.txt)), like Shattered Pixel Dungeon and Pixel Dungeon. Third-party
components — libGDX, LWJGL (GLFW, OpenAL Soft), FreeType, gdx-controllers and Jamepad (SDL2), JLayer,
the Temurin/OpenJDK runtime — and their licenses
are listed in [sailfish/packaging/NOTICE.md](sailfish/packaging/NOTICE.md).
The original README of Shattered Pixel Dungeon is kept in [docs/README-shattered.md](docs/README-shattered.md).
If you like the game, please consider [supporting Evan on Patreon](https://www.patreon.com/ShatteredPixel).

---

## [ITALIANO]

Requisiti: Sailfish OS 5.1 o successivo · **aarch64** (i pacchetti armv7hl sono costruiti ma non ancora provati su un vero telefono a 32 bit)  
Gruppo Telegram: https://t.me/+E7V-a7x4JbY1Njhk  
RooTerror Pixel Dungeon è testato:  
- POCO M4 Pro 4G (Sailfish OS 5.2.0.22, port della comunità)  

Questa applicazione è stata sviluppata utilizzando tecnologie di intelligenza artificiale, in particolare Claude Code Opus. Pertanto, se l'uso di un'applicazione generata tramite un modello linguistico su larga scala (LLM) non fosse per l'utente confortevole, si raccomanda di evitarne l'installazione e l'uso. Si specifica che qualsiasi commento negativo riguardante questa circostanza non verrà solo ignorato, ma comporterà il blocco immediato dell'utente.

Con la presente declino ogni responsabilità relativa all'applicazione, al suo funzionamento e a qualsiasi conseguenza derivante dal suo utilizzo. L'utente, scegliendo di utilizzare l'applicazione, riconosce e accetta di farlo a proprio ed esclusivo rischio, e concorda che lo sviluppatore non potrà essere ritenuto responsabile per eventuali danni, perdite o effetti negativi — diretti, indiretti, incidentali o consequenziali — derivanti dall'uso o dall'uso improprio dell'applicazione.

### Crediti

RooTerror Pixel Dungeon **è il gioco di Evan Debenham**: il design, il codice, la grafica e tutti i contenuti vengono
da **[Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/)**; questo porting aggiunge solo quello che serve
a Sailfish OS.

- **Shattered Pixel Dungeon** — sviluppato da **Evan Debenham** ([shatteredpixel.com](https://shatteredpixel.com),
  [sorgente](https://github.com/00-Evan/shattered-pixel-dungeon), [Patreon](https://www.patreon.com/ShatteredPixel))
- **Pixel Dungeon** — sviluppato da **Oleg Dolya (Watabou)** ([watabou.itch.io](https://watabou.itch.io/)),
  ispirato a Brogue di Brian Walker
- Dai crediti del gioco: illustrazioni e grafica dei sotterranei **Aleksandar Komitov**, musiche **Lumine Haaristo**,
  pixel art degli oggetti **PumpkinVolt**, effetti sonori **Celesti**, altra pixel art **Alastair Braun**, musiche di
  Pixel Dungeon **Cube Code**, i traduttori volontari su **Transifex**, e **libGDX**
- Porting per Sailfish OS — **RootGPT**

I crediti completi, invariati, sono nella schermata «Informazioni» del gioco.

### Cos'è

- **Shattered Pixel Dungeon 4.0.2**, il gioco completo: sotterranei e nemici sempre diversi e
  centinaia di oggetti, tutte le classi di eroi e le sfide, nelle lingue del gioco originale.
- Fatto per Sailfish OS:
  - **tocco** tramite Wayland, con i gesti del gioco;
  - un **pallino «indietro»** in alto a sinistra, come nelle pagine di Sailfish: compare quando si
    può tornare indietro o chiudere una finestra, e diventa scuro sugli sfondi chiari e bianco su
    quelli scuri;
  - i **tasti del volume** regolano il suono del gioco;
  - quando il gioco va in **copertina** si mette in pausa, smette di disegnare e ferma la musica
    (la musica in sottofondo si può riattivare nelle impostazioni del gioco);
  - **margini** per il foro della fotocamera e gli angoli arrotondati dello schermo.
- Nome, titolo e icona propri, come Evan chiede alle versioni modificate; **tutti i crediti
  originali restano**, con quelli del porting aggiunti in cima a «Informazioni». Niente controllo
  degli aggiornamenti e niente notizie verso i server del gioco originale.
- I salvataggi stanno in `~/.local/share/com.github.RootGPT_YouTube/rooterror/`.

### Installazione

Copia sul telefono l'RPM giusto (`aarch64` = 64 bit, `armv7hl` = 32 bit) e installalo:

```sh
devel-su pkcon install-local rooterror-pixel-dungeon-<versione>.aarch64.rpm
```

`pkcon` (come il gestore file, Storeman e Chum) installa anche `libaudioresource` dal repository
di Jolla se manca; un semplice `rpm -i` si fermerebbe su quella dipendenza.
**Aggiornamento:** lo stesso comando con l'RPM nuovo.

Il gioco e il suo Java (~95 MB) si installano in `/home/rooterror` (la partizione `/home`, perché il
file system principale dei telefoni è piccolo); sul file system principale restano solo il
lanciatore, il file `.desktop` e le icone.

### Compilare dal sorgente

La parte per Sailfish OS sta in [`sailfish/`](sailfish): il modulo Gradle (classe di avvio,
supporto della piattaforma, tocco, eventi della finestra) e [`sailfish/packaging/`](sailfish/packaging)
(lanciatore nativo che avvia la JVM con JNI, libreria del tocco Wayland, spec dell'RPM, `.desktop`,
NOTICE). Il gioco (`core/`) ha solo piccole modifiche, ciascuna segnata `//RooTerror`; `desktop/` è
intatto.

Servono l'SDK di Sailfish (`sfdk`, target `SailfishOS-5.1.0.11-aarch64` e `-armv7hl`) e tre download
di [Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17): x64 per la macchina di
compilazione, aarch64 e arm (32 bit) per i loro `jmods`. Poi:

```sh
JDK_HOST=/percorso/jdk-17-x64 JDK_AARCH64=/percorso/jdk-17-aarch64 JDK_ARM32=/percorso/jdk-17-arm \
  sailfish/packaging/prepara-pacchetto.sh          # i due RPM in sailfish/packaging/RPMS/
```

Gli script in [`sailfish/grafica/`](sailfish/grafica) ridisegnano il titolo ROOTERROR, il disegno
sorgente dell'icona e l'avatar dell'eroe (Python + PySide6).

### Licenza

RooTerror Pixel Dungeon è software libero con licenza **GNU General Public License v3.0** (vedi
[LICENSE.txt](LICENSE.txt)), come Shattered Pixel Dungeon e Pixel Dungeon. I componenti di terze
parti — libGDX, LWJGL (GLFW, OpenAL Soft), FreeType, gdx-controllers e Jamepad (SDL2), JLayer, il
runtime Temurin/OpenJDK — e le loro licenze sono
elencati in [sailfish/packaging/NOTICE.md](sailfish/packaging/NOTICE.md).
Il README originale di Shattered Pixel Dungeon è conservato in [docs/README-shattered.md](docs/README-shattered.md).
Se il gioco ti piace, valuta di [sostenere Evan su Patreon](https://www.patreon.com/ShatteredPixel).
