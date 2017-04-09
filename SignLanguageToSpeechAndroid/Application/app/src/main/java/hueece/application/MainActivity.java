package hueece.application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * PLEASE READ: If you have NO Java experience, or no experience with classes, overriding, or any
 *              OOP principles, stop now and go learn or find someone who does.
 *              If you do not have experience creating an Android app, stop here, go learn or find
 *              someone who does.
 *
 * This class represents an ACTUAL screen that the user is seeing in the app. Specifically,
 * the initial screen the user sees when the app opens.
 *
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tells android which xml layout to use for this activity
        setContentView(R.layout.activity_main);
    }

    /**
     * Called by the Start Translation button when the button is pressed.
     *
     * @param v
     */
    protected void startTranslationButtonCallback(View v) {
        // Create intent object that will transition out of this activity to a new one.
        Intent cameraPreviewIntent = new Intent(MainActivity.this, CameraPreviewActivity.class);

        // Retrieve IP Address from text field.
        EditText editTextIpField = (EditText) findViewById(R.id.edtx_ip_field);
        String ipAddress = editTextIpField.getText().toString();

        // If an IP Address is provided, save it in the intent object, then start the new activity.
        if (ipAddress != null) {
            cameraPreviewIntent.putExtra("ipAddress", ipAddress);
            startActivity(cameraPreviewIntent);
        }

    }

}