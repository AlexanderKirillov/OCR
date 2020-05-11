package org.vogu35.ocr.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

/**
 * Фрагмент "Распознавание текста с изображения".
 * Предназначен для реализации технологии распознавания текста с фото.
 */
public class OCRFragment extends Fragment {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;

    /**
     * Строка с распознанным текстом.
     */
    private String OCRText;

    /**
     * Поле, где будет размещаться распознанный текст, доступный для редактирования пользователем.
     */
    private EditText resultText;

    /**
     * Заголовок экрана распознавания текста.
     */
    private TextView ocrResultHeader;

    /**
     * Часть интерфейса, показывающаяся пользователю в процессе распознавания текста.
     */
    private RelativeLayout loading_scr;

    /**
     * Кнопка экспорта распознанного текста
     */
    private Button exportButton;

    /**
     * Объект класса Utilities.
     */
    private Utilities utils = new Utilities(getContext());

    /**
     * Путь к импортированному фото.
     */
    private String photoPath;

    /**
     * URI (специальный идентификатор, по которому можно определить ресурс) импортированного фото.
     */
    private Uri photoURI;

    /**
     * Язык распознаваемого текста (на выбор, русский или английский).
     */
    private String language;

    /**
     * Метод диначеского создания нового экземпляра данного фрагмента.
     */
    public static OCRFragment newInstance() {
        return new OCRFragment();
    }

    /**
     * Основной метод фрагмента.
     * в нем реализуется инициализация интерфейса, запуск процесса распознавания текста и т.д.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ocr_fragment, container, false);

        resultText = rootView.findViewById(R.id.resultText);
        exportButton = rootView.findViewById(R.id.exportbutton);
        ConstraintLayout ocr_layout = rootView.findViewById(R.id.ocr_layout);

        loading_scr = rootView.findViewById(R.id.loadingscr);

        ocrResultHeader = rootView.findViewById(R.id.ocrResultHeader);

        if (utils.getTheme(getContext()) <= THEME_LIGHT) {
            ocr_layout.setBackgroundColor(Color.parseColor("#ffffff"));
            resultText.setBackgroundResource(R.drawable.edit_text_style_light);
            resultText.setTextColor(Color.parseColor("#000000"));

        } else if (utils.getTheme(getContext()) == THEME_DARK) {
            ocr_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
            resultText.setBackgroundResource(R.drawable.edit_text_style_dark);
            resultText.setTextColor(Color.parseColor("#ffffff"));
        }

        if (getArguments().getString("lang").equals("Русский")) {
            language = "rus";
        }
        if (getArguments().getString("lang").equals("Английский")) {
            language = "eng";
        }

        photoPath = getArguments().getString("path_to_photo");
        photoURI = Uri.parse(getArguments().getString("uri_path"));

        OCRProgress OCRProgress = new OCRProgress();
        OCRProgress.execute();

        return rootView;
    }

    /**
     * Класс, позволяющий осуществить распознавание текста с в отдельном потоке
     * Без разделения потоков, в процессе распознавания текста, произойдет зависание программы
     */
    class OCRProgress extends AsyncTask<Integer, Integer, String> {

        /**
         * Метод, в котором реализуется непосредственно сам процесс распознавания,
         * который осуществляется в отдельном потоке и в фоне.
         */
        protected String doInBackground(Integer... params) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap imageBitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
            try {
                OCRText = utils.extractText(imageBitmap, language);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return OCRText;
        }

        /**
         * Метод, который запускается после окончания работы метода распознавания текста.
         * В нем реализуется динамическое изменение интерфейса и назначается обработчик на нажатие кнопки "ЭКСПОРТ"
         */
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            resultText.setText(result);
            ocrResultHeader.setVisibility(View.VISIBLE);
            resultText.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
            loading_scr.setVisibility(View.INVISIBLE);

            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Bundle shText = new Bundle();
                    shText.putString("OCRTEXT", resultText.getText().toString());

                    Fragment exportFragment = ExportFragment.newInstance();
                    exportFragment.setArguments(shText);
                    utils.switchFragment(getActivity(), exportFragment);
                }
            });

        }
    }
}

