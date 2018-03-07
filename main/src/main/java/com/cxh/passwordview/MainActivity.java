package com.cxh.passwordview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    PasswordView mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPasswordView = findViewById(R.id.password_view);
        mPasswordView.setListener(new PasswordView.OnFinishedListener() {
            @Override
            public void selected(char ch) {
                // 每个字符选中回调
            }

            @Override
            public void onResult(String password) {
                if(password.length() < 4){
                    mPasswordView.setPasswordError(true);
                }
            }
        });
    }
}
