#!/usr/bin/python3
"""RooTerror Pixel Dungeon — avatar 16x16 per i crediti (il teschio dell'icona, disegno originale, GPLv3).
Uso: sailfish/grafica/avatar-rooterror.py USCITA.png"""
import sys
from PySide6.QtGui import QImage, QColor

TESCHIO = [
    "...XXXXXX...",
    ".XXXXXXXXXX.",
    "XXXXXXXXXXXX",
    "XXXXXXXXXXXX",
    "XX...XX...XX",
    "XX...XX...XX",
    "XX...XX...XX",
    "XXXXXXXXXXXX",
    "XXXXX..XXXXX",
    ".XXXXXXXXXX.",
    "..XX.XX.XX..",
    "..XXXXXXXX..",
]
OSSO = QColor(0xe8, 0xe2, 0xcc)
OSSO_OMBRA = QColor(0xb8, 0xb0, 0x96)
CONTORNO = QColor(0x1e, 0x21, 0x15)
GHIACCIO = QColor(0x66, 0xcc, 0xff)  # occhi azzurri, il colore di RooTundra

img = QImage(16, 16, QImage.Format_RGBA8888)
img.fill(QColor(0, 0, 0, 0))
ox, oy = 2, 2
pieno = lambda x, y: 0 <= y < 12 and 0 <= x < 12 and TESCHIO[y][x] == "X"
for y in range(-1, 13):
    for x in range(-1, 13):
        if pieno(x, y):
            img.setPixelColor(ox + x, oy + y, OSSO_OMBRA if not pieno(x, y + 1) else OSSO)
        elif any(pieno(x + dx, y + dy) for dx in (-1, 0, 1) for dy in (-1, 0, 1)):
            # al centro delle orbite un punto di luce azzurra
            img.setPixelColor(ox + x, oy + y, GHIACCIO if y == 5 and x in (3, 8) else CONTORNO)
img.save(sys.argv[1])
