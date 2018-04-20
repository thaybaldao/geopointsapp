package com.example.thay.test;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.parse.Parse;
import com.parse.ParseUser;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Parse.initialize(this);

        final Button user_location_button = findViewById(R.id.button_user);
        user_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Intent intent = new Intent(MainActivity.this, UsersActivity.class);
               intent.putExtra("function", 1);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(intent);


            }
        });

        final Button show_users_location = findViewById(R.id.button_users);
        show_users_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                intent.putExtra("function", 2);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        final Button button_closest_user = findViewById(R.id.button_closest_user);
        button_closest_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                intent.putExtra("function", 3);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        final Button button_users_delimited_area = findViewById(R.id.button_polygon);
        button_users_delimited_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, UsersActivity.class);
                intent.putExtra("function", 4);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        final Button logout_button = findViewById(R.id.logout_button);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dlg = new ProgressDialog(MainActivity.this);
                dlg.setTitle("Please, wait a moment.");
                dlg.setMessage("Signing Out...");
                dlg.show();

                // logging out of Parse
                ParseUser.logOut();

                alertDisplayer("So, you're going...", "Ok...Bye-bye then");

            }
        });



    }

    private void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

}
