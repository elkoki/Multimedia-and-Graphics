package com.elkoki.multimediaandgraphics;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Objetos de las clases SoundPool y MediaPlayer
    private SoundPool sPool;
    private MediaPlayer mPlayer;
    // Guarda los IDs de sonidos que se deben reproducir por SoundPool
    private int soundID1=-1, soundID2=-1;
    // Vistas de la Actividad
    private TextView logTextView = (TextView) findViewById(R.id.Log);
    private ScrollView scrollview = ((ScrollView) findViewById(R.id.ScrollView));
    // Grabador de audio
    private MediaRecorder recorder;
    // Fichero donde guardamos el audio
    private File audiofile = null;
    // Botones de la Actividad
    private Button  boton_spool, boton_spool1, boton_spool2;
    private Button boton_mplayer;
    private Button boton_mrecorder;
    private Button boton_exit;
    private Object Aula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), this::onApplyWindowInsets);
    }

    private WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;

        // Localizamos las Vistas del layout
        logTextView = (TextView) findViewById(R.id.Log);
        scrollview = ((ScrollView) findViewById(R.id.ScrollView));
        // Establecemos el tipo de flujo de audio que deseamos
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Cargamos el tono con SoundPool indicando el tipo de flujo
        // STREAM_MUSIC
        sPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        // Cuando la carga del archivo con SoundPool se completa...
        sPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                // Mostramos un log
                log("Tono " + sampleId + " cargado con SoundPool");
            }
        });

        // Cargamos los archivos para SoundPool y guardamos su ID para
        // poder reproducirlo
        soundID1 = sPool.load(this, R.raw.por_amar_a_ciegas, 1);
        soundID2 = sPool.load(this, R.raw.por_amar_a_ciegas, 1);
        // Definimos el mismo evento onClick de los botones SoundPool y
        // los distinguimos por su propiedad Tag
        View.OnClickListener click = new View.OnClickListener() {
            public void onClick(View v) {
                // Obtenemos acceso al gestor de Audio para obtener
                // información
                AudioManager audioManager =
                        (AudioManager)getSystemService(AUDIO_SERVICE);
                // Buscamos el volumen establecido para el tipo
                // STREAM_MUSIC
                float volumenMusica = (float) audioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                // Obtenemos el vólumen máx para el tipo STREAM_MUSIC
                float volumeMusicaMax = (float) audioManager
                        .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                // Vamos a reducir el volumen del sonido
                float volumen = volumenMusica / volumeMusicaMax;
                // ¿Qué botón se ha pulsado? ¿Se ha cargado el tono?
                if (v.getTag().toString().equals("1") && soundID1>-1)
                    // Reproducimos el sonido 1
                    sPool.play(soundID1, volumen, volumen, 1, 0, 1f);
                else
                if (v.getTag().toString().equals("2") && soundID2>-1)
                    // Reproducimos el sonido 2
                    sPool.play(soundID2, volumen, volumen, 1, 0, 1f);
            }
        }; // end onClick botón
        // Buscamos los botones de SoundPool y asociamos su evento
        // onClick
        boton_spool1 = (Button) findViewById(R.id.soundpool1);
        boton_spool2 = (Button) findViewById(R.id.soundpool2);
        boton_spool1.setOnClickListener(click);
        boton_spool2.setOnClickListener(click);
        // Buscamos el botón que reproduce MediaPlayer y definimos su
        // evento onClick
        boton_mplayer = (Button) findViewById(R.id.mediaplayer);
        boton_mplayer.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
             // Si ya estamos reproduciendo un sonido, lo paramos
             if (mPlayer!=null  && mPlayer.isPlaying()) {
                 mPlayer.stop();
                 // Cambiamos los botones y mostramos log
                 boton_mplayer.setText("Reproducir Audio con Mediaplayer");
                 boton_spool.setEnabled(true);
                 boton_mrecorder.setEnabled(true);
                 log("Cancelada reproducción MediaPlayer");
             } else // Si no, iniciamos la reproducción
             {
                 // Cambiamos los botones y hacemos log
                 boton_mplayer.setText("Cancelar");
                 boton_spool.setEnabled(false);
                 boton_mrecorder.setEnabled(false);
                 log("Reproduciendo Audio con MediaPlayer");
                 // Creamos el objeto MediaPlayer asociándole la canción
                 mPlayer = MediaPlayer.create(MainActivity.this,
                         R.raw.por_amar_a_ciegas);
                 // Iniciamos la reproducción
                 mPlayer.start();
                 // Definimos el listener que se lanza cuando la canción
                 // acaba
                 mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                     public void onCompletion(MediaPlayer arg0) {
                         // Hacemos log y cambiamos botones
                         log("Fin Reproducción MediaPlayer");
                         boton_spool.setEnabled(true);
                         boton_mrecorder.setEnabled(true);
                     }
                 }); // end setOnCompletionListener
             }
         }
     }
        ); // end onClick botón
        // Buscamos el botón que graba con  MediaRecorder y definimos su
        // evento onClick
        boton_mrecorder = (Button) findViewById(R.id.mediaplayer_record);
        boton_mrecorder.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                   // Si estamos grabando sonido
                   if (boton_mrecorder.getText().equals("Parar grabación"))
                   {
                       // Paramos la grabación, liberamos los recursos y la
                       // añadimos
                       recorder.stop();
                       recorder.release();
                       addRecordingToMediaLibrary();
                       // Refrescamos interfaz usuario
                       boton_mrecorder.setText("Grabar conversación");
                       boton_spool.setEnabled(true);
                       boton_mplayer.setEnabled(true);
                       // Log de la acción
                       log("Parada grabación MediaRecorder");
                   } else
                   {
                       // Cambiamos los botones y hacemos log
                       boton_mrecorder.setText("Parar grabación");
                       boton_spool.setEnabled(false);
                       boton_mplayer.setEnabled(false);
                       log("Grabando conversación");
                       // Obtenemos el directorio de tarjeta SD
                       File directorio =
                               Environment.getExternalStorageDirectory();
                       try {
                           // Definimos el archivo de salida
                           audiofile = File.createTempFile("sonido", ".3gp",
                                   directorio);
                       } catch (IOException e) {
                           Log.e("ERROR", "No se puede acceder a la tarjeta SD");
                           return;
                       }
                       // Creamos el objeto MediaRecorder
                       recorder = new MediaRecorder();
                       // Establecemos el micrófono
                       recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                       // Tipo de formato de salida
                       recorder.setOutputFormat(
                               MediaRecorder.OutputFormat.THREE_GPP);
                       // Codificación de la salida
                       recorder.setAudioEncoder(
                               MediaRecorder.AudioEncoder.AMR_NB);
                       // Fichero de salida
                       recorder.setOutputFile(audiofile.getAbsolutePath());
                       try {
                           // Preparamos la grabación
                           recorder.prepare();
                       } catch (IllegalStateException e) {
                           Log.e("ERROR", "Estado incorrecto");
                           return;
                       } catch (IOException e) {
                           Log.e("ERROR", "No se puede acceder a la tarjeta SD");
                           return;
                       }
                       // Iniciamos la grabación
                       recorder.start();
                   } // end else
               }
           }
        );
// end onClick botón
        log("");
    }

    private void initializeAudioPlayback() {
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // ... (Rest of your SoundPool or MediaPlayer initialization)
    }

    // Metodo que añade la nueva grabación a la librería
    // multimedia del dispositivo. Para ello, vamos a
    // utilizar un Intent del sistema operativo
    protected void addRecordingToMediaLibrary() {
        // Valores que vamos a pasar al Intent
        ContentValues values = new ContentValues(4);
        // Obtenemos tiempo actual
        long tiempoActual = System.currentTimeMillis();
        // Indicamos que queremos buscar archivos de tipo audio
        values.put(MediaStore.Audio.Media.TITLE, "audio" +
                audiofile.getName());
        // Indicamos la fecha sobre la que deseamos buscar
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int)(tiempoActual /
                1000));
        // Tipo de archivo
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        // Directorio destino
        values.put(MediaStore.Audio.Media.DATA,
                audiofile.getAbsolutePath());
        // Utilizamos un ContentResolver
        ContentResolver contentResolver = getContentResolver();
        // URI para buscar en la tarjeta SD
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);
        // Enviamos un mensaje Broadcast para buscar el nuevo contenido de
        // tipo audio
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                newUri));
        Toast.makeText(this, "Se ha añadido archivo " + newUri +
                " a la librería multimedia.", Toast.LENGTH_LONG).show();
    }
    // end addRecordingToMediaLibrary
    // Metodo que añade a la etiqueta Log un nuevo evento
    private void log(String s) {
        logTextView.append(s + "\n");
        // Movemos el Scroll abajo del todo
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
// end log
}
// end clase