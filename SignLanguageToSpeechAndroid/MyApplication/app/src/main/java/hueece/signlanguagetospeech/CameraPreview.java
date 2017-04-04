package hueece.signlanguagetospeech;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Chuck on 3/15/2017.
 *
 * PLEASE READ: If you have no idea what a SurfaceView is or how it works, it's best to look it up.
 *              If you have NO Java experience, or no experience with classes, overriding, or any
 *              OOP principles, stop now and go learn or find someone who does.
 *              If you do not have experience creating an Android app, stop here, go learn or find
 *              someone who does.
 *
 * This class is a child of SurfaceView which is going to allow us to create our own
 * camera window rather using the built-in camera app that comes with android devices.
 * We are creating our own camera window so that we can draw on it.
 *
 * This class also implements the interface SurfaceHolder.Callback so that this class MUST
 * implement crucial callback functions that the SurfaceView uses.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfHolder;
    private Camera camera;

    public CameraPreview(Context context, Camera cam) {
        super(context);
        camera = cam;

        // A SurfaceHolder is like a wrapper around the actual SurfaceView.
        // The SurfaceHolder takes care of callback functions that are called when things happen
        // to the SurfaceView.
        surfHolder = getHolder();
        // We are adding this class as a callback because it implements the functions that the
        // SurfaceHolder is going to call when anything happens to our SurfaceView.
        surfHolder.addCallback(this);

        // Notice that we never do any operations DIRECTLY on the SurfaceView. The operations are
        // done on the SurfaceHolder which does to the SurfaceView what we need it to.
    }

    // ------ SurfaceHolder callback functions START ------
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Called when the SurfaceHolder is created.
        try {
            // Set the SurfaceHolder as the display for the camera
            // Set the orientation, 90 degrees
            // Then start the preview. This is where the user can see what the camera sees.
            camera.setPreviewDisplay(surfaceHolder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // Called when the SurfaceHolder is destroyed by the JVM.

        // Stop the camera view and release the memory resources that were being used by the camera.
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // Called when something about the way the SurfaceHolder looks changes.
        // We are not concerned about this at this time.
        // For any future engineers, if you want to do something special here, you go.

        // Does the same thing as the surfaceCreated callback.
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ------ SurfaceHolder callback functions END ------
}
