package com.passvault.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.passvault.app.R;
import com.passvault.app.databinding.ActivityPasswordGeneratorBinding;
import com.passvault.app.util.PasswordGenerator;

public class PasswordGeneratorActivity extends AppCompatActivity {

    public static final String EXTRA_RETURN_PASSWORD = "return_password";
    public static final String EXTRA_PASSWORD_RESULT = "password_result";

    private ActivityPasswordGeneratorBinding binding;
    private final PasswordGenerator generator = new PasswordGenerator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        updateLengthLabel((int) binding.sliderLength.getValue());
        updateLowerLabel(binding.sliderLower.getValue());
        binding.sliderLength.addOnChangeListener((s, v, fromUser) -> updateLengthLabel((int) v));
        binding.sliderLower.addOnChangeListener((s, v, fromUser) -> updateLowerLabel(v));

        binding.btnGenerate.setOnClickListener(v -> generate());
        binding.btnCopy.setOnClickListener(v -> copyGenerated());
    }

    private void updateLengthLabel(int len) {
        binding.labelLength.setText(String.valueOf(len));
    }

    private void updateLowerLabel(float f) {
        binding.labelLower.setText(String.format("%.2f", f));
    }

    private void generate() {
        int len = (int) binding.sliderLength.getValue();
        boolean digits = binding.checkDigits.isChecked();
        boolean upper = binding.checkUpper.isChecked();
        boolean special = binding.checkSpecial.isChecked();
        float lowerRatio = binding.sliderLower.getValue();
        String pass = generator.generate(len, digits, upper, special, lowerRatio);
        binding.editGenerated.setText(pass);
    }

    private void copyGenerated() {
        String s = binding.editGenerated.getText() != null ? binding.editGenerated.getText().toString() : "";
        if (s.isEmpty()) {
            Toast.makeText(this, "Generate a password first", Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("password", s));
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        if (getIntent().getBooleanExtra(EXTRA_RETURN_PASSWORD, false)) {
            String s = binding.editGenerated.getText() != null ? binding.editGenerated.getText().toString() : "";
            if (!s.isEmpty()) {
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_PASSWORD_RESULT, s));
            }
        }
        super.finish();
    }
}
