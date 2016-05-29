package com.caljon.snapcalendar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraIntentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CameraIntentFragment extends Fragment {

    private static final String TAG = CameraIntentFragment.class.getSimpleName();
    public static final int REQUEST_CAMERA = 11;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    private static final String TESSDATA = "tessdata";
    String result = "empty";
    private TessBaseAPI tessBaseAPI;

    private ImageView photoView;
//    private String mCurrentPhotoPath;
    Uri outputFileUri;
    private CameraIntentListener mListener;
    private String mDirPath = null;
    private static final String lang = "eng";

    public interface CameraIntentListener {
//        void launchCamera(View view);
//        Bitmap getTextImage(String text, int width, int height);
//        void onActivityResult(int requestCode, int resultCode, Intent data);
//        String extractText(Bitmap bitmap, String dataPath);
    }

    public CameraIntentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera_intent, container, false);
        Button cameraButton = (Button) view.findViewById(R.id.cameraButton);
        photoView = (ImageView) view.findViewById(R.id.photoView);

        // Check permission for CAMERA
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            // Callback onRequestPermissionsResult interceptado na Activity MainActivity
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA);
        }

        // Check permission for WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            // Callback onRequestPermissionsResult interceptado na Activity MainActivity
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainActivity.REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchCamera(v);
                    }
                }
        );

        // Disable the button if user has no camera
        if (!hasCamera()) {
            System.out.println("DEVICE HAS NO CAMERA");
            cameraButton.setEnabled(false);
        }

        return view;
    }

    // Lauching the camera.
    public void launchCamera(View view) {
        try {
            System.out.println("INSIDE LAUNCHCAMERA");
            String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/SnapCalendar/imgs";
            prepareDirectory(IMGS_PATH);

            String img_path = IMGS_PATH + "/ocr.jpg";

            outputFileUri = Uri.fromFile(new File(img_path));
//            final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, outputFileUri);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                System.out.println("About to start Camera Intent");
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static Bitmap getTextImage(String text, int width, int height) {
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Paint paint = new Paint();
        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(40.0f);
        canvas.drawText(text, width / 2, height / 2, paint);
        return bmp;
    }

    // Returning the image taken.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == getActivity().RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap photo = BitmapFactory.decodeFile(outputFileUri.getPath(), options);
            photoView.setImageBitmap(photo);
            prepareTesseract();
            startOCR(outputFileUri);
        } else {
            Toast.makeText(getActivity(), "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getActivity().getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getActivity().getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param imgUri
     */
    private void startOCR(Uri imgUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);

            result = extractText(bitmap);
            System.out.println(result);
            TextView textView = (TextView) getActivity().findViewById(R.id.textView);
            textView.setText(result);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String extractText(Bitmap bitmap) {
        try {
            tessBaseAPI = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseAPI == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseAPI.init(DATA_PATH, lang);

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded");
        tessBaseAPI.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseAPI.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseAPI.end();
        return extractedText;
    }


//    // TODO: Testing OCR functionality
//    private String extractText(Bitmap bitmap, String dataPath) throws Exception
//    {
//        TessBaseAPI tessBaseApi = new TessBaseAPI();
//        tessBaseApi.init(dataPath, lang);
//        tessBaseApi.setImage(bitmap);
//        String extractedText = tessBaseApi.getUTF8Text();
//        tessBaseApi.end();
//        return extractedText;
//    }

    // Check if the user has a camera.
    private boolean hasCamera() {
        // Since this is a fragment and the .getPackageManager() method is from Context,
        // need to use getActivity()
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CameraIntentListener) {
            mListener = (CameraIntentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment CameraIntentFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static CameraIntentFragment newInstance(String param1, String param2) {
//        CameraIntentFragment fragment = new CameraIntentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }


//        // Create fragment and give it an argument specifying the article it should show
//        PhotoViewFragment photoViewFragment = new PhotoViewFragment();
//        Bundle args = new Bundle();
//        args.putInt(PhotoViewFragment.ARG_POSITION, position);
//        photoViewFragment.setArguments(args);
//
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//
//        // Replace whatever is in the fragment_container view with this fragment,
//        // and add the transaction to the back stack so the user can navigate back
//        transaction.replace(R.id.fragment_container, photoViewFragment);
//        transaction.addToBackStack("cameraintentfragment");
//
//        // Commit the transaction
//        transaction.commit();

