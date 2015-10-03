package it.jaschke.alexandria;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

import it.jaschke.alexandria.services.BookService;

/**
 * Created by PC on 03.10.2015.
 */
public class BarcodeCaptureActivity extends ActionBarActivity implements SurfaceHolder.Callback, MultiProcessor.Factory<Barcode>{

    public static final int BARCODE_CAPTURE_INTENT = 1;
    private CameraSource mCameraSource;
    private SurfaceView mPreview;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_barcode_capture);

        mPreview = (SurfaceView) findViewById(R.id.cameraSourcePreview);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(this).build());
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        mCameraSource = builder.build();
        mPreview.getHolder().addCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCameraSource.start(holder);
            SetCameraAutofocusAndFlash();
        } catch (Exception e) {
            Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
            messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.camera_error));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);

            mCameraSource=null;
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        int h=0;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCameraSource!=null)
        {
            mCameraSource.stop();
            mCameraSource=null;
        }
    }

    private String lastBarCode="";

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        //Once we have an BARCODE, start a book intent
        for (int i=0;i<barcode.displayValue.length();i++)
        {
            char T=barcode.displayValue.charAt(i);
            if (T<'0' || T>'9')
                return null;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("BARCODE",barcode.displayValue);
        setResult(RESULT_OK,returnIntent);
        finish();
        return null;
    }

    void SetCameraAutofocusAndFlash()
    {
        //set flash and auto
        Field[] declaredFields = CameraSource.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(mCameraSource);
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();

                        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        }

                        if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        }
                        camera.setParameters(params);
                        return;
                    }

                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
