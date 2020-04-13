package org.vogu35.ocr.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

public class AboutFragment extends Fragment {

    private Utilities utils = new Utilities(getContext());

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.about_fragment, container, false);

        ConstraintLayout about_layout = rootView.findViewById(R.id.about_layout);

        if (utils.getTheme(getActivity().getApplicationContext()) == 1) {
            about_layout.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        if (utils.getTheme(getActivity().getApplicationContext()) == 2) {
            about_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
        }

        return rootView;
    }

}
