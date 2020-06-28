package com.sdzx.news;

import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.*;
import android.widget.Toast;
import android.content.*;
import android.app.*;
import android.view.*;
import com.avos.avoscloud.*;
import android.widget.*;
import android.widget.Toolbar;

public class InitActivity extends AppCompatActivity
{
	EditText phoneEt,passwordEt;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.initToolbar);
		setSupportActionBar(toolbar);
		setTitle("登录");

		passwordEt=(EditText)findViewById(R.id.initlayoutEditText1pass);
		phoneEt=(EditText)findViewById(R.id.initlayoutEditText1phone);



		AVOSCloud.initialize(this, "rTL0VObhRrmgAPdG7fWrd0lr", "xjLm6hYPJHxxtkdEfJkpPomD");

	
		final AVUser currentUser = AVUser.getCurrentUser();
		/*if (currentUser!=null){
		currentUser.refreshInBackground(new RefreshCallback<AVObject>() {
			@Override
			public void done(AVObject arg0, AVException arg1) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(InitActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		}*/
	}
	
	
	public void onLogInClick(View v){
		
		if ((phoneEt.getText().toString().length()!=0)&&(passwordEt.getText().toString().length()!=0)){
		
		AVUser.loginByMobilePhoneNumberInBackground(phoneEt.getText().toString(), passwordEt.getText().toString(), new LogInCallback() {

			@Override
			public void done(AVUser p1, AVException p2) {
				// TODO: Implement this method
				if (p2 == null) {
					if ((AVUser.getCurrentUser().get("level") == 0) || (AVUser.getCurrentUser().get("level") == 1) || (AVUser.getCurrentUser().get("level") == 3)) {

						AVQuery<AVObject> ableQue = new AVQuery<AVObject>("Value");
						ableQue.whereEqualTo("ValueName", "IfLogInAble");
						ableQue.findInBackground(new FindCallback<AVObject>() {
							@Override
							public void done(List<AVObject> list, AVException e) {
								if (e == null) {
									if (list.get(0).getString("Value").equals("1")) {
										Toast.makeText(InitActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
										Intent intent = new Intent();
										intent.setClass(InitActivity.this, MainActivity.class);
										startActivity(intent);
										finish();
									} else {
										Toast.makeText(InitActivity.this, "不允许登陆", Toast.LENGTH_SHORT).show();
									}
								}else Toast.makeText(InitActivity.this, "不允许登陆:"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
							}
						});


					} else {
						Toast.makeText(InitActivity.this, "您没有权限使用内测版，请联系开发组成员", Toast.LENGTH_SHORT).show();
					}
				} else
					Toast.makeText(InitActivity.this, "登陆失败  " + p2.getLocalizedMessage(), Toast.LENGTH_SHORT).show();


			}


		});
		
		}
		else Toast.makeText(InitActivity.this,"请输入用户名和密码",Toast.LENGTH_SHORT).show();
		
		
	}

	public void onForgetPasswordClick(View v){
		Intent intent = new Intent();
		intent.setClass(InitActivity.this, ForgetPassActivity.class);
		startActivity(intent);
	}

	public void onSignUpClick(View v){
		Intent intent = new Intent();
		intent.setClass(InitActivity.this, SignUpActivity.class);
		startActivity(intent);
		
	}
	
	public void jumpClick(View v){
		Toast.makeText(InitActivity.this, "您没有权限使用内测版", Toast.LENGTH_SHORT).show();
	}
	
}
