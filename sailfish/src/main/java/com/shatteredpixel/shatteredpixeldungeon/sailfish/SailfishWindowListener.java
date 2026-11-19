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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.backends.lwjgl3.RooTouch;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.audio.Music;

//RooTerror: finestra su SailfishOS — tocco Wayland, pausa in secondo piano
public class SailfishWindowListener implements Lwjgl3WindowListener {

	@Override
	public void created ( Lwjgl3Window lwjgl3Window ) {
		RooTouch.init( lwjgl3Window );
	}

	//la finestra è sempre a tutto schermo: niente da ricordare
	@Override
	public void maximized ( boolean b ) { }

	@Override
	public void iconified ( boolean b ) { }

	public void focusLost () {
		if (!SPDSettings.playMusicInBackground()) {
			Music.INSTANCE.pause();
		}
		//in secondo piano non si disegna: la cover mostra l'ultimo fotogramma
		Gdx.graphics.setContinuousRendering(false);
		System.err.println("[rooterror] in secondo piano: pausa");
	}

	public void focusGained () {
		if (!SPDSettings.playMusicInBackground()){
			Music.INSTANCE.resume();
		}
		Gdx.graphics.setContinuousRendering(true);
		System.err.println("[rooterror] in primo piano: ripresa");
	}

	public boolean closeRequested () { return true; }
	public void filesDropped ( String[] strings ) { }
	public void refreshRequested () { }
}
