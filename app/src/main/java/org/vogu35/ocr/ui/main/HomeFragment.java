package org.vogu35.ocr.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.vogu35.ocr.R;
import org.vogu35.ocr.Utilities;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 1;

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;

    private static int GMODE;
    private Utilities utils = new Utilities(getContext());
    private String photoPath;
    private Uri photoURI;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public View onCreateView(final @NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        ConstraintLayout home_layout = rootView.findViewById(R.id.home_layout);
        ImageView ocr_home_logo = rootView.findViewById(R.id.ocr_home_logo);

        if (utils.getTheme(getContext()) <= THEME_LIGHT) {
            home_layout.setBackgroundColor(Color.parseColor("#ffffff"));
            ocr_home_logo.setImageResource(R.drawable.ocr_homelogo);
        } else if (utils.getTheme(getContext()) == THEME_DARK) {
            home_layout.setBackgroundColor(Color.parseColor("#2d2d2d"));
            ocr_home_logo.setImageResource(R.drawable.ocr_homelogo_white);
        }

        Button fromCamera = rootView.findViewById(R.id.fromCamera);
        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GMODE = 1;
                takePictureFromCameraIntent();
            }
        });

        Button fromGallery = rootView.findViewById(R.id.fromGallery);
        fromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GMODE = 2;
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        Button fromURL = rootView.findViewById(R.id.fromURL);
        fromURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GMODE = 3;

                final View urlDialogView = inflater.inflate(R.layout.url_dialog, null);
                AlertDialog.Builder urlDialogBuilder = new AlertDialog.Builder(getActivity());
                urlDialogBuilder.setView(urlDialogView);

                urlDialogBuilder.setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            EditText url = urlDialogView.findViewById(R.id.url);
                            String urlStr = url.getText().toString();

                            final View progressDialogView = inflater.inflate(R.layout.progress_dialog, null);
                            AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(getActivity());
                            progressDialogBuilder.setView(progressDialogView);
                            progressDialogBuilder.setCancelable(false);
                            final AlertDialog progressDialog = progressDialogBuilder.create();

                            Picasso.get()
                                    .load(urlStr)
                                    .resize(utils.dpToPx(365, getContext()), utils.dpToPx(550, getContext()))
                                    .into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                            progressDialog.dismiss();

                                            try {
                                                File imageFile = utils.createImageFile(0);
                                                imageFile.createNewFile();

                                                utils.saveBitmap(bitmap, imageFile.getAbsolutePath());

                                                photoURI = Uri.fromFile(imageFile);
                                                photoPath = imageFile.getAbsolutePath();

                                                Fragment imageFragment = ImageFragment.newInstance();
                                                imageFragment.setArguments(utils.createBundle(photoPath, photoURI, null, GMODE, true, false));
                                                utils.switchFragment(getActivity(), imageFragment);

                                            } catch (IOException e) {
                                                utils.showSnackBar(getActivity().getCurrentFocus(), R.string.errorH + ": " + e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                            progressDialog.dismiss();
                                            new AlertDialog.Builder(getContext())
                                                    .setTitle(R.string.errorH)
                                                    .setMessage(R.string.load_image_error)
                                                    .setCancelable(false)
                                                    .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .show();
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                                            progressDialog.show();
                                        }
                                    });

                        } catch (Exception e) {
                            utils.showSnackBar(getView(), R.string.errorH + ": " + e.getMessage());
                        }
                    }
                });
                urlDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                urlDialogBuilder.show();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment imageFragment = ImageFragment.newInstance();

        if (GMODE == 1) {
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
                imageFragment.setArguments(utils.createBundle(photoPath, photoURI, null, GMODE, true, false));
                utils.switchFragment(getActivity(), imageFragment);
            } else {
                utils.showSnackBar(getView(), getResources().getString(R.string.camera_error_title));
            }
        }
        if (GMODE == 2) {
            if (resultCode == RESULT_OK) {
                photoURI = data.getData();
                photoPath = utils.getRealPathFromURI(getContext(), photoURI);

                imageFragment.setArguments(utils.createBundle(photoPath, photoURI, null, GMODE, true, false));
                utils.switchFragment(getActivity(), imageFragment);
            } else {
                utils.showSnackBar(getView(), getResources().getString(R.string.choose_image_error));
            }
        }
    }

    private void takePictureFromCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File imageFile = null;
            try {
                imageFile = utils.createImageFile(0);
                imageFile.createNewFile();

            } catch (IOException ex) {
                utils.showSnackBar(getView(), R.string.errorH + ": " + ex.getMessage());
            }
            if (imageFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "org.vogu35.ocr.fileprovider",
                        imageFile);
                photoPath = imageFile.getAbsolutePath();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
