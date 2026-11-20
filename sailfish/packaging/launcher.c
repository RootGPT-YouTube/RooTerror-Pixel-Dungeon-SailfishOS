/*
 * RooTerror Pixel Dungeon — launcher ELF per SailfishOS.
 *
 * Perché non uno script né "java -jar":
 *  - sailjail rifiuta gli script e vuole come Exec un ELF in /usr/bin;
 *  - lipstick aggancia finestra e cover a un processo la cui cmdline combacia con la
 *    riga Exec del .desktop: con "java -jar ..." la cmdline sarebbe quella di java.
 * Quindi questo programma carica la JVM al suo interno (API d'invocazione JNI, come fa
 * il comando java stesso) e chiama SailfishLauncher.main: il processo resta
 * /usr/bin/rooterror-pixel-dungeon, senza argomenti.
 *
 * Audio: OpenAL apre un flusso PulseAudio che la politica di Sailfish (ohm) mette nel
 * gruppo «othermedia», a volume fisso e fuori dai tasti del volume. Prima di avviare la
 * JVM il lanciatore prende una risorsa audio di tipo gioco (libaudioresource, come SDL):
 * ohm mette allora i flussi di questo processo nel gruppo «game», che i tasti regolano.
 *
 * Si compila con:
 *   gcc -O2 -DAPP_HOME='"/home/rooterror"' -o rooterror-pixel-dungeon launcher.c \
 *       $(pkg-config --cflags --libs audioresource glib-2.0) -ldl -lpthread
 *
 * Diagnostica sul campo (come RooTree): se esiste ~/.config/rootgpt/rooterror.debug,
 * stdout e stderr vanno in /tmp/rooterror.log.
 *
 * Licenza: GPLv3 (come il gioco).
 */
#include <jni.h>
#include <dlfcn.h>
#include <limits.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#include <audioresource.h>
#include <glib.h>

#ifndef APP_HOME
#error "definire APP_HOME (cartella dell'app, es. /home/rooterror)"
#endif

#define CLASSE_MAIN "com/shatteredpixel/shatteredpixeldungeon/sailfish/SailfishLauncher"
#define APP_ID "rooterror"

typedef jint (JNICALL *CreaJVM)(JavaVM **, void **, void *);

static int esito = 0;

/* ---- risorsa audio (vive in un thread col ciclo GLib, che libaudioresource richiede) ---- */

static GMutex audio_mutex;
static GCond audio_cond;
static int audio_risposta = 0; /* 0 = in attesa, 1 = concessa, -1 = negata */

static void audio_cb(audioresource_t *res, bool acquisita, void *dati)
{
    (void)res; (void)dati;
    fprintf(stderr, "[launcher] risorsa audio %s\n", acquisita ? "concessa" : "persa o negata");
    g_mutex_lock(&audio_mutex);
    audio_risposta = acquisita ? 1 : -1;
    g_cond_broadcast(&audio_cond);
    g_mutex_unlock(&audio_mutex);
}

static void *ciclo_audio(void *arg)
{
    (void)arg;
    /* contesto predefinito: libresource vi aggancia la connessione D-Bus; nessun altro lo usa */
    GMainLoop *loop = g_main_loop_new(NULL, FALSE);
    audioresource_t *res = audioresource_init(AUDIO_RESOURCE_GAME, audio_cb, NULL);
    if (res)
        audioresource_acquire(res);
    else
        fprintf(stderr, "[launcher] libaudioresource non inizializzata\n");
    g_main_loop_run(loop); /* resta acceso finché vive il processo: la risorsa si libera all'uscita */
    return NULL;
}

static void prendi_audio(void)
{
    pthread_t t;
    if (pthread_create(&t, NULL, ciclo_audio, NULL) != 0)
        return;
    pthread_detach(t);
    /* aspetta la risposta di ohm (di solito pochi ms) perché OpenAL apra il flusso dopo */
    gint64 fine = g_get_monotonic_time() + 2 * G_TIME_SPAN_SECOND;
    g_mutex_lock(&audio_mutex);
    while (audio_risposta == 0)
        if (!g_cond_wait_until(&audio_cond, &audio_mutex, fine))
            break;
    if (audio_risposta == 0)
        fprintf(stderr, "[launcher] nessuna risposta per la risorsa audio: avvio lo stesso\n");
    g_mutex_unlock(&audio_mutex);
}

static int esiste(const char *p)
{
    struct stat st;
    return stat(p, &st) == 0;
}

static void ambiente(void)
{
    const char *home = getenv("HOME");
    char buf[PATH_MAX];

    snprintf(buf, sizeof(buf), "%s/.config/rootgpt/" APP_ID ".debug", home ? home : "");
    if (home && access(buf, F_OK) == 0
        && freopen("/tmp/" APP_ID ".log", "w", stdout)
        && freopen("/tmp/" APP_ID ".log", "a", stderr)) {
        setvbuf(stdout, NULL, _IOLBF, 0);
        setvbuf(stderr, NULL, _IOLBF, 0);
        fprintf(stderr, "[launcher] diagnostica attiva (%s)\n", buf);
    }

    const char *xrd = getenv("XDG_RUNTIME_DIR");
    if (!xrd || !*xrd) {
        snprintf(buf, sizeof(buf), "/run/user/%u", (unsigned)getuid());
        setenv("XDG_RUNTIME_DIR", buf, 1);
        xrd = getenv("XDG_RUNTIME_DIR");
    }
    /* il socket del compositore: in XDG_RUNTIME_DIR se c'è, altrimenti quello di sistema */
    if (!getenv("WAYLAND_DISPLAY") || !*getenv("WAYLAND_DISPLAY")) {
        snprintf(buf, sizeof(buf), "%s/wayland-0", xrd);
        if (esiste(buf))
            setenv("WAYLAND_DISPLAY", "wayland-0", 1);
        else if (esiste("/run/display/wayland-0"))
            setenv("WAYLAND_DISPLAY", "/run/display/wayland-0", 1);
    }
    /* libhybris: piattaforma EGL Wayland */
    setenv("EGL_PLATFORM", "wayland", 0);
    if (!getenv("DBUS_SESSION_BUS_ADDRESS")) {
        snprintf(buf, sizeof(buf), "unix:path=%s/dbus/user_bus_socket", xrd);
        setenv("DBUS_SESSION_BUS_ADDRESS", buf, 1);
    }
    /* GLFW su Wayland non deve cercare X11 */
    unsetenv("DISPLAY");
}

static void *gioco(void *arg)
{
    (void)arg;
    void *jvm_lib = dlopen(APP_HOME "/jre/lib/server/libjvm.so", RTLD_NOW | RTLD_GLOBAL);
    if (!jvm_lib) {
        fprintf(stderr, "[launcher] JVM non trovata: %s\n", dlerror());
        esito = 127;
        return NULL;
    }
    CreaJVM crea = (CreaJVM)dlsym(jvm_lib, "JNI_CreateJavaVM");
    if (!crea) {
        fprintf(stderr, "[launcher] JNI_CreateJavaVM mancante\n");
        esito = 127;
        return NULL;
    }

    JavaVMOption opz[] = {
        { "-Djava.class.path=" APP_HOME "/rooterror-pixel-dungeon.jar", NULL },
        { "-Djava.library.path=" APP_HOME "/lib", NULL },
        /* il gioco su Android vive in poche centinaia di MB: un tetto basso tiene la RAM del telefono libera */
        { "-Xmx512m", NULL },
        { "-XX:+UseSerialGC", NULL },
        { "-Dfile.encoding=UTF-8", NULL },
        { "-Djava.awt.headless=true", NULL },
        { "-Dsun.java.command=" CLASSE_MAIN, NULL },
    };
    JavaVMInitArgs args;
    args.version = JNI_VERSION_10;
    args.nOptions = sizeof(opz) / sizeof(opz[0]);
    args.options = opz;
    args.ignoreUnrecognized = JNI_FALSE;

    JavaVM *vm;
    JNIEnv *env;
    if (crea(&vm, (void **)&env, &args) != JNI_OK) {
        fprintf(stderr, "[launcher] creazione della JVM fallita\n");
        esito = 1;
        return NULL;
    }

    jclass cls = (*env)->FindClass(env, CLASSE_MAIN);
    jmethodID main_id = cls ? (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V") : NULL;
    if (!main_id) {
        (*env)->ExceptionDescribe(env);
        esito = 1;
    } else {
        jobjectArray argv = (*env)->NewObjectArray(env, 0, (*env)->FindClass(env, "java/lang/String"), NULL);
        (*env)->CallStaticVoidMethod(env, cls, main_id, argv);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);
            esito = 1;
        }
    }
    /* aspetta i thread non demoni, come il comando java */
    (*vm)->DestroyJavaVM(vm);
    return NULL;
}

int main(void)
{
    ambiente();
    prendi_audio();
    /* la JVM gira in un thread suo con una pila ampia, come fa il comando java:
     * il thread iniziale ha una pila che la JVM non sa proteggere bene */
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    pthread_attr_setstacksize(&attr, 8 * 1024 * 1024);
    pthread_t t;
    if (pthread_create(&t, &attr, gioco, NULL) != 0) {
        perror("pthread_create");
        return 1;
    }
    pthread_join(t, NULL);
    return esito;
}
