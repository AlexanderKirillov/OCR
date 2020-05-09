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

import com.kaopiz.kprogresshud.KProgressHUD;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

public class OCRFragment extends Fragment {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;
    private String OCRText;
    private EditText resultText;
    private TextView ocrResultHeader;
    private RelativeLayout loading_scr;
    private Button exportButton;
    private Utilities utils = new Utilities(getContext());
    private String photoPath;
    private Uri photoURI;
    private String language;
    private KProgressHUD hud;

    public static OCRFragment newInstance() {
        return new OCRFragment();
    }

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

    class OCRProgress extends AsyncTask<Integer, Integer, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

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

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            resultText.setText(result);
            ocrResultHeader.setVisibility(View.VISIBLE);
            resultText.setVisibility(View.VISIBLE);
            exportButton.setVisibility(View.VISIBLE);
            loading_scr.setVisibility(View.INVISIBLE);

            final Bundle shText = new Bundle();
            shText.putString("OCRTEXT", OCRText);

            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment exportFragment = ExportFragment.newInstance();
                    exportFragment.setArguments(shText);
                    utils.switchFragment(getActivity(), exportFragment);
                }
            });

        }
    }
}

