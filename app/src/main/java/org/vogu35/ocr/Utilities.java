package org.vogu35.ocr;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import word.api.interfaces.IDocument;
import word.w2004.Document2004;
import word.w2004.elements.BreakLine;
import word.w2004.elements.Heading1;
import word.w2004.elements.Heading2;
import word.w2004.elements.Heading3;

public class Utilities {
    /**
     * Текущий Context
     */
    private Context context;

    /**
     * Конструктор класса
     */
    public Utilities(Context context) {
        this.context = context;
    }

    /**
     * Метод перевода density-independent pixels в pixels
     *
     * @param dp  density-independent pixels
     * @param ctx Context
     * @return pixels
     */
    public int dpToPx(int dp, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Метод сохранения Bitmap в файл с расширение PNG
     *
     * @param bitmap bitmap, который необходимо сохранить в файл
     * @param path   путь файла
     */
    public void saveBitmap(Bitmap bitmap, String path) {
        if (bitmap != null) {
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод поворота Bitmap на указанное число градусов
     *
     * @param source bitmap, который необходимо повернуть
     * @param angle  угол поворота
     * @return перевернутый bitmap
     */
    public Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Метод создания файла-изображения
     *
     * @param mode режим создания (0 - обычное изображение, 1 - обрезанное изображение, 2 - перевернутое изображение)
     * @return файл-изображение
     */
    public File createImageFile(int mode) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = null;
        if (mode == 0) {
            imageFileName = "OCR_" + timeStamp + "_";
        }
        if (mode == 1) {
            imageFileName = "CROPPED_OCR_" + timeStamp + "_";
        }
        if (mode == 2) {
            imageFileName = "ROTATED_OCR_" + timeStamp + "_";
        }

        File basePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OCR");

        File imageFile = new File(basePath, imageFileName + ".jpg");

        return imageFile;
    }

    /**
     * Метод создания файла с указанным расширением. Используется в процессе экспорта.
     *
     * @param fn        строка с именем файла
     * @param extension расширение файла
     * @return созданный файл
     */
    public File createDocumentFile(String fn, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String filename = fn;
        if (fn.matches("")) {
            filename = "OCR_TXT_" + timeStamp;
        }

        File basePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OCR");

        if (!basePath.exists()) {
            basePath.mkdirs();
        }

        File documentFile = new File(basePath, filename + extension);

        return documentFile;
    }

    private void addMetaData(Document document, String title) {
        document.addTitle(title);
        document.addSubject("Распознанный текст");
        document.addAuthor("OCR App");
        document.addCreator("OCR App");
    }

    /**
     * Метод записи содержимого в .doc файл.
     *
     * @param content строка, которую будем записывать в .doc файл
     * @param f       файл
     */
    public void writeToDoc(String content, File f) {

        String filename = f.getName().substring(0, f.getName().length() - 4);
        String timeStamp = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date());

        IDocument myDoc = new Document2004();
        myDoc.addEle(Heading1.with("Имя файла: " + filename).create());
        myDoc.addEle(Heading2.with("Дата создания файла: " + timeStamp).create());
        myDoc.addEle(BreakLine.times(1).create());
        myDoc.addEle(Heading3.with("Распознанный текст: "));
        myDoc.addEle(BreakLine.times(1).create());
        myDoc.addEle(word.w2004.elements.Paragraph.with(content).create());

        String wordFormat = myDoc.getContent();

        writeToFile(wordFormat, f, false);
    }

    public void writeToPDF(String text, File file) {

        Document doc = new Document();

        try {
            FileOutputStream fOut = new FileOutputStream(file);
            String filename = file.getName().substring(0, file.getName().length() - 4);
            String timeStamp = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date());

            BaseFont bf = BaseFont.createFont("/assets/fonts/TimesNewRoman.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fileNameFont = new Font(bf, 24, Font.BOLD, BaseColor.GRAY);
            Font titleFont = new Font(bf, 22, Font.BOLD, BaseColor.GRAY);
            Font dateFont = new Font(bf, 18, Font.BOLDITALIC, BaseColor.GRAY);
            Font font = new Font(bf, 14, Font.NORMAL);

            PdfWriter.getInstance(doc, fOut);
            doc.open();
            addMetaData(doc, filename);

            doc.add(new Paragraph("Имя файла: " + filename, fileNameFont));
            doc.add(new Paragraph("Дата создания файла: " + timeStamp, dateFont));
            doc.add(new Paragraph("\n", font));
            doc.add(new Paragraph("Распознанный текст:", titleFont));
            doc.add(new Paragraph("\n" + text, font));

        } catch (DocumentException de) {
            Log.e("PDFCreator", "DocumentException:" + de);
        } catch (IOException e) {
            Log.e("PDFCreator", "ioException:" + e);
        } finally {
            doc.close();
        }
    }

    /**
     * Метод записи содержимого в файл при помощи FileWriter.
     *
     * @param content              строка, которую будем записывать в файл
     * @param file                 файл
     * @param isNeedAdditionalInfo опция активации\деактивации необходимости добавления дополнительной информации (имя файла, дата создания) в файл
     */
    public void writeToFile(String content, File file, Boolean isNeedAdditionalInfo) {

        String filename = file.getName().substring(0, file.getName().length() - 4);
        String timeStamp = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss").format(new Date());

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            if (isNeedAdditionalInfo) {
                writer.append("Имя файла: " + filename);
                writer.append("\nДата создания файла:" + timeStamp);
                writer.append("\n\nРаспознанный текст:\n\n");
            }
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
    }

    /**
     * Метод удаления папки из памяти устройства.
     *
     * @param dir папка, которую необходимо удалить
     * @return статус удаления (успешно\неуспешно)
     */
    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                boolean success = deleteDir(child);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Метод создания папки программы (/sdcard/Pictures/OCR)
     */
    public void createOCRFolder() {
        File ocrDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OCR");
        if (!ocrDir.exists()) {
            ocrDir.mkdirs();
        }
    }

    /**
     * Метод переключения фрагмента
     *
     * @param activity текущее Activity
     * @param fr       фрагмент, на который мы переключаемся
     */
    public void switchFragment(Activity activity, Fragment fr) {
        FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fr);
        fragmentTransaction.commit();
    }

    /**
     * Метод создания обьекта Bundle, предназначенного для передачи данных между фрагментами
     *
     * @param photoPath  путь к фото
     * @param photoURI   URI фото
     * @param language   язык распознавания текста
     * @param GMODE      режим (импорт из камеры, галереи или по ссылке)
     * @param isModeNeed требуется ли передача режима в данную функцию
     * @param isLangNeed требуется ли передача языка распознавания в данную функцию
     * @return обьект Bundle
     */
    public Bundle createBundle(String photoPath, Uri photoURI, String language, int GMODE, boolean isModeNeed, boolean isLangNeed) {
        Bundle bundle = new Bundle();

        bundle.putString("path_to_photo", photoPath);
        bundle.putString("uri_path", photoURI.toString());
        if (isModeNeed) {
            bundle.putInt("mode", GMODE);
        }
        if (isLangNeed) {
            bundle.putString("lang", language);
        }

        return bundle;
    }

    /**
     * Метод показа всплывающего сообщения (продвинутый аналог Toast)
     *
     * @param view    текущий View
     * @param message строка, которую необходимо вывести
     */
    public void showSnackBar(View view, String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.parseColor("#5c0000"));
        snackbar.show();
    }

    public String extractText(Bitmap bitmap, String language) throws Exception {
        TessBaseAPI tessBaseApi = new TessBaseAPI();

        File appPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OCR");

        if (language.equals("rus")) {
            tessBaseApi.init(appPath.getAbsolutePath(), "rus");
        }
        if (language.equals("eng")) {
            tessBaseApi.init(appPath.getAbsolutePath(), "eng");
        }
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;
    }

    /**
     * Метод получения реального пути к файлу по URI
     *
     * @param context текущий Context
     * @param uri     URI (специальный идентификатор, по которому можно определить ресурс) файла
     * @return строка с абсолютным путем к файлу
     */
    public String getRealPathFromURI(final Context context, final Uri uri) {

        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Метод проверки, является ли файл документом в памяти устройства
     *
     * @param uri URI файла
     * @return true или false, в зависимости от того, является или нет
     */
    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * Метод проверки, является ли файл загруженным документом
     *
     * @param uri URI файла
     * @return true или false, в зависимости от того, является или нет
     */
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * Метод проверки, является ли файл медиа-документом (фото, видео, аудио)
     *
     * @param uri URI файла
     * @return true или false, в зависимости от того, является или нет
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Метод получения определенных данных при парсинге URI
     *
     * @param context       текущий Context
     * @param uri           URI
     * @param selection     строка с данными для выборки
     * @param selectionArgs различные аргументы, применимые к выборке
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Метод установки темы оформления интерфейса
     *
     * @param context текущий Context
     * @param theme   номер темы (1 - светлая тема, 2 - темная тема)
     */
    public void setTheme(Context context, int theme) {
        SharedPreferences BD = PreferenceManager.getDefaultSharedPreferences(context);
        BD.edit().putInt(context.getString(R.string.prefs_theme_key), theme).apply();
    }

    /**
     * Метод получения текущей темы оформления
     *
     * @param context текущий Context
     * @return номер темы (1 - светлая тема, 2 - темная тема)
     */
    public int getTheme(Context context) {
        SharedPreferences BD = PreferenceManager.getDefaultSharedPreferences(context);
        return BD.getInt(context.getString(R.string.prefs_theme_key), -1);
    }

    /**
     * Метод копирования вспомогательных файлов (например, словарей, используемых для распознавания текста) в папку программы
     *
     * @param filename имя файла, находящегося в assets
     */
    public void copyAssets(String filename) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File basePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OCR");

            File folder = new File(basePath, "tessdata");

            if (!folder.exists()) {
                folder.mkdir();
            }
            File outFile = new File(folder, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);

        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Метод копирования файла
     *
     * @param in  InputStream (откуда копировать)
     * @param out OutputStream (куда копировать)
     */
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
