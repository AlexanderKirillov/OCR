package org.vogu35.ocr;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * Активити загрузочного экрана приложения.
 */
public class SplashScreen extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    /**
     * Необходимые разрешения для работы программы - чтение и запись в память устройства.
     */
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Объект класса Utilities.
     */
    private Utilities utils = new Utilities(this);

    /**
     * Метод, вызываемый при инициализации активити.
     * В нем реализуется инициализация интерфейса загрузочного экрана, запрос разрешений (если необходимо),
     * и, непосредственно, вызов ключевого метода -  {@link SplashScreen#start()}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            start();
        }
    }

    /**
     * Метод, который вызывается после пользов. предоставления\отклонения запроса разрешения на доступ к файлам во внутр.памяти устр.
     * Если разрешение не предоставлено - выводится ошибка.
     * Если предоставлено - программа начинает запускаться (см. {@link SplashScreen#start()}) .
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog aboutDialog = new AlertDialog.Builder(
                            this)
                            .setTitle(R.string.errorH)
                            .setMessage(R.string.access_storage_error)
                            .setNeutralButton(R.string.exit, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }).create();

                    aboutDialog.show();

                } else {
                    start();
                }
            }
        }
    }

    /**
     * Метод запуска программы (создание папки приложения, распаковка в нее необх. файлов,
     * эмуляция загрузки, переход к другой активити).
     */
    public void start() {
        utils.createOCRFolder();
        utils.copyAssets("rus.traineddata");
        utils.copyAssets("eng.traineddata");

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        }, 2300);
    }
}
