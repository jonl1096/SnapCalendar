package com.caljon.snapcalendar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraIntentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CameraIntentFragment extends Fragment {


    public static final int REQUEST_CAMERA = 1;

    ImageView imageView;

    private CameraIntentListener mListener;

    public interface CameraIntentListener {
        void launchCamera(View view);
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
//        imageView = (ImageView) view.findViewById(R.id.photoview);

        // Check permission for CAMERA
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            // Callback onRequestPermissionsResult interceptado na Activity MainActivity
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    MainActivity.REQUEST_CAMERA);
        } else {
            // permission has been granted, continue as usual

            Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(captureIntent, 0);
        }

        cameraButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        buttonClicked(v);
                    }
                }
        );

        // Disable the button if user has no camera
        if (!hasCamera()) {
            cameraButton.setEnabled(false);
        }

        return view;
    }



    // Check if the user has a camera.
    private boolean hasCamera() {
        // Since this is a fragment and the .getPackageManager() method is from Context,
        // need to use getActivity()
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    // call this when the button is clicked
    public void buttonClicked(View v) {
        this.mListener.launchCamera(v);
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
