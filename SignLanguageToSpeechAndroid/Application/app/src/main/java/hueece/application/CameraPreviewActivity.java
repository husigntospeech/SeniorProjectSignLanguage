package hueece.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


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

    private Camera camera;
    private CameraPreview cameraPreview;
    private String ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tells android which xml layout to use for this activity.
        setContentView(R.layout.activity_camera_preview);

        // Recall that in Main Activity we had stored the retrieved IP Address in an intent.
        // We are going to retrieve that stored IP Address here.
        Bundle extras = getIntent().getExtras();
        ipAddress = extras.getString("ipAddress");

        // Get an instance that represents the camera of the android device.
        camera = getCameraInstance();

        // Instantiate an object of CameraPreview.
        // It helps to actually look at this class before reading the rest of this class.
        cameraPreview = new CameraPreview(this, camera);

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

            // This restarts the camera preview that the user is seeing otherwise the user would
            // only see the static image that they just took.
            camera.stopPreview();
            camera.startPreview();
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
    protected void sendRequestToServer(byte[] imageBytes) {

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
        imageBytes = stream.toByteArray();

        // Convert the byte array into a Base64 string.
        String imageByteString64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Instantiate a JSON Object.
        JSONObject urlParamJSON = new JSONObject();

        try {
            // Put the Base64 string into the JSON Object.
            urlParamJSON.put("img_string_b64", imageByteString64);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            // Construct the URL where the image will be sent.
            String url = "http://" + this.ipAddress;
            // Execute the Async process that will perform the actual HTTP POST Request.
            new EndpointsAsyncTask().execute(new Pair<String, String>(url, urlParamJSON.toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets called when the server responds to the HTTP Request or when there's an error.
     *
     * @param response
     */
    protected void httpPOSTRequestCallback(String response) {
        TextView translationTxtView = (TextView) findViewById(R.id.tv_translation);
        if (response == null) {
            translationTxtView.setText("Server is being weird. Check IP Address or check server.");
        } else {
            translationTxtView.setText(response);
        }
    }

    /**
     * This is a private class that will do something/anything asynchronously. It will perform
     * the HTTP POST request asynchronously.
     *
     * Some explanation for the way this class structure works.
     * https://androidresearch.wordpress.com/2012/03/17/understanding-asynctask-once-and-forever/
     *
     * AsyncTask< Params, Progress, Result>
     * Params will be url and url parameter
     * Result will be the result of the request from the server
     *
     */
    private class EndpointsAsyncTask extends AsyncTask<Pair<String, String>, Void, String> {

        @Override
        protected String doInBackground(Pair<String, String>... params) {
            // Everything in this function is performed in the background/asynchronously.

            String url = params[0].first;
            String urlParams = params[0].second;
            return executePost(url, urlParams);
        }

        @Override
        protected void onPostExecute(String result) {
            // The word "post" in onPostExecute has nothing to do with the HTTP POST request. It
            // just means that this function is called post/after execution of the async task.
            httpPOSTRequestCallback(result);
        }

        /**
         * A helper function to do the ACTUAL HTTP POST request.
         * If you DO NOT know how HTTP Requests, especially POST request are sent over a network,
         * stop now and go learn. Otherwise, this code will look like gibberish.
         *
         * @param targetURL
         * @param urlParameters
         * @return HTTP Post server response
         */
        protected String executePost(String targetURL, String urlParameters) {
            HttpURLConnection connection = null;

            try {
                // Create connection with the target URL.
                URL url = new URL(targetURL);

                // Open the connection and configure the connection the way a HTTP POST connection
                // that communicates via JSON should be configured.
                // Notice that we didn't specify a timeout. There's a default timeout (idk what it
                // is) but i didn't specify a timeout because who knows how long it'll take Howard's
                // network to send this request.
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/json; charset=UTF-8");
                connection.setRequestProperty("Content-Length",
                        Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);

                // Create a stream with the server where data can be sent.
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream());
                // Send the url parameters as data.
                wr.writeBytes(urlParameters);
                // Close the stream.
                wr.close();

                // Create a stream with the server where data can be received.
                InputStream is;
                // Get the response code of the POST request. 400 is error. 200 is OK.
                int status = connection.getResponseCode();

                if (status >= 400)
                    // This call will block. In other words, the JVM will pause execution of this
                    // app until this function finishes returning the error stream.
                    is = connection.getErrorStream();
                else
                    // This call will block. In other words, the JVM will pause execution of this
                    // app until this function finishes returning the input stream.
                    is = connection.getInputStream();

                // Once either an error stream or input stream has been received, it the actual
                // contents of the stream need to be parsed via a BufferedReader.
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                // StringBuilder will be used to, well, build a string out of the input stream data.
                StringBuilder response = new StringBuilder();
                String line;
                // While the buffered reader is able to read data from the InputStream
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\n');
                }
                // Close BufferedReader.
                rd.close();

                // Return the built string.
                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }
    }
}
