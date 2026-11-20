# RooTerror Pixel Dungeon — porting nativo per Sailfish OS di Shattered Pixel Dungeon
# (Evan Debenham, GPLv3), a sua volta basato su Pixel Dungeon (Oleg Dolya).
#
# Qui si compila solo la parte nativa (lanciatore ELF e tocco Wayland); jar del gioco
# e Java ridotto (jlink) arrivano già pronti in staging/ (sailfish/packaging/prepara-pacchetto.sh):
# Gradle vuole la rete, che il motore di build di sfdk non ha.
#
# I file grandi (Java ~52 MB, gioco ~42 MB) stanno in /home/rooterror: il rootfs dei
# telefoni è piccolo. Sul rootfs restano lanciatore, .desktop e icone.
# Lanciatore /usr/bin/rooterror-pixel-dungeon: sailjail rifiuta gli script e vuole un
# ELF in /usr/bin, e lipstick aggancia finestra e cover solo se la cmdline del processo
# è la riga Exec del .desktop; per questo la JVM gira DENTRO il lanciatore (JNI).

%global _apphome /home/rooterror
%global debug_package %{nil}
# niente strip automatico: il Java ridotto è già come deve essere
%define __os_install_post %{nil}
%define __brp_strip %{nil}
%define __brp_strip_static_archive %{nil}
%define __brp_strip_comment_note %{nil}

Name:       rooterror-pixel-dungeon
Version:    0.2.0
Release:    1
Summary:    RooTerror Pixel Dungeon — roguelike for Sailfish OS
License:    GPLv3+ and GPLv2 with exceptions and Apache-2.0 and BSD
Group:      Amusements/Games
URL:        https://github.com/00-Evan/shattered-pixel-dungeon
ExclusiveArch: aarch64 armv7hl
AutoReqProv: no
BuildRequires: pkgconfig(wayland-client)
BuildRequires: pkgconfig(audioresource)
BuildRequires: pkgconfig(glib-2.0)
# le dipendenze automatiche delle librerie a 64 bit portano «()(64bit)»
%ifarch aarch64
%global b64 ()(64bit)
%else
%global b64 %{nil}
%endif
# librerie di sistema usate da GLFW, LWJGL e OpenAL (estratte da libGDX in /tmp all'avvio)
Requires:   libEGL.so.1%{b64}
Requires:   libGLESv2.so.2%{b64}
Requires:   libwayland-client.so.0%{b64}
Requires:   libwayland-cursor.so.0%{b64}
Requires:   libwayland-egl.so.1%{b64}
Requires:   libxkbcommon.so.0%{b64}
Requires:   libpulse.so.0%{b64}
# risorsa audio di tipo gioco: senza, ohm mette il suono fuori dai tasti del volume
Requires:   libaudioresource

%description
RooTerror Pixel Dungeon is a native Sailfish OS port of Shattered Pixel Dungeon,
the traditional roguelike by Evan Debenham, based on Pixel Dungeon by Oleg Dolya.
It runs on its own small Java runtime, with OpenGL ES and Wayland touch input.

%prep

%build
gcc -O2 -Wall -Wextra -o rooterror-pixel-dungeon launcher.c \
  -Ijni-include -Ijni-include/linux '-DAPP_HOME="%{_apphome}"' \
  $(pkg-config --cflags --libs audioresource glib-2.0) -ldl -lpthread
gcc -O2 -Wall -Wextra -fPIC -shared -o librootouch.so rootouch.c \
  -Ijni-include -Ijni-include/linux \
  $(pkg-config --cflags --libs wayland-client)
strip rooterror-pixel-dungeon librootouch.so

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}%{_apphome}/lib
cp -a staging/jre %{buildroot}%{_apphome}/jre
install -m0644 staging/rooterror-pixel-dungeon.jar %{buildroot}%{_apphome}/
install -m0755 librootouch.so %{buildroot}%{_apphome}/lib/
install -D -m0755 rooterror-pixel-dungeon %{buildroot}%{_bindir}/%{name}
install -D -m0644 %{name}.desktop %{buildroot}%{_datadir}/applications/%{name}.desktop
for s in 86 108 128 172 256; do
  install -D -m0644 staging/icons/${s}x${s}/%{name}.png \
    %{buildroot}%{_datadir}/icons/hicolor/${s}x${s}/apps/%{name}.png
done
install -D -m0644 staging/LICENSE %{buildroot}%{_defaultlicensedir}/%{name}/LICENSE
install -D -m0644 NOTICE.md %{buildroot}%{_defaultlicensedir}/%{name}/NOTICE.md

%files
%defattr(-,root,root,-)
%license %{_defaultlicensedir}/%{name}/LICENSE
%license %{_defaultlicensedir}/%{name}/NOTICE.md
%{_bindir}/%{name}
%{_apphome}
%{_datadir}/applications/%{name}.desktop
%{_datadir}/icons/hicolor/*/apps/%{name}.png

%changelog
* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.2.0-1
- Prima versione pubblica di RooTerror Pixel Dungeon: Shattered Pixel Dungeon
  4.0.2 nativo su Sailfish OS, per telefoni a 64 bit (aarch64) e a 32 bit
  (armv7hl).

* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-9
- Anche per i telefoni a 32 bit (armv7hl), con il suo Java ridotto a 32 bit.

* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-8
- Titolo: la R di ROOTERROR ora ha la base aperta e non si confonde più con una B.

* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-7
- Titolo: la R di ROOTERROR ridisegnata con la gamba aperta (sembrava una B).
- Tolto il pulsante Notizie dalla schermata del titolo.
- Durante l'introduzione (la prima partita) si può tornare al menu principale e
  uscire dalla scelta eroe: prima erano bloccati finché non si leggeva la guida.

* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-6
- Titolo nuovo: ROOTERROR PIXEL DUNGEON (scritta ROOTERROR disegnata apposta,
  nello stile del titolo originale).
- Crediti: RooTerror e il porting per Sailfish OS in cima a «Info», con tutti i
  crediti originali di Shattered Pixel Dungeon, Pixel Dungeon e libGDX.

* Fri Nov 20 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-5
- Pallino «indietro» in alto a sinistra, come nelle pagine di Sailfish OS:
  compare quando si può tornare indietro o chiudere una finestra, e diventa
  scuro sugli sfondi chiari e bianco su quelli scuri.

* Thu Nov 19 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-4
- In copertina il gioco si ferma davvero: prima continuava a disegnare 60
  fotogrammi al secondo (circa un quinto di un processore) per colpa del
  controllo dei gamepad, che ora si sospende e riprende al ritorno.

* Thu Nov 19 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-3
- La musica si ferma quando il gioco va in copertina (si può riattivare
  nelle impostazioni del gioco, «musica in sottofondo»).

* Thu Nov 19 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-2
- Volume: i tasti del volume ora regolano anche il gioco (prima il suono
  restava fisso, spesso al massimo).

* Thu Nov 19 2026 RootGPT <166707820+RootGPT-YouTube@users.noreply.github.com> - 0.1.0-1
- Prima versione: Shattered Pixel Dungeon 4.0.2 nativo su Sailfish OS, con il
  suo Java ridotto, OpenGL ES, tocco Wayland, margini per il foro della
  fotocamera e gli angoli arrotondati, pausa quando la finestra va dietro.
