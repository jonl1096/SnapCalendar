package com.caljon.snapcalendar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import com.googlecode.tesseract.android.TessBaseAPI;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button) findViewById(R.id.cameraButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        // Disable the button if user has no camera
        if (!hasCamera()) {
            cameraButton.setEnabled(false);
        }

    }

    // Check if the user has a camera.
    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    // Lauching the camera.
    public void launchCamera(View view) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Launch the cameraIntent, take the picture, and pass the results to onActivityResult
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    private static Bitmap getTextImage(String text, int width, int height) {
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Paint paint = new Paint();
        final Canvas canvas = new Canvas(bmp);

        canvas.drawColor(Color.WHITE);

        paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(40.0f);
        canvas.drawText(text, width / 2, height / 2, paint);

        return bmp;
    }

    // Returning the image taken.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Getting the photo
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(photo);
            try {
//                String text = extractText(photo, "/mnt/sdcard/tesseract/tessdata/eng.traineddata");
//                String text = extractText(photo, "/mnt/sdcard/tesseract");
                final String inputText = "hello";
                final Bitmap bmp = getTextImage(inputText, 640, 480);
                imageView.setImageBitmap(bmp);
//                String text = extractText(bmp, "/mnt/sdcard/tesseract");
                String text = extractText(bmp, "Evironment.getExternalStorageDirectory().getPath()");
//                String text = "test text";
                System.out.println("The photo text is: " + text);
                TextView photoText = (TextView) findViewById(R.id.photoText);
                photoText.setText(text);
            } catch (Exception e) {
                System.out.println("java.lang.Exception");
            }

        }
    }

    // TODO: Testing OCR functionality
    private String extractText(Bitmap bitmap, String dataPath) throws Exception
    {
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(dataPath, "eng");
        tessBaseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        tessBaseApi.setImage(bitmap);
        String extractedText = tessBaseApi.getUTF8Text();
        tessBaseApi.end();
        return extractedText;
    }
}
