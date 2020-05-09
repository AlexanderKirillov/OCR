package org.vogu35.ocr.ui.main;

import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

import java.io.File;

public class ExportFragment extends Fragment {

    private TextView exportText;
    private TextView savestatus;
    private ImageView statusicon;
    private Button go_to_menu;
    private LinearLayout ll1;
    private String OCRText;
    private Utilities utils = new Utilities(getContext());

    public static ExportFragment newInstance() {
        return new ExportFragment();
    }

    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        OCRText = getArguments().getString("OCRTEXT");

        View rootView = inflater.inflate(R.layout.export_fragment, container, false);

        exportText = rootView.findViewById(R.id.exportText);
        ll1 = rootView.findViewById(R.id.ll1);
        savestatus = rootView.findViewById(R.id.savestatus);
        statusicon = rootView.findViewById(R.id.statusicon);
        go_to_menu = rootView.findViewById(R.id.go_to_menu);

        ConstraintLayout export_layout = rootView.findViewById(R.id.export_layout);

        if (utils.getTheme(getActivity().getApplicationContext()) == 1) {
            export_layout.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        if (utils.getTheme(getActivity().getApplicationContext()) == 2) {
            export_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
        }

        final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.img_btn_anim);

        final ImageButton txtButton = rootView.findViewById(R.id.textToTXT);
        final ImageButton pdfButton = rootView.findViewById(R.id.textToPDF);

        txtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View fileNameDialogView = inflater.inflate(R.layout.filename_dialog, null);
                AlertDialog.Builder fileNameDialogBuilder = new AlertDialog.Builder(getActivity());
                fileNameDialogBuilder.setView(fileNameDialogView);

                fileNameDialogBuilder.setPositiveButton(R.string.saveTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            EditText filename = fileNameDialogView.findViewById(R.id.filename);
                            String filenameStr = filename.getText().toString();

                            File txt = utils.createDocumentFile(filenameStr, ".txt");
                            utils.writeToFile(OCRText, txt);

                            ll1.setVisibility(View.INVISIBLE);
                            exportText.setVisibility(View.INVISIBLE);

                            savestatus.setVisibility(View.VISIBLE);
                            statusicon.setVisibility(View.VISIBLE);
                            go_to_menu.setVisibility(View.VISIBLE);

                            go_to_menu.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Fragment homeFragment = HomeFragment.newInstance();
                                    utils.switchFragment(getActivity(), homeFragment);
                                }
                            });

                            if (!txt.exists()) {
                                savestatus.setText(getActivity().getResources().getString(R.string.errorSaving));
                                statusicon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.error));

                            } else {
                                savestatus.setText(getActivity().getResources().getString(R.string.savingSuccess) + " " + txt.getAbsolutePath());
                                statusicon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.success));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                fileNameDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                fileNameDialogBuilder.show();
            }
        });

        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View fileNameDialogView = inflater.inflate(R.layout.filename_dialog, null);
                AlertDialog.Builder fileNameDialogBuilder = new AlertDialog.Builder(getActivity());
                fileNameDialogBuilder.setView(fileNameDialogView);

                fileNameDialogBuilder.setPositiveButton(R.string.saveTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            EditText filename = fileNameDialogView.findViewById(R.id.filename);
                            String filenameStr = filename.getText().toString();

                            File pdf = utils.createDocumentFile(filenameStr, ".pdf");
                            utils.writeToPDF(OCRText, pdf);

                            ll1.setVisibility(View.INVISIBLE);
                            exportText.setVisibility(View.INVISIBLE);

                            savestatus.setVisibility(View.VISIBLE);
                            statusicon.setVisibility(View.VISIBLE);
                            go_to_menu.setVisibility(View.VISIBLE);

                            go_to_menu.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Fragment homeFragment = HomeFragment.newInstance();
                                    utils.switchFragment(getActivity(), homeFragment);
                                }
                            });

                            if (!pdf.exists()) {
                                savestatus.setText(getActivity().getResources().getString(R.string.errorSaving));
                                statusicon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.error));

                            } else {
                                savestatus.setText(getActivity().getResources().getString(R.string.savingSuccess) + " " + pdf.getAbsolutePath());
                                statusicon.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.success));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                fileNameDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                fileNameDialogBuilder.show();
            }
        });

        setAnimOnImageButton(txtButton, anim);
        setAnimOnImageButton(pdfButton, anim);

        return rootView;
    }

    private void setAnimOnImageButton(final ImageButton imgBtn, final Animation anim) {
        imgBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imgBtn.startAnimation(anim);
                        break;

                    case MotionEvent.ACTION_UP:
                        imgBtn.clearAnimation();
                        break;
                }
                return false;
            }
        });
    }

}
