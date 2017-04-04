package hueece.signlanguagetospeech;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Chuck on 3/15/2017.
 *
 * PLEASE READ: If you have NO Java experience, or no experience with classes, overriding, or any
 *              OOP principles, stop now and go learn or find someone who does.
 *              If you do not have experience creating an Android app, stop here, go learn or find
 *              someone who does.
 *
 * This class is representation of a View in Android. Think of a View as a playground to display
 * things. Whether it be a picture, bitmap graphics, vector graphics, etc. We are going to use
 * this View to draw a green rectangle where a user needs to capture the hand of someone making
 * a sign language gesture. This View will be appended to the SurfaceView that is showing the user
 * a camera preview.
 */

public class Box extends View {

    private Paint paint;
    private int cameraPreviewWidth;
    private int camearPreviewHeight;

    Box(Context context, int width, int height) {
        super(context);
        // A paint object will allow us to draw things on the View.
        paint = new Paint();

        cameraPreviewWidth = width;
        camearPreviewHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) { // Override the onDraw() Method
        super.onDraw(canvas);

        // Just setting some characteristics that we want that our Paint object should know about.
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);

        // The green box is drawn relative to the width and height of the size of the camera preview
        // that the user is seeing. This is because when the user takes a picture, a picture of that
        // same size will be sent to the server. The server will need to crop the part of the
        // picture with the gesturers hand. Server and Client need to agree on where the gesture
        // will be located. So both server and client will have this location defined the same way,
        // relative to the size of the camera preview size or the size of the image sent to the
        // server.
        int left = cameraPreviewWidth / 5;      // x coordinate
        int top = 0;                            // y coordinate
        int right = cameraPreviewWidth / 2;     // x coordinate
        int bottom = camearPreviewHeight / 3;   // y coordinate

        /**
         *      top
         *  left    right
         *      bottom
         */

        // Draw the rectangle.
        canvas.drawRect(left, top, right, bottom, paint);
    }
}