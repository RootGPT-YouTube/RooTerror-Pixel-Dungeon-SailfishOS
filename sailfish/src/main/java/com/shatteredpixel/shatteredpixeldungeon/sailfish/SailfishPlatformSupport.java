/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.sailfish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;
import com.watabou.utils.RectF;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SailfishPlatformSupport extends PlatformSupport {

	//RooTerror: su SailfishOS la finestra ha sempre le dimensioni dello schermo
	// (la mostra il compositore a tutto schermo e i tocchi arrivano in coordinate di schermo):
	// niente dimensioni salvate, niente passaggi finestra/schermo intero
	@Override
	public void updateDisplaySize() { }

	@Override
	public void updateSystemUI() { }

	//RooTerror: margini sicuri dal foro della fotocamera e dagli angoli arrotondati, come su Android.
	// SailfishOS li descrive in dconf (gli stessi che usa Silica per Screen.topCutout e gli angoli):
	//   /desktop/sailfish/silica/cutouts          [[x, y, larghezza, altezza], ...]
	//   /desktop/sailfish/silica/rounded_corners  [[x, y, raggio], ...]
	private static RectF foro = null;
	private static float raggioAngoli = 0;

	private static String dconf( String chiave ){
		try {
			Process p = new ProcessBuilder("dconf", "read", chiave).redirectErrorStream(true).start();
			String s = new String(p.getInputStream().readAllBytes()).trim();
			p.waitFor();
			return s;
		} catch (Exception e) {
			return "";
		}
	}

	private static java.util.ArrayList<int[]> numeri( String s ){
		java.util.ArrayList<int[]> gruppi = new java.util.ArrayList<>();
		Matcher g = Pattern.compile("\\[([0-9,\\s]+)\\]").matcher(s);
		while (g.find()){
			String[] parti = g.group(1).split(",");
			int[] v = new int[parti.length];
			try {
				for (int i = 0; i < parti.length; i++) v[i] = Integer.parseInt(parti[i].trim());
				gruppi.add(v);
			} catch (NumberFormatException ignorata) { }
		}
		return gruppi;
	}

	private static void leggiSchermo(){
		if (foro != null) return;
		foro = new RectF();
		for (int[] c : numeri(dconf("/desktop/sailfish/silica/cutouts"))){
			if (c.length == 4 && c[1] == 0){ //solo i fori attaccati al bordo superiore
				foro = new RectF(c[0], c[1], c[0] + c[2], c[1] + c[3]);
			}
		}
		for (int[] a : numeri(dconf("/desktop/sailfish/silica/rounded_corners"))){
			if (a.length == 3) raggioAngoli = Math.max(raggioAngoli, a[2]);
		}
		System.err.println("[rooterror] foro " + foro.left + "," + foro.top + "-" + foro.right + "," + foro.bottom + ", raggio angoli " + raggioAngoli);
	}

	@Override
	public RectF getSafeInsets( int level ) {
		leggiSchermo();
		RectF margini = new RectF();
		//gli angoli arrotondati tagliano i pulsanti attaccati al bordo: margine come la barra di stato di Silica
		float angolo = raggioAngoli * 0.7f;
		margini.bottom = angolo;
		if (level > INSET_BLK) {
			margini.top = angolo;
			//come su Android: un foro piccolo conta solo per chi chiede tutti i margini
			boolean grande = foro.width() * foro.height() * 133.33f >= Game.width * Game.height;
			if (foro.height() > 0 && (grande || level == INSET_ALL)) {
				margini.top = Math.max(margini.top, foro.bottom);
			}
		}
		return margini;
	}

	@Override
	public RectF getDisplayCutout() {
		leggiSchermo();
		return new RectF(foro);
	}

	@Override
	public boolean connectedToUnmeteredNetwork() {
		return true; //no easy way to check this in desktop, just assume user doesn't care
	}

	@Override
	public boolean supportsVibration() {
		//only supports vibration via controller
		return ControllerHandler.vibrationSupported();
	}

	/* FONT SUPPORT */
	
	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;
	
	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}
		
		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());
		
		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}
	
	private static Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (asianMatcher.reset(input).find()){
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}
	
	//splits on newline (for layout), chinese/japanese (for font choice), and '_'/'**' (for highlighting)
	private Pattern regularsplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	//additionally splits on spaces, so that each word can be laid out individually
	private Pattern regularsplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularsplitterMultiline.split(text);
		} else {
			return regularsplitter.split(text);
		}
	}
}
