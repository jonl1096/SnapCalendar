package com.caljon.snapcalendar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraIntentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CameraIntentFragment extends Fragment {


    public static final int REQUEST_CAMERA = 11;

    private ImageView photoView;
    private String mCurrentPhotoPath;
    private ProgressDialog mProgressDialog;
    private CameraIntentListener mListener;

    public interface CameraIntentListener {
//        void launchCamera(View view);
//        Bitmap getTextImage(String text, int width, int height);
//        void onActivityResult(int requestCode, int resultCode, Intent data);
//        String extractText(Bitmap bitmap, String dataPath);
    }

    public CameraIntentFragment() {
        // Required empty public constructor
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

        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchCamera(v);
                    }
                }
        );

        // Disable the button if user has no camera
        if (!hasCamera()) {
            cameraButton.setEnabled(false);
        }

        return view;
    }

    // Lauching the camera.
    public void launchCamera(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


//        File photoFile = null;
//        try {
//            photoFile = createImageFile();
//        } catch (IOException ex) {
//            // Error occurred while creating the File
//
//        }
//        // Continue only if the File was successfully created
//        if (photoFile != null) {
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                    Uri.fromFile(photoFile));
//            // Launch the cameraIntent, take the picture, and pass the results to onActivityResult
//            startActivityForResult(cameraIntent, REQUEST_CAMERA);
//        }

        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String storageDir = Environment.getExternalStorageDirectory()
                + "/TessOCR";
        File dir = new File(storageDir);
        if (!dir.exists())
            dir.mkdir();

        File image = new File(storageDir + "/" + imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
        System.out.println("IN ONACTIVITYRESULT");
        System.out.println("requestCode: " + requestCode);
        System.out.println("REQUEST_CAMERA: " + REQUEST_CAMERA);
        System.out.println("resultCode: " + resultCode);
        System.out.println("RESULT_OK: " + getActivity().RESULT_OK);



        if (requestCode == REQUEST_CAMERA && resultCode == getActivity().RESULT_OK) {
            // Getting the photo
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            photoView.setImageBitmap(photo);
            System.out.println("JUST SET THE IMAGEVIEW");
            try {
//                String text = extractText(photo, "/mnt/sdcard/tesseract/tessdata/eng.traineddata");
//                String text = extractText(photo, "/mnt/sdcard/tesseract");
                final String inputText = "hello";
                final Bitmap bmp = getTextImage(inputText, 640, 480);
//                photoView.setImageBitmap(bmp);

                String text = extractText(bmp, "/mnt/sdcard/");
//                String text = extractText(bmp, "Evironment.getExternalStorageDirectory().getPath()");
//                String text = "test text";
//                TextView photoText = (TextView) findViewById(R.id.photoText);
//                photoText.setText(text);
            } catch (IOException e) {
                System.out.println("java.lang.IOException");
            } catch (Exception e) {
                System.out.println("java.lang.Exception");
            }

        }

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

    }

    // TODO: Testing OCR functionality
    private String extractText(Bitmap bitmap, String dataPath) throws Exception
    {
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(dataPath, "eng");
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;
    }



    // Check if the user has a camera.
    private boolean hasCamera() {
        // Since this is a fragment and the .getPackageManager() method is from Context,
        // need to use getActivity()
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    // call this when the button is clicked
//    public void buttonClicked(View v) {
//        this.mListener.launchCamera(v);
//    }

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
