package com.iot.sentinel;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

public class DialogActivity extends Dialog implements OnTouchListener {

    private EditText Number;
    private Button addOK, addCancel;
    private String _Number;

    public DialogActivity(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acrivity_dialog);

        Number = (EditText) findViewById(R.id.Number);
        addOK = (Button) findViewById(R.id.addOK);
        addCancel = (Button) findViewById(R.id.addCancel);

        addOK.setOnTouchListener(this);
        addCancel.setOnTouchListener(this);
    }

    public String getNumber() {
        return _Number;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == addOK) {
            _Number = Number.getText().toString();
            dismiss();
        }
        else if (v == addCancel)
            cancel();

        return false;
    }
}
