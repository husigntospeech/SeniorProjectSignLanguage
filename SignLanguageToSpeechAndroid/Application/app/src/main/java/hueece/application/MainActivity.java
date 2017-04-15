package hueece.application;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

        String title = "Hi";
        String message = "Remember to change the IP Address and check what mode you want the app " +
                        "to work in! =)";
        displayAlertDialog(title, message);
    }

    /**
     * Called by the Start Translating button when the button is pressed.
     *
     * @param v
     */
    public void startTranslatingButtonCallback(View v) {
        // Create intent object that will transition out of this activity to a new one.
        Intent cameraPreviewIntent = new Intent(MainActivity.this, CameraPreviewActivity.class);

        // Retrieve IP Address from text field.
        EditText editTextIpField = (EditText) findViewById(R.id.edtx_ip_field);
        String ipAddress = editTextIpField.getText().toString();

        // If an IP Address is provided, save it in the intent object, then start the new activity.
        if (ipAddress != null) {
            cameraPreviewIntent.putExtra("ipAddress", ipAddress);
            cameraPreviewIntent.putExtra("isTraining", checkBoxIsChecked());
            startActivity(cameraPreviewIntent);
        }

    }

    private boolean checkBoxIsChecked() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.check_box_training);
        return checkBox.isChecked();
    }

    public void displayAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(message);

        // Set up the buttons
        builder.setPositiveButton("Got It!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Nothing special needs to happen.
            }
        });

        builder.show();
    }

}
