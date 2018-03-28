package com.muzi.lockview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.muzi.library.LockView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edit)
    EditText edit;
    @BindView(R.id.btnReset)
    Button btnReset;
    @BindView(R.id.btnSet)
    Button btnSet;
    @BindView(R.id.lockView)
    LockView lockView;
    @BindView(R.id.btnSetPsd)
    Button btnSetPsd;

    private StringBuilder builder = new StringBuilder();
    private List<Integer> defaultPsd = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        defaultPsd.add(1);
        defaultPsd.add(2);
        defaultPsd.add(3);
        defaultPsd.add(6);
        defaultPsd.add(9);

        lockView.setOnLockState(new LockView.OnLockState() {
            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "密码正确", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnable() {
                Toast.makeText(MainActivity.this, "不可用", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPassWord(List<Integer> psd) {
                builder.setLength(0);
                for (Integer integer : psd) {
                    builder.append(integer);
                }
                Toast.makeText(MainActivity.this, "输入密码:" + builder.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick({R.id.btnReset, R.id.btnSet, R.id.btnSetPsd})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnReset:
                lockView.setReset();
                break;
            case R.id.btnSet:
                lockView.setCount(Integer.parseInt(edit.getText().toString()));
                break;
            case R.id.btnSetPsd:
                lockView.setPsd(defaultPsd);
                break;
        }
    }
}
