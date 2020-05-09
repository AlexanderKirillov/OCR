package org.vogu35.ocr.ui.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

import java.io.File;

public class SettingsFragment extends Fragment {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;
    private Utilities utils = new Utilities(getContext());

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void recreateActivity() {
        Intent intent = getActivity().getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);

        ConstraintLayout home_layout = rootView.findViewById(R.id.settings_layout);

        Button light_theme_btn = rootView.findViewById(R.id.lightThemeBtn);
        Button dark_theme_btn = rootView.findViewById(R.id.darkThemeBtn);
        Button clearCache = rootView.findViewById(R.id.clearCache);

        if (utils.getTheme(getActivity().getApplicationContext()) == 1) {
            home_layout.setBackgroundColor(Color.parseColor("#ffffff"));
            light_theme_btn.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.okeysmalllogo), null, null, null);
        }
        if (utils.getTheme(getActivity().getApplicationContext()) == 2) {
            home_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
            dark_theme_btn.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.okeysmalllogo), null, null, null);
        }

        light_theme_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.setTheme(getContext(), THEME_LIGHT);
                recreateActivity();
            }
        });

        dark_theme_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.setTheme(getContext(), THEME_DARK);
                recreateActivity();
            }
        });

        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View progressDialogView = inflater.inflate(R.layout.progress_dialog, null);
                AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(getActivity());
                progressDialogBuilder.setView(progressDialogView);
                progressDialogBuilder.setCancelable(false);
                final AlertDialog progressDialog = progressDialogBuilder.create();
                progressDialog.show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (utils.deleteDir(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "OCR"))) {
                            progressDialog.dismiss();
                            utils.createOCRFolder();
                            utils.copyAssets("rus.traineddata");
                            utils.copyAssets("eng.traineddata");
                            utils.showSnackBar(getView(), getResources().getString(R.string.cleanup_success));
                        } else {
                            utils.showSnackBar(getView(), getResources().getString(R.string.cleanup_error));
                        }
                    }
                }, 1500);

            }
        });

        return rootView;
    }
}
