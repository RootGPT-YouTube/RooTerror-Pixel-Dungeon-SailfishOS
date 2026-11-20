# RooTerror Pixel Dungeon — crediti e licenze

RooTerror Pixel Dungeon è il porting nativo per Sailfish OS di **Shattered Pixel Dungeon**.

- **Shattered Pixel Dungeon** — Copyright (C) 2014-2026 Evan Debenham — GPLv3 —
  https://github.com/00-Evan/shattered-pixel-dungeon
- **Pixel Dungeon** — Copyright (C) 2012-2015 Oleg Dolya — GPLv3
- Porting per Sailfish OS (modulo `sailfish`, lanciatore, tocco Wayland) — RootGPT — GPLv3

Il codice sorgente completo, comprese le modifiche del porting, è disponibile secondo la GPLv3.

Componenti di terze parti inclusi:

- **libGDX** e **gdx-lwjgl3-angle** (classe `Lwjgl3GLES20`) — Apache License 2.0
- **LWJGL 3** (GLFW, OpenGL ES, EGL, OpenAL Soft, jemalloc, stb) — BSD 3-Clause; GLFW zlib/libpng;
  OpenAL Soft LGPLv2; jemalloc BSD 2-Clause; stb MIT o dominio pubblico
- **gdx-freetype** (libGDX) con **FreeType** — Apache License 2.0; FreeType con la FreeType License (FTL):
  «Portions of this software are copyright © The FreeType Project (www.freetype.org). All rights reserved.»
- **gdx-controllers** e **Jamepad** (gamepad, con dentro **SDL2**) — Apache License 2.0; SDL2 zlib
- **JLayer** (decodifica MP3 di libGDX) — GNU LGPL 2.1; è nel jar come classi separate (`javazoom/`),
  sostituibili: il sorgente è su https://github.com/libgdx/jlayer
- **Eclipse Temurin / OpenJDK 17** (runtime ridotto con jlink) — GPLv2 con Classpath Exception;
  le licenze complete sono in `/home/rooterror/jre/legal/`
- Musiche ed effetti sonori: vedi i crediti nel gioco (schermata «Informazioni»)
