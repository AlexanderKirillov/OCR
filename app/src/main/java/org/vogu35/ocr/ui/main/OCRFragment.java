package org.vogu35.ocr.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

public class OCRFragment extends Fragment {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;
    private Utilities utils = new Utilities(getContext());
    private String photoPath;
    private Uri photoURI;

    public static OCRFragment newInstance() {
        return new OCRFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ocr_fragment, container, false);

        EditText resultText = rootView.findViewById(R.id.resultText);
        ConstraintLayout ocr_layout = rootView.findViewById(R.id.ocr_layout);

        if (utils.getTheme(getContext()) <= THEME_LIGHT) {
            ocr_layout.setBackgroundColor(Color.parseColor("#ffffff"));
            resultText.setBackgroundResource(R.drawable.edit_text_style_light);
            resultText.setTextColor(Color.parseColor("#000000"));

        } else if (utils.getTheme(getContext()) == THEME_DARK) {
            ocr_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
            resultText.setBackgroundResource(R.drawable.edit_text_style_dark);
            resultText.setTextColor(Color.parseColor("#ffffff"));
        }

        photoPath = getArguments().getString("path_to_photo");
        photoURI = Uri.parse(getArguments().getString("uri_path"));

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap imageBitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

        //imageBitmap - итоговый Bitmap, который можно использовать для распознавания текста

        return rootView;
    }

}

