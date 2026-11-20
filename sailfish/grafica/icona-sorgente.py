#!/usr/bin/env python3
# RooTerror Pixel Dungeon — disegno sorgente dell'icona (bianco su nero), da incidere
# sul disco d'acciaio di RooTundra con RooTundra/grafica/mieapp.py.
# Disegno originale: teschio in pixel-art sotto l'arco di una porta di sotterraneo.
# Uso: QT_QPA_PLATFORM=offscreen python3 -I icona-sorgente.py <uscita.png>
import sys
from PySide6.QtGui import QGuiApplication, QImage, QPainter, QColor, QPen, QPainterPath
from PySide6.QtCore import Qt, QRectF

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

def main():
    app = QGuiApplication(sys.argv)
    lato = 512
    img = QImage(lato, lato, QImage.Format_ARGB32)
    img.fill(QColor(0, 0, 0))
    p = QPainter(img)
    p.setRenderHint(QPainter.Antialiasing, True)
    bianco = QColor(255, 255, 255)

    # arco della porta: due stipiti e una volta a tutto sesto
    pen = QPen(bianco, 18)
    pen.setCapStyle(Qt.FlatCap)
    pen.setJoinStyle(Qt.MiterJoin)
    p.setPen(pen)
    p.setBrush(Qt.NoBrush)
    x0, x1, base, imposta = 136, 376, 392, 236
    arco = QPainterPath()
    arco.moveTo(x0, base)
    arco.lineTo(x0, imposta)
    arco.arcTo(QRectF(x0, imposta - (x1 - x0) / 2, x1 - x0, x1 - x0), 180, -180)
    arco.lineTo(x1, base)
    p.drawPath(arco)
    # soglia
    p.drawLine(112, base, 400, base)

    # teschio in pixel-art, al centro della porta (pixel netti, senza antialias)
    p.setRenderHint(QPainter.Antialiasing, False)
    p.setPen(Qt.NoPen)
    p.setBrush(bianco)
    px = 13
    w = len(TESCHIO[0]) * px
    ox, oy = (lato - w) // 2, 214
    for r, riga in enumerate(TESCHIO):
        for c, ch in enumerate(riga):
            if ch == "X":
                p.drawRect(ox + c * px, oy + r * px, px, px)
    p.end()
    img.save(sys.argv[1])

if __name__ == "__main__":
    main()
