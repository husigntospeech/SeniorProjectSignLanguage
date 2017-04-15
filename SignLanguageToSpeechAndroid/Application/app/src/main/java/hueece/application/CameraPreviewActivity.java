package hueece.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * PLEASE READ: If you have NO Java experience, or no experience with classes, overriding, or any
 *              OOP principles, stop now and go learn or find someone who does.
 *              If you do not have experience creating an Android app, stop here, go learn or find
 *              someone who does.
 *
 * This class represents an ACTUAL screen that the user is seeing in the app. Specifically,
 * the screen where the user sees a camera preview and is able to take a picture to send to the
 * server.
 *
 * Please note that we have a CameraPreview that is a SurfaceView and a CameraPreviewActivity that
 * represents the ENTIRE window that the user is looking at at this point in the app. Do not
 * confuse the two. I'd have picked a different naming convention, but, yeah, no.
 *
 */
public class CameraPreviewActivity extends AppCompatActivity {

    private boolean isTraining;
    private String serverResponse;
    private Camera camera;
    private CameraPreview cameraPreview;
    private String ipAddress;
    private PersistentClient persistentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tells android which xml layout to use for this activity.
        setContentView(R.layout.activity_camera_preview);

        // Recall that in Main Activity we had stored the retrieved IP Address in an intent.
        // We are going to retrieve that stored IP Address here.
        Bundle extras = getIntent().getExtras();
        ipAddress = extras.getString("ipAddress");
        isTraining = extras.getBoolean("isTraining");

        setTranslateButtonVisibility(false);

        try {
            persistentClient = new PersistentClient("ws://" + ipAddress);
            displayPopUp("Attempting to connect to " + ipAddress +
                            ". Will load the rest of the activity if successful.");
            persistentClient.connect();
        } catch (java.net.URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setupActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Get an instance that represents the camera of the android device.
                camera = getCameraInstance();

                // Instantiate an object of CameraPreview.
                // It helps to actually look at this class before reading the rest of this class.
                cameraPreview = new CameraPreview(CameraPreviewActivity.this, camera);

                // Create a FrameLayout that will hold the CameraPreview SurfaceView.
                // NOTE: THIS IS IMPORTANT. At this stage in the code, NOTHING HAS A size yet. Nothing
                // has actually been created that the user ACTUALLY SEE.
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fr_layout_camera_preview);
                frameLayout.addView(cameraPreview);

                // Okay, a ViewTreeObserver allows us to listen for layout changes on a particular
                // element. So, we are going to attach a callback to the ViewTreeObserver that belongs
                // to the CameraPreview SurfaceView that will be called when any layout changes occur in
                // the CameraPreview SurfaceView.
                ViewTreeObserver observer = cameraPreview.getViewTreeObserver();
                // Adding the callback function "setBox" to be called.
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        setBox();
                    }
                });

            }
        });
    }

    public void closeActivity() {
        finish();
    }

    public void setTranslateButtonVisibility(final boolean enable) {
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Button b = (Button) findViewById(R.id.btn_translate);
                b.setEnabled(enable);
            }
        });
    }
    public void closeActivity(String message) {
        displayPopUp(message);
        closeActivity();
    }

    public void displayPopUp(final String message) {
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(getApplicationContext(), message, duration);
                toast.setGravity(Gravity.TOP|Gravity.TOP, 0, 0);
                toast.show();
            }
        });
    }

    public void displayAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] pieces = serverResponse.split(" ");
        builder.setTitle("Server Translation is " + pieces[0] + ". Pick Correct Translation.");

        // Get list of letters in the alphabet
        CharSequence[] letters = new CharSequence[26];
        for (int i = 0; i < letters.length; i++)
            letters[i] = ((char) (i + 65)) + "";

        builder.setItems(letters, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pieces[0] = ((char) (which + 65)) + "";

                String message = pieces[0] + " " + pieces[1];

                displayPopUp("Oh no! Alright, will let server know. =(");
                persistentClient.send(message);
                camera.startPreview();
                setTranslateButtonVisibility(true);
            }
        });

        // Set up the buttons
        builder.setPositiveButton("Translation Was Correct.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                persistentClient.send(serverResponse);
                displayPopUp("Yay! Server got it right! =)");
                camera.startPreview();
                setTranslateButtonVisibility(true);
            }
        });

        builder.setNegativeButton("Discard Translation.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pieces[0] = "!";

                String message = pieces[0] + " " + pieces[1];
                displayPopUp("Will tell server to discard image.");
                persistentClient.send(message);
                camera.startPreview();
                setTranslateButtonVisibility(true);
            }
        });

        builder.show();
    }

    /**
     * Gets called by the VIewTreeObserver that belongs to the CameraPreview SurfaceView.
     * All it does it adds the Box View to the FrameLayout.
     *
     */
    private void setBox() {
        // Gets called by the VIewTreeObserver that belongs to the CameraPreview SurfaceView.
        // All it does it adds the Box View to the FrameLayout.
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fr_layout_camera_preview);
        Box box = new Box(cameraPreview.getContext(), frameLayout.getMeasuredWidth(),
                frameLayout.getMeasuredHeight());
        frameLayout.addView(box);
    }

    /**
     *  All this function does it take a bitmap image and rotate it by some angle.
     *
     * @param source
     * @param angle
     * @return The rotated Bitmap image.
     */
    public Bitmap rotateImage(Bitmap source, float angle) {
        // Create an empty  matrix.
        Matrix matrix = new Matrix();
        // Add a rotation to the matrix.
        matrix.postRotate(angle);

        // Create a bitmap image based on the source bitmap image and the matrix then return the
        // image.
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Gets called when the Translate button is pressed by the user.
     * All it does it invokes the camera to take a picture and call the function pictureCallback.
     *
     * @param v
     */
    public void translateButtonCallback(View v) {
        camera.takePicture(null, null, pictureCallback);
    }

    /**
     * Gets the object representation of the devices camera. Will throw an exception if it fails.
     *
     * @return An instance, an object representation of the devices camera.
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * When the camera takes a picture, this function is called. The camera passes in the
     * image is just took as an array of bytes.
     *
     */
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            sendRequestToServer(data);

            if (isTraining) {
                camera.stopPreview();
                setTranslateButtonVisibility(false);
            } else {
                // This restarts the camera preview that the user is seeing otherwise the user would
                // only see the static image that they just took.
                camera.stopPreview();
                camera.startPreview();
            }

        }
    };

    /**
     * Does the work required to prepare the taken image to be sent to the server via an HTTP POST
     * request.
     * First the image is converted into a Base64 string and then the quality of the image is
     * compressed as it does not need to be high quality to be interpreted by the server. Lowering
     * the quality makes the server work less hard when it has to parse the Base64 string.
     *
     * @param imageBytes
     */
    protected void sendRequestToServer(final byte[] imageBytes) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Turn the byte array into its actual bitmap image.
                Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                // Rotate the image.
                // READ, THIS IS IMPORTANT: The image is rotated because in SOME Android phones, the camera
                // software that comes with the Android phone will AUTOMATICALLY rotate an image to
                // landscape mode when a picture is taken using the camera. This function rotates it back.
                // IF YOU ARE USING AN ANDROID PHONE THAT DOES NOT AUTOMATICALLY ROTATE,
                // COMMENT OUT THE CALL TO THE ROTATE FUNCTION BELOW.
                image = rotateImage(image, 90);

                // Turn the bitmap image into a byte array that has LESS quality than the original.
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 30, stream);
                byte[] bytes = stream.toByteArray();

                // Convert the byte array into a Base64 string.
                String imageByteString64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                displayPopUp("Sending Image to Server...");
                persistentClient.send(imageByteString64);
            }
        });
        t.start();
    }

    /**
     * Gets called when the server responds to the HTTP Request or when there's an error.
     *
     * @param response
     */
    protected void serverResponseCallback(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView txtView = (TextView) findViewById(R.id.tv_translation);
                if (response == null) {
                    txtView.setText("Error: Check Server.");
                    displayPopUp("Server is being weird. Check IP Address or check server.");
                } else {
                    if (isTraining) {
                        serverResponse = response;
                        displayAlertDialog();
                    } else {
                        String[] pieces = response.split(" ");
                        String text = pieces[0];
                        txtView.setText(text);
                    }
                }
            }
        });
    }

    private class PersistentClient extends WebSocketClient {

        public PersistentClient(String url) throws URISyntaxException {
            super(new URI(url), new Draft_17());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata ) {
            displayPopUp("Connection with server has been established.");
            setupActivity();
            setTranslateButtonVisibility(true);
        }

        @Override
        public void onMessage(String message ) {
            serverResponseCallback(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote ) {
            // The codes are documented in class org.java_websocket.framing.CloseFrame
            closeActivity("Connection with the server has been lost.\n" + reason);
        }

        @Override
        public void onError( Exception ex ) {
            // if the error is fatal then onClose will be called additionally
            displayPopUp("There was an error: " + ex.getMessage());
            ex.printStackTrace();
        }

    }
}
