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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

//RooTerror Pixel Dungeon: avvio su SailfishOS (libGDX LWJGL3, GLES2 via EGL, Wayland)
public class SailfishLauncher {

	//cartella dati secondo la convenzione di Sailjail: ~/.local/share/<OrganizationName>/<ApplicationName>/
	static final String ORGANIZZAZIONE = "com.github.RootGPT_YouTube";
	static final String APPLICAZIONE = "rooterror";

	public static void main (String[] args) {

		final String title;
		if (SailfishLauncher.class.getPackage().getSpecificationTitle() == null){
			title = System.getProperty("Specification-Title", "RooTerror Pixel Dungeon");
		} else {
			title = SailfishLauncher.class.getPackage().getSpecificationTitle();
		}

		String dati = System.getenv("XDG_DATA_HOME");
		if (dati == null || dati.isEmpty()) dati = System.getProperty("user.home") + "/.local/share";
		final String basePath = dati + "/" + ORGANIZZAZIONE + "/" + APPLICAZIONE + "/";
		new File(basePath).mkdirs();

		//niente finestre di dialogo sul telefono: il resoconto va su stderr e in crash.txt
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Game.reportException(throwable);
				StringWriter sw = new StringWriter();
				throwable.printStackTrace(new PrintWriter(sw));
				String resoconto = title + " " + Game.version + " — "
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n" + sw;
				System.err.println(resoconto);
				try (FileWriter w = new FileWriter(basePath + "crash.txt", true)) {
					w.write(resoconto + "\n");
				} catch (Exception ignorata) { }
				System.exit(1);
			}
		});

		Game.version = SailfishLauncher.class.getPackage().getSpecificationVersion();
		if (Game.version == null) {
			Game.version = System.getProperty("Specification-Version", "INDEV");
		}

		try {
			Game.versionCode = Integer.parseInt(SailfishLauncher.class.getPackage().getImplementationVersion());
		} catch (NumberFormatException e) {
			Game.versionCode = Integer.parseInt(System.getProperty("Implementation-Version", "0"));
		}

		//controllo aggiornamenti e notizie di Shattered spenti (docs/recommended-changes.md)
		if (UpdateImpl.supportsUpdates()){
			Updates.service = UpdateImpl.getUpdateService();
		}
		if (NewsImpl.supportsNews()){
			News.service = NewsImpl.getNewsService();
		}

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle( title );

		Files.FileType baseFileType = Files.FileType.Absolute;
		config.setPreferencesConfig( basePath, baseFileType );
		SPDSettings.set( new Lwjgl3Preferences( new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, baseFileType) ));
		FileUtils.setDefaultFileProperties( baseFileType, basePath );

		//GLES2 via EGL (Mali/libhybris) invece dell'OpenGL desktop: è la modalità ANGLE di libGDX,
		// ma senza ANGLELoader le librerie GLES/EGL sono quelle di sistema
		config.setOpenGLEmulation( Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0 );

		//la finestra nasce grande quanto lo schermo: il compositore la mostra a tutto schermo
		// e manda i tocchi in coordinate di schermo
		Graphics.DisplayMode schermo = Lwjgl3ApplicationConfiguration.getDisplayMode();
		config.setWindowSizeLimits( -1, -1, -1, -1 );
		config.setWindowedMode( schermo.width, schermo.height );
		config.setMaximized( false );
		//niente bordi di ripiego di GLFW
		config.setDecorated( false );
		//in secondo piano (cover, altra app, blocco schermo): pausa del gioco, che salva come su Android
		config.setPauseWhenLostFocus( true );
		//senza finestre da disegnare il ciclo di libGDX dorme 1000/idleFPS ms
		config.setIdleFPS( 4 );
		System.err.println("[rooterror] " + title + " " + Game.version + ", finestra " + schermo.width + "x" + schermo.height
				+ ", dati in " + basePath);

		config.setWindowListener( new SailfishWindowListener() );

		new Lwjgl3Application(new ShatteredPixelDungeon(new SailfishPlatformSupport()), config);
	}
}
