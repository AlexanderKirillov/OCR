package org.vogu35.ocr.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.onebit.spinner2.Spinner2;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.markushi.ui.CircleButton;

import static android.app.Activity.RESULT_OK;

/**
 * Фрагмент "Работа с импортированным изображением".
 * Предназначен для реализации возможностей просмотра и редактирования изображения (кадрирования, поворота),
 * с которого будет осуществляться распознавание текста.
 */
public class ImageFragment extends Fragment {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;

    /**
     * Объект класса Utilities.
     */
    private Utilities utils = new Utilities(getContext());

    /**
     * ImageView для просмотра импортированного фото.
     */
    private ImageView imageView;

    /**
     * Путь к импортированному фото.
     */
    private String photoPath;

    /**
     * URI (специальный идентификатор, по которому можно определить ресурс) импортированного фото.
     */
    private Uri photoURI;

    /**
     * Метод диначеского создания нового экземпляра данного фрагмента.
     */
    public static ImageFragment newInstance() {
        return new ImageFragment();
    }

    /**
     * Основной метод фрагмента.
     * В нем реализуется инициализация интерфейса, находятся обработчики кнопок и т.д.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.image_fragment, container, false);

        ConstraintLayout image_layout = rootView.findViewById(R.id.image_layout);
        if (utils.getTheme(getContext()) <= THEME_LIGHT) {
            image_layout.setBackgroundColor(Color.parseColor("#ffffff"));
        } else if (utils.getTheme(getContext()) == THEME_DARK) {
            image_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
        }

        photoPath = getArguments().getString("path_to_photo");
        photoURI = Uri.parse(getArguments().getString("uri_path"));

        final File imageFile = new File(photoPath);

        if (imageFile.exists()) {
            imageView = rootView.findViewById(R.id.photoView);

            final View progressDialogView = inflater.inflate(R.layout.progress_dialog, null);
            AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(getActivity());
            progressDialogBuilder.setView(progressDialogView);
            progressDialogBuilder.setCancelable(false);
            final AlertDialog progressDialog = progressDialogBuilder.create();
            progressDialog.show();

            Picasso.get()
                    .load(Uri.fromFile(imageFile))
                    .resize(utils.dpToPx(365, getContext()), utils.dpToPx(550, getContext()))
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();

                                    final File croppedImage = utils.createImageFile(1);

                                    final CircleButton cropButton = rootView.findViewById(R.id.cropButton);
                                    cropButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (!croppedImage.exists()) {
                                                try {
                                                    croppedImage.createNewFile();
                                                } catch (IOException e) {
                                                    utils.showSnackBar(getView(), R.string.errorH + ": " + e.getMessage());
                                                }
                                            }

                                            UCrop.Options options = new UCrop.Options();
                                            options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                                            options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                                            options.setToolbarTitle(getResources().getString(R.string.image_editor_title));
                                            options.setToolbarWidgetColor(Color.parseColor("#ffffff"));
                                            options.useSourceImageAspectRatio();
                                            options.setCompressionQuality(100);
                                            options.setFreeStyleCropEnabled(true);
                                            options.setHideBottomControls(true);

                                            startActivityForResult(UCrop.of(photoURI, Uri.fromFile(croppedImage))
                                                    .withOptions(options)
                                                    .getIntent(getContext()), UCrop.REQUEST_CROP);
                                        }
                                    });

                                    final CircleButton prevStepButton = rootView.findViewById(R.id.prevStepButton);
                                    prevStepButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Fragment homeFragment = HomeFragment.newInstance();
                                            utils.switchFragment(getActivity(), homeFragment);
                                        }
                                    });

                                    final File rotatedImage = utils.createImageFile(2);

                                    final CircleButton rotateButton = rootView.findViewById(R.id.rotateButton);
                                    rotateButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (!rotatedImage.exists()) {
                                                try {
                                                    rotatedImage.createNewFile();
                                                } catch (IOException e) {
                                                    utils.showSnackBar(getView(), R.string.errorH + ": " + e.getMessage());
                                                }
                                            }

                                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                            bitmap = utils.RotateBitmap(bitmap, 90);

                                            try (FileOutputStream out = new FileOutputStream(rotatedImage)) {
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                                                photoURI = Uri.fromFile(rotatedImage);
                                                photoPath = rotatedImage.getAbsolutePath();

                                            } catch (IOException e) {
                                                utils.showSnackBar(getView(), R.string.errorH + ": " + e.getMessage());
                                            }

                                            imageView.setImageBitmap(bitmap);
                                        }
                                    });

                                    final CircleButton nextStepButton = rootView.findViewById(R.id.nextStepButton);
                                    nextStepButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Spinner2 lang = rootView.findViewById(R.id.lang);
                                            String selectedLang = lang.getSelectedItem().toString();

                                            final Fragment OCRFragment = org.vogu35.ocr.ui.main.OCRFragment.newInstance();
                                            OCRFragment.setArguments(utils.createBundle(photoPath, photoURI, selectedLang, 0, false, true));
                                            utils.switchFragment(getActivity(), OCRFragment);
                                        }
                                    });
                                }
                            }, 300);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressDialog.dismiss();

                            new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.errorH)
                                    .setMessage(R.string.load_image_error_short + ": " + e.getMessage())
                                    .setCancelable(false)
                                    .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Fragment homeFragment = HomeFragment.newInstance();
                                            utils.switchFragment(getActivity(), homeFragment);
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
        } else {
            utils.showSnackBar(getView(), getResources().getString(R.string.load_image_error_short));
        }

        return rootView;
    }

    /**
     * Метод получения и обработки данных, полученных из других активити
     * (в данном случае, из активити кадрирования изображения).
     *
     * @param requestCode - параметр, определяющий то, откуда получать результат.
     * @param resultCode  - статус получения данных (успешно или нет).
     * @param data        - данные.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            photoURI = UCrop.getOutput(data);
            photoPath = utils.getRealPathFromURI(getContext(), photoURI);

            Picasso.get()
                    .load(photoURI)
                    .into(imageView);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.errorH)
                    .setMessage(R.string.crop_error)
                    .setCancelable(false)

                    .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })

                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }
}

