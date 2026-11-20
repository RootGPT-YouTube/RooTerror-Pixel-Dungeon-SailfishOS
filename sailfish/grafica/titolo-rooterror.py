#!/usr/bin/python3
"""RooTerror Pixel Dungeon — scritta ROOTERROR del titolo (disegno originale, GPLv3 come il gioco).

Evan chiede a chi distribuisce una versione modificata di cambiare il titolo: questo script
sostituisce la parola SHATTERED in interfaces/banners.png (versione verticale e orizzontale,
più lo strato delle crepe luminose) con ROOTERROR, disegnata qui lettera per lettera nello
stesso stile (pietra verde-grigia, contorno scuro, luce in alto, ombra in basso, muschio).
PIXEL DUNGEON resta quello del gioco originale.

Uso: sailfish/grafica/titolo-rooterror.py ORIGINALE.png USCITA.png
     (l'originale si recupera con: git show master:core/src/main/assets/interfaces/banners.png)
"""
import sys
from PySide6.QtGui import QImage, QColor

# lettere 19 righe, disegnate a mano ('#' = pietra, 'c' = pietra con crepa)
GLIFI = {
    'R': [
        # occhiello chiuso in alto; sotto, una gamba sottile in diagonale che si stacca sempre di
        # più dall'asta (con la gamba larga e vicina all'asta sembrava una B)
        "##########...",
        "###########..",
        "#####...####.",
        "#####....####",
        "#####....####",
        "#####...####.",
        "###########..",
        "##########...",
        "#####..###...",
        "#####..###...",
        "#####...###..",
        "#####...###..",
        "#####....###.",
        "#####....###.",
        "#####.....###",
        "#####.....###",
        # base APERTA: i piedini di asta e gamba non si allungano l'uno verso l'altro
        # (uniti dal contorno sembravano la pancia chiusa di una B)
        "######....###",
        "######....###",
        "######....###",
    ],
    'O': [
        "...#######...",
        ".###########.",
        "#############",
        "#####...#####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "####.....####",
        "#####...#####",
        "#############",
        ".###########.",
        "...#######...",
    ],
    'T': [
        "#############",
        "#############",
        "#############",
        "##..#####..##",
        "#...#####...#",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "....#####....",
        "..#########..",
        "..#########..",
        "..#########..",
    ],
    'E': [
        "############",
        "############",
        "############",
        "#####.....##",
        "#####......#",
        "#####.......",
        "#####.......",
        "#####...#...",
        "#########...",
        "#########...",
        "#########...",
        "#####...#...",
        "#####.......",
        "#####.......",
        "#####......#",
        "#####.....##",
        "############",
        "############",
        "############",
    ],
}

# crepe (come in SHATTERED): (indice lettera, [(x, y)...]) — scure sulla pietra e luminose nello strato «glow»
CREPE = {
    1: [(1, 6), (2, 7), (2, 8), (1, 9), (2, 10), (3, 11)],               # prima O
    3: [(6, 1), (6, 2), (5, 3), (6, 4), (7, 5), (6, 6)],                 # T
    5: [(2, 11), (3, 12), (2, 13), (1, 14), (2, 15)],                    # R
    7: [(9, 4), (10, 5), (9, 6), (10, 7), (11, 8), (10, 9), (9, 10)],    # ultima O
    8: [(7, 1), (8, 2), (9, 2), (10, 3)],                                # ultima R
}

PIETRA = QColor(0x84, 0x8e, 0x6f)
PIETRA_MEDIA = QColor(0x7b, 0x84, 0x65)
PIETRA_BASSA = QColor(0x6c, 0x76, 0x5a)
LUCE = QColor(0xd4, 0xdb, 0xc8)
LUCE_LATO = QColor(0xb1, 0xba, 0xa2)
OMBRA = QColor(0x65, 0x6e, 0x53)
OMBRA_LATO = QColor(0x6c, 0x76, 0x5a)
CREPA = QColor(0x46, 0x4e, 0x35)
CONTORNO = QColor(0x1e, 0x21, 0x15)
NERO = QColor(0, 0, 0)
MUSCHIO = QColor(0x6f, 0xa2, 0x68)
GLOW = QColor(48, 147, 65, 255)
ALONE = QColor(48, 147, 65, 51)

PAROLA = "ROOTERROR"
SPAZIO = 2
ALTEZZA = 19


def maschera():
    """matrice dei pixel della parola: None, 'p' (pietra), 'c' (crepa)"""
    w = sum(len(GLIFI[c][0]) for c in PAROLA) + SPAZIO * (len(PAROLA) - 1)
    m = [[None] * w for _ in range(ALTEZZA)]
    x0 = 0
    for i, c in enumerate(PAROLA):
        g = GLIFI[c]
        for y, riga in enumerate(g):
            for x, ch in enumerate(riga):
                if ch != '.':
                    m[y][x0 + x] = 'p'
        for (cx, cy) in CREPE.get(i, []):
            if m[cy][x0 + cx] is not None:
                m[cy][x0 + cx] = 'c'
        x0 += len(g[0]) + SPAZIO
    return m, w


def dipingi(img, glow, ox, oy, gx):
    """disegna la parola con l'angolo in alto a sinistra delle lettere in (ox, oy);
    lo strato delle crepe luminose va in (gx, oy)"""
    m, w = maschera()
    pieno = lambda x, y: 0 <= y < ALTEZZA and 0 <= x < w and m[y][x] is not None
    # ombra nera in basso a destra, poi contorno scuro tutto intorno
    for y in range(-1, ALTEZZA + 2):
        for x in range(-1, w + 2):
            if pieno(x, y):
                continue
            if pieno(x - 1, y - 1) or pieno(x, y - 1) and pieno(x - 1, y):
                img.setPixelColor(ox + x, oy + y, NERO)
            if any(pieno(x + dx, y + dy) for dx in (-1, 0, 1) for dy in (-1, 0, 1)):
                img.setPixelColor(ox + x, oy + y, CONTORNO)
    for y in range(ALTEZZA):
        for x in range(w):
            v = m[y][x]
            if v is None:
                continue
            if v == 'c':
                col = CREPA
            elif not pieno(x, y - 1):
                col = LUCE
            elif not pieno(x, y + 1):
                # base delle lettere: un po' di muschio, a caso ma sempre uguale
                col = MUSCHIO if (x * 7 + y * 3) % 5 == 0 else OMBRA
            elif not pieno(x - 1, y):
                col = LUCE_LATO
            elif not pieno(x + 1, y):
                col = OMBRA_LATO
            else:
                # la pietra scurisce verso il basso, come in PIXEL DUNGEON
                col = PIETRA if y < 8 else PIETRA_MEDIA if y < 13 else PIETRA_BASSA
            img.setPixelColor(ox + x, oy + y, col)
            if v == 'c':
                glow.setPixelColor(gx + x, oy + y, GLOW)
                for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                    q = glow.pixelColor(gx + x + dx, oy + y + dy)
                    if q.alpha() == 0:
                        glow.setPixelColor(gx + x + dx, oy + y + dy, ALONE)
    return w


def svuota(img, x0, y0, x1, y1):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img.setPixelColor(x, y, QColor(0, 0, 0, 0))


def main():
    src, dst = sys.argv[1], sys.argv[2]
    img = QImage(src).convertToFormat(QImage.Format_RGBA8888)
    _, w = maschera()
    # verticale: titolo in x 0..138, crepe in x 139..277; lettere nelle righe 15..33
    svuota(img, 0, 11, 138, 36)
    svuota(img, 139, 11, 277, 36)
    ox = (139 - w) // 2
    dipingi(img, img, ox, 15, 139 + ox)
    # orizzontale: titolo in x 0..239, crepe in x 240..479; SHATTERED era in x 53..187, righe 102..120
    svuota(img, 50, 100, 190, 122)
    svuota(img, 290, 100, 430, 122)
    ox = 120 - w // 2
    dipingi(img, img, ox, 102, 240 + ox)
    img.save(dst)
    print("ROOTERROR larga", w, "px ->", dst)


if __name__ == "__main__":
    main()
