package com.example.administrator.myapplicationtest;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

public class SuperActivity extends Activity {
	
	public void initForm(final EditText et, final ImageView iv) {
        et.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
										  int arg2, int arg3) {}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
									  int arg3) {
				if(TextUtils.isEmpty(arg0))
					iv.setVisibility(View.INVISIBLE);
				else
					iv.setVisibility(View.VISIBLE);
			}
        	
        });
        iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				et.setText("");
			}
        	
        });
    }

}
