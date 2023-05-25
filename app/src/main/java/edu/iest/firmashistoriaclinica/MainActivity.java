package edu.iest.firmashistoriaclinica;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import edu.iest.firmashistoriaclinica.R;
import edu.iest.firmashistoriaclinica.SignatureDialog;

public class MainActivity extends AppCompatActivity {

    private ImageView firmaImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firmaImageView = findViewById(R.id.firmaImageView);

        firmaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignatureDialog signatureDialog = new SignatureDialog();
                signatureDialog.showDialog(MainActivity.this, firmaImageView);
            }
        });
    }
}

