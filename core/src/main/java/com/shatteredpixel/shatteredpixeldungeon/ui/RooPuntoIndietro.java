/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * RooTerror Pixel Dungeon (porting per Sailfish OS)
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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;

import java.nio.ByteBuffer;

//RooTerror: il pallino «indietro» in alto a sinistra, come le pagine di Sailfish OS.
// Su Sailfish non c'è il tasto Indietro di Android né l'Esc del PC: toccando il pallino il
// gioco riceve un vero Indietro (lo intercetta RooTouch, prima di qualsiasi altro tocco).
// Si vede solo quando Indietro serve a qualcosa e cambia colore secondo lo sfondo sotto di lui.
public class RooPuntoIndietro extends Image {

	//lo accende il lanciatore di Sailfish: sulle altre piattaforme il pallino non esiste
	public static boolean attivo = false;

	//letti da RooTouch, sullo stesso thread: c'è il pallino, e la sua zona di tocco in pixel di schermo
	public static boolean visibile = false;
	public static final float ZONA_TOCCO_PX = 150f;

	private static final int TEX = 64;
	private static final float DIAMETRO_PX = 22f;
	private static final float CENTRO_PX = 50f;

	//grigio del pallino: 1 = bianco (sfondo scuro), basso = scuro (sfondo chiaro)
	private static final float CHIARO = 1f;
	private static final float SCURO = 0.13f;
	private float grigio = CHIARO;
	private float grigioMira = CHIARO;

	private static final int LATO_CAMPIONE = 8;
	private final ByteBuffer pixel = BufferUtils.newByteBuffer(LATO_CAMPIONE * LATO_CAMPIONE * 4);
	private int fotogrammi = 0;

	public RooPuntoIndietro(){
		super(texture());
		camera = PixelScene.uiCamera;
		posiziona();
	}

	private void posiziona(){
		Camera c = camera;
		float z = c.zoom;
		scale.set(DIAMETRO_PX / TEX / z);
		x = (CENTRO_PX - DIAMETRO_PX / 2f - c.x) / z;
		y = (CENTRO_PX - DIAMETRO_PX / 2f - c.y) / z;
	}

	//cerchio bianco con il bordo sfumato, disegnato una volta sola (e di nuovo se la cache si svuota)
	private static SmartTexture texture(){
		SmartTexture tx = TextureCache.create(RooPuntoIndietro.class, TEX, TEX);
		Pixmap pm = tx.bitmap;
		if ((pm.getPixel(TEX / 2, TEX / 2) & 0xFF) == 0){
			float c = (TEX - 1) / 2f;
			float r = TEX / 2f - 2f;
			for (int py = 0; py < TEX; py++){
				for (int px = 0; px < TEX; px++){
					float d = (float)Math.hypot(px - c, py - c);
					float a = Math.max(0f, Math.min(1f, r + 0.5f - d));
					pm.drawPixel(px, py, 0xFFFFFF00 | Math.round(a * 255));
				}
			}
			tx.bitmap(pm);
		}
		return tx;
	}

	@Override
	public void update() {
		super.update();
		Scene s = Game.scene();
		boolean vis = attivo && s instanceof PixelScene && ((PixelScene) s).puoTornareIndietro();
		visible = vis;
		visibile = vis;
		if (camera != PixelScene.uiCamera){
			camera = PixelScene.uiCamera;
			posiziona();
		}
		grigio += (grigioMira - grigio) * Math.min(1f, Game.elapsed * 8f);
		rm = gm = bm = grigio;
		am = 0.9f;
	}

	@Override
	public void draw() {
		//il pallino è l'ultimo a essere disegnato: sotto c'è già tutta la scena.
		// Sei letture al secondo bastano, e costano poco (8x8 pixel)
		if (fotogrammi++ % 10 == 0) campiona();
		super.draw();
	}

	private void campiona(){
		int lato = LATO_CAMPIONE;
		int px = Math.round(CENTRO_PX - lato / 2f);
		//glReadPixels conta dal basso
		int py = Game.height - Math.round(CENTRO_PX + lato / 2f);
		pixel.clear();
		Gdx.gl.glReadPixels(px, py, lato, lato, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixel);
		float somma = 0;
		for (int i = 0; i < lato * lato; i++){
			int r = pixel.get(i * 4) & 0xFF;
			int g = pixel.get(i * 4 + 1) & 0xFF;
			int b = pixel.get(i * 4 + 2) & 0xFF;
			somma += 0.2126f * r + 0.7152f * g + 0.0722f * b;
		}
		float luce = somma / (lato * lato * 255f);
		//isteresi: il pallino non sfarfalla su uno sfondo a metà
		if (luce > 0.6f) grigioMira = SCURO;
		else if (luce < 0.45f) grigioMira = CHIARO;
	}
}
