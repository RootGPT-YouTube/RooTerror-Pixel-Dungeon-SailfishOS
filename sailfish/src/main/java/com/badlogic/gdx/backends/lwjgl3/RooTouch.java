/*
 * RooTerror Pixel Dungeon — tocco su SailfishOS (Wayland).
 * GPLv3, come il gioco.
 *
 * Sta nel pacchetto del backend LWJGL3 di libGDX per poter usare la coda
 * degli eventi (package-private) di DefaultLwjgl3Input: i tocchi arrivano
 * al gioco nello stesso modo e nello stesso ordine del mouse.
 */
package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWayland;

public final class RooTouch {

	private static final int TIPO_DOWN = 0, TIPO_UP = 1, TIPO_MOTION = 2, TIPO_CANCEL = 3;
	private static final int DITA = 10;

	/** id Wayland del dito per ogni pointer di libGDX; -1 = libero */
	private static final int[] idDito = new int[DITA];
	private static final int[] ultimoX = new int[DITA];
	private static final int[] ultimoY = new int[DITA];
	private static DefaultLwjgl3Input input;
	private static long finestra;
	private static int registrati;

	private RooTouch() {}

	private static native int nativeInit(long display);

	/** Da chiamare in Lwjgl3WindowListener.created(). */
	public static void init(Lwjgl3Window window) {
		java.util.Arrays.fill(idDito, -1);
		finestra = window.getWindowHandle();
		if (!(window.getInput() instanceof DefaultLwjgl3Input)) {
			System.err.println("[rootouch] input inatteso: " + window.getInput());
			return;
		}
		input = (DefaultLwjgl3Input) window.getInput();
		long display;
		try {
			display = GLFWNativeWayland.glfwGetWaylandDisplay();
		} catch (Throwable t) {
			System.err.println("[rootouch] niente display Wayland: " + t);
			return;
		}
		try {
			System.loadLibrary("rootouch");
		} catch (UnsatisfiedLinkError e) {
			System.err.println("[rootouch] librerie non trovate: " + e.getMessage());
			return;
		}
		int esito = nativeInit(display);
		System.err.println("[rootouch] init = " + esito);
		logDimensioni("all'avvio");
	}

	public static void logDimensioni(String quando) {
		int[] w = new int[1], h = new int[1], fw = new int[1], fh = new int[1];
		GLFW.glfwGetWindowSize(finestra, w, h);
		GLFW.glfwGetFramebufferSize(finestra, fw, fh);
		System.err.println("[rootouch] dimensioni " + quando + ": finestra " + w[0] + "x" + h[0]
				+ ", framebuffer " + fw[0] + "x" + fh[0]
				+ (Gdx.graphics != null ? ", gdx " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight()
				+ " back " + Gdx.graphics.getBackBufferWidth() + "x" + Gdx.graphics.getBackBufferHeight() : ""));
	}

	private static int pointerDi(int id) {
		for (int i = 0; i < DITA; i++) if (idDito[i] == id) return i;
		return -1;
	}

	/** Chiamato dalla libreria nativa, sul thread principale, dentro glfwPollEvents. */
	static void onTouch(int tipo, int id, float x, float y) {
		if (input == null) return;
		long t = System.nanoTime();
		if (tipo != TIPO_MOTION && tipo != 4 && registrati < 40) {
			registrati++;
			System.err.println("[rootouch] tocco tipo=" + tipo + " id=" + id + " x=" + x + " y=" + y);
		}
		switch (tipo) {
			case TIPO_DOWN: {
				int p = pointerDi(-1);
				if (p < 0) return;
				idDito[p] = id;
				ultimoX[p] = (int) x; ultimoY[p] = (int) y;
				input.eventQueue.touchDown((int) x, (int) y, p, Input.Buttons.LEFT, t);
				break;
			}
			case TIPO_MOTION: {
				int p = pointerDi(id);
				if (p < 0) return;
				ultimoX[p] = (int) x; ultimoY[p] = (int) y;
				input.eventQueue.touchDragged((int) x, (int) y, p, t);
				break;
			}
			case TIPO_UP: {
				int p = pointerDi(id);
				if (p < 0) return;
				input.eventQueue.touchUp(ultimoX[p], ultimoY[p], p, Input.Buttons.LEFT, t);
				idDito[p] = -1;
				break;
			}
			case TIPO_CANCEL: {
				for (int p = 0; p < DITA; p++) {
					if (idDito[p] != -1) {
						//la coda di libGDX non ha touchCancelled: un tocco annullato si chiude come rilasciato
						input.eventQueue.touchUp(ultimoX[p], ultimoY[p], p, Input.Buttons.LEFT, t);
						idDito[p] = -1;
					}
				}
				break;
			}
			default:
				break;
		}
	}
}
