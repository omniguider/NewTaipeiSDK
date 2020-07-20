package omni.com.newtaipeisdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import omni.com.newtaipeisdk.NewTaipeiSDKActivity;

public class MainActivity extends AppCompatActivity {

    private String ARG_KEY_USERNAME = "arg_key_username";
    private String ARG_KEY_USERID = "arg_key_userid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText UN = findViewById(R.id.activity_main_user_name);
        final EditText UI = findViewById(R.id.activity_main_user_id);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTaipeiSDKActivity.class);
                intent.putExtra(ARG_KEY_USERNAME, UN.getText().toString());
                intent.putExtra(ARG_KEY_USERID, UI.getText().toString());
                startActivity(intent);
                UN.getText().clear();
                UI.getText().clear();
            }
        });
    }
}
