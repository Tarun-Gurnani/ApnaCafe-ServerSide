package com.example.tarun.apna_cafe_server;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button btnSignin;
    TextView txtSlogan;


    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        btnSignin =findViewById ( R.id.btnSignIN );
        txtSlogan=findViewById ( R.id.txtslogan );
        Typeface face = Typeface.createFromAsset ( getAssets (),"fonts/Nabila.ttf" );
        txtSlogan.setTypeface ( face );

        btnSignin.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                Intent signin = new Intent ( MainActivity.this,SignIn.class );
                startActivity ( signin );
            }
        } );
    }
}
