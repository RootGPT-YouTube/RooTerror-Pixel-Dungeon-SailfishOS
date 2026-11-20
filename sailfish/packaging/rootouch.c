/*
 * RooTerror Pixel Dungeon — libreria nativa del tocco per SailfishOS.
 *
 * GLFW 3.4 (quello dentro LWJGL) su Wayland non gestisce wl_touch.
 * Qui si aggancia un wl_seat nostro sulla STESSA connessione Wayland di GLFW
 * (glfwGetWaylandDisplay) e si chiede il wl_touch. Gli eventi arrivano mentre
 * GLFW svuota la coda predefinita in glfwPollEvents, quindi sul thread principale
 * del gioco: si chiama Java direttamente, che li mette nella coda di libGDX.
 *
 * Licenza: GPLv3 (come il gioco).
 */
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <wayland-client.h>

#define TIPO_DOWN   0
#define TIPO_UP     1
#define TIPO_MOTION 2
#define TIPO_CANCEL 3
#define TIPO_FRAME  4

static JavaVM *jvm;
static jclass classe;
static jmethodID metodo;
static struct wl_seat *seat;
static struct wl_touch *touch;

static void a_java(int tipo, int id, double x, double y)
{
    JNIEnv *env;
    if (!jvm || (*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6) != JNI_OK)
        return;
    (*env)->CallStaticVoidMethod(env, classe, metodo, tipo, id, (jfloat)x, (jfloat)y);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
}

static void t_down(void *d, struct wl_touch *t, uint32_t serial, uint32_t time,
                   struct wl_surface *s, int32_t id, wl_fixed_t x, wl_fixed_t y)
{
    (void)d; (void)t; (void)serial; (void)time; (void)s;
    a_java(TIPO_DOWN, id, wl_fixed_to_double(x), wl_fixed_to_double(y));
}
static void t_up(void *d, struct wl_touch *t, uint32_t serial, uint32_t time, int32_t id)
{
    (void)d; (void)t; (void)serial; (void)time;
    a_java(TIPO_UP, id, 0, 0);
}
static void t_motion(void *d, struct wl_touch *t, uint32_t time, int32_t id,
                     wl_fixed_t x, wl_fixed_t y)
{
    (void)d; (void)t; (void)time;
    a_java(TIPO_MOTION, id, wl_fixed_to_double(x), wl_fixed_to_double(y));
}
static void t_frame(void *d, struct wl_touch *t) { (void)d; (void)t; a_java(TIPO_FRAME, 0, 0, 0); }
static void t_cancel(void *d, struct wl_touch *t) { (void)d; (void)t; a_java(TIPO_CANCEL, 0, 0, 0); }
static void t_shape(void *d, struct wl_touch *t, int32_t id, wl_fixed_t a, wl_fixed_t b)
{ (void)d; (void)t; (void)id; (void)a; (void)b; }
static void t_orient(void *d, struct wl_touch *t, int32_t id, wl_fixed_t o)
{ (void)d; (void)t; (void)id; (void)o; }
static const struct wl_touch_listener touch_listener = {
    t_down, t_up, t_motion, t_frame, t_cancel, t_shape, t_orient
};

static void seat_caps(void *d, struct wl_seat *s, uint32_t caps)
{
    (void)d;
    if ((caps & WL_SEAT_CAPABILITY_TOUCH) && !touch) {
        touch = wl_seat_get_touch(s);
        wl_touch_add_listener(touch, &touch_listener, NULL);
        fprintf(stderr, "[rootouch] wl_touch agganciato\n");
    } else if (!(caps & WL_SEAT_CAPABILITY_TOUCH) && touch) {
        wl_touch_release(touch);
        touch = NULL;
        fprintf(stderr, "[rootouch] wl_touch sparito\n");
    }
}
static void seat_name(void *d, struct wl_seat *s, const char *n) { (void)d; (void)s; (void)n; }
static const struct wl_seat_listener seat_listener = { seat_caps, seat_name };

static void reg_global(void *d, struct wl_registry *r, uint32_t name,
                       const char *iface, uint32_t ver)
{
    (void)d;
    if (!strcmp(iface, "wl_seat") && !seat) {
        seat = wl_registry_bind(r, name, &wl_seat_interface, ver < 5 ? ver : 5);
        wl_seat_add_listener(seat, &seat_listener, NULL);
    }
}
static void reg_remove(void *d, struct wl_registry *r, uint32_t n) { (void)d; (void)r; (void)n; }
static const struct wl_registry_listener reg_listener = { reg_global, reg_remove };

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    (void)reserved;
    jvm = vm;
    return JNI_VERSION_1_6;
}

/* Ritorna 1 se il touch è agganciato, 0 se il seat non ha touch, <0 per errore. */
JNIEXPORT jint JNICALL
Java_com_badlogic_gdx_backends_lwjgl3_RooTouch_nativeInit(JNIEnv *env, jclass cls, jlong display)
{
    struct wl_display *dpy = (struct wl_display *)(intptr_t)display;
    if (!dpy) return -1;
    classe = (*env)->NewGlobalRef(env, cls);
    metodo = (*env)->GetStaticMethodID(env, cls, "onTouch", "(IIFF)V");
    if (!metodo) return -2;
    struct wl_registry *reg = wl_display_get_registry(dpy);
    wl_registry_add_listener(reg, &reg_listener, NULL);
    wl_display_roundtrip(dpy);   /* global → seat */
    wl_display_roundtrip(dpy);   /* capacità del seat → touch */
    return touch ? 1 : (seat ? 0 : -3);
}
