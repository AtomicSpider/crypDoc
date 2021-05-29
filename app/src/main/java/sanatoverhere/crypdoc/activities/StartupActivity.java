package sanatoverhere.crypdoc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

import sanatoverhere.crypdoc.R;

public class StartupActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(StartupActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication Error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startActivity(new Intent(StartupActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication Failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

//        promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Biometric login for CrypDoc")
//                .setSubtitle("Log in using your biometric credential")
//                .setNegativeButtonText("Use account password")
//                .build();
//
//        biometricPrompt.authenticate(promptInfo);

        startActivity(new Intent(StartupActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        biometricPrompt.authenticate(promptInfo);
    }
}
