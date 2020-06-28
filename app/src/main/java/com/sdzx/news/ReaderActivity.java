package com.sdzx.news;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.*;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.avos.avoscloud.*;
import com.sdzx.news.*;
import com.sdzx.tools.*;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.utils.Util;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class ReaderActivity extends SwipeBackActivity {
	private String cid,className;
	private TextView titleView,timeView,userNameView;
	private ImageView readerImage[],userImg;
	private Bitmap bmp;
	private String userString="0";
	private int picId;
	private NestedScrollView scollView;
	private SharedPreferences sharedPref;
	private LinearLayout readerLinearLayout;
	private boolean ifLoadImg;
	private String fileName[]= new String[40];
	private String messageMainString;
	private AVObject readAvo;
	private ApplicationHelper appHelper;
	private ImageView toolbarIv;
	int lastScollPosition=0,nowScollPosition=0;
	private boolean haveLoadImg=false;
	private FloatingActionButton fab;
	private AVUser thisWriterAvuer;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reader_layout);

		readerImage=new ImageView[20];
		appHelper=new ApplicationHelper();
		toolbarIv=(ImageView)findViewById(R.id.readerIvImage);
		scollView=(NestedScrollView)findViewById(R.id.readerlayoutScollView1);
		scollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				nowScollPosition = scrollY;
				Log.i("SCL", "" + nowScollPosition + "/" + lastScollPosition);
				if (nowScollPosition - lastScollPosition > 20) fab.hide();
				else if (nowScollPosition - lastScollPosition <= -20) fab.show();
				lastScollPosition = nowScollPosition;
			}
		});

		getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
		android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.readertoolbar);
		setSupportActionBar(toolbar);
		fab = (FloatingActionButton) findViewById(R.id.readerfab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onReadComment(null);
			}
		});
		fab.hide();
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		ifLoadImg=sharedPref.getBoolean("IfLoadImg", true);

		titleView=(TextView)findViewById(R.id.readerlayoutTitleTextView1);
		timeView=(TextView)findViewById(R.id.readerlayoutitemTextView1);
		readerLinearLayout=(LinearLayout)findViewById(R.id.readerActivityLinearLayout);
		userNameView=(TextView)findViewById(R.id.readerlayoutitemUserNameTextView2);
		userImg=(ImageView)findViewById(R.id.readerUserImageView);

		titleView.setTextIsSelectable(true);

		Intent intent = getIntent();
		cid = intent.getStringExtra("cid");
		className=intent.getStringExtra("ClassName");
		final int idx=ApplicationHelper.findIndexOfClasses(className,this);
		if (idx!=-1) setTitle(getResources().getStringArray(R.array.categories_name)[idx]);
		else setTitle("详情");

		AVQuery<AVObject> query = new AVQuery<AVObject>(className);
		if (!appHelper.isNetworkConnected(this)){
			query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
			}
		else{
			query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
		}

		query.getInBackground(cid,new GetCallback<AVObject>()
		{
			@Override
			public void done(AVObject avObject1, AVException e) {
				if (e == null)
				{
					readAvo=avObject1;
					titleView.setText(avObject1.getString("Title"));
					AVObject thisavo = avObject1.getAVObject("user");
					if (thisavo != null)
						thisavo.fetchIfNeededInBackground(new GetCallback() {
							@Override
							public void done(AVObject p1, AVException p2) {
								// TODO: Implement this method
								if (p2 == null) {
									thisWriterAvuer=(AVUser)p1;
									userString=thisWriterAvuer.getObjectId();
									userNameView.setText(""+thisWriterAvuer.getUsername());
									AVFile imgFile=p1.getAVFile("Image");
									if (imgFile!=null) imgFile.getDataInBackground(new GetDataCallback() {
										@Override
										public void done(byte[] bytes, AVException e) {
											userImg.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
										}
									});
								} else Log.i("ERR",p2.toString());
							}
						});

					fab.show();
					timeView.setText(avObject1.getUpdatedAt().toLocaleString());
					String messageString=avObject1.getString("Message");
					messageMainString=messageString;
					String messageStrings[]=messageString.split("\\^");
					int lenOfMes=messageStrings.length;
					for (int j=0;j<lenOfMes;j++){
						if (messageStrings[j].startsWith("pic=")){
							if (ifLoadImg){
								final int jfnl=j;
								final String imgId=messageStrings[j].substring(4);

								final ImageView newImgView=new ImageView(ReaderActivity.this);
								newImgView.setScaleType(ImageView.ScaleType.FIT_XY);
								newImgView.setAdjustViewBounds(true);
								newImgView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
								readerLinearLayout.addView(newImgView);

							if (appHelper.fileIsExists("/SDFocus/cache/"+imgId+".cache")){

								new Thread(){
									@Override
									public void run(){
										Bitmap mBitmap;
										mBitmap=appHelper.convertToBitmap("/SDFocus/cache/"+imgId+".cache",80);
										newImgView.setImageBitmap(mBitmap);
										if (haveLoadImg==false) {
											toolbarIv.setImageBitmap(mBitmap);
											haveLoadImg=true;
										}
										newImgView.setOnClickListener(new OnClickListener() {

												public void onClick(View v) {
													// TODO Auto-generated method stub

													Intent intent = new Intent();
													intent.putExtra("cid",imgId);
													intent.setClass(ReaderActivity.this, PictureActivity.class);
													startActivity(intent);
												}
											});

									}
								}.run();

							} else{

								AVQuery<AVObject> query = new AVQuery<AVObject>("images");
								query.getInBackground(imgId, new GetCallback<AVObject>() {

									@Override
									public void done(AVObject arg0, AVException arg1) {
										// TODO Auto-generated method stub
										AVFile avoFile=arg0.getAVFile("File");
										avoFile.getDataInBackground(new GetDataCallback() {

											@Override
											public void done(byte[] arg0, AVException arg1) {
												// TODO Auto-generated method stub
												final Bitmap newBmp=BitmapUtils.compressByKByte(arg0, 200, 500, 500);
												newImgView.setImageBitmap(newBmp);
												if (haveLoadImg==false) {
													toolbarIv.setImageBitmap(newBmp);
													haveLoadImg=true;
												}
												final byte[] fnlData=arg0;
												class  MySaveHandler  extends  Handler{
											        public  MySaveHandler() {}
											         public  MySaveHandler(Looper looper){
											             super (looper);
											        }

											         public   void  handleMessage(Message msg){
											        	 //....这里运行耗时的过程
											        	 appHelper.saveMyBitmap(BitmapUtils.compressByKByte(fnlData, 200, 1500, 1500),"/SDFocus/cache/"+imgId+".cache",false);

											        }
											    }

												HandlerThread handlerThread = new  HandlerThread( "backgroundThread" );
												handlerThread.start();
												MySaveHandler myHandler = new  MySaveHandler(handlerThread.getLooper());
												 Message msg = myHandler.obtainMessage();
												//....此处对msg进行赋值，可以创建一个Bundle进行承载
												msg.sendToTarget();

												newImgView.setOnClickListener(new OnClickListener() {

													public void onClick(View v) {
														// TODO Auto-generated method stub
														Intent intent = new Intent();
														intent.putExtra("cid",imgId);
														intent.setClass(ReaderActivity.this, PictureActivity.class);
														startActivity(intent);
													}
												});

											}
										}, new ProgressCallback() {

											@Override
											public void done(Integer arg0) {
												// TODO Auto-generated method stub

											}
										});


									}
								});
							}
							}
						}
						else {
							TextView newTv=new TextView(ReaderActivity.this);
							newTv.setTextIsSelectable(true);
							newTv.setText(messageStrings[j]);
							readerLinearLayout.addView(newTv);
						}
					}


				}
			}
		});
	}





	public void onShareClick(View v){
		/*

		int scrollViewHeight=0;
		for (int i = 0; i < scollView.getChildCount(); i++) {
			scrollViewHeight += scollView.getChildAt(i).getHeight();
			scollView.getChildAt(i).setBackgroundResource(R.drawable.white);
		}
		final int fnlscrollViewHeight=scrollViewHeight;

		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap bitmap = null;
				bitmap = Bitmap.createBitmap(scollView.getWidth(), fnlscrollViewHeight,Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);
				scollView.draw(canvas);
				Calendar c = Calendar.getInstance();
				String path="/SDFocus/pictures/"+ c.getTimeInMillis();
				ApplicationHelper.saveMyBitmap(bitmap, path);

				Tencent mTencent = Tencent.createInstance("1105167874",ReaderActivity.this.getApplicationContext());
				final Bundle params = new Bundle();
				params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
				params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "SDapp分享功能测试");
				ArrayList<String> imgs=new ArrayList<String>();
				imgs.add("https://www.baidu.com/img/bd_logo1.png");
				params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, imgs);
				mTencent.publishToQzone(ReaderActivity.this, params, qZoneShareListener);

			}
		}).start();
		*/
		Snackbar.make(fab,"程序猿正在刻苦钻研分享功能",Snackbar.LENGTH_SHORT).show();
	}

	IUiListener qZoneShareListener = new IUiListener() {

		@Override
		public void onCancel() {
			Log.i("SRE", "onCancel:test ");
		}

		@Override
		public void onError(UiError e) {
			// TODO Auto-generated method stub
			Log.i("SRE", "onError: " + e.errorMessage);
		}

		@Override
		public void onComplete(Object response) {
			// TODO Auto-generated method stub
			Log.i("SRE", "onComplete: " + response.toString());
		}

	};


	public void onThisUserClick(View v){
		if (!userString.equals("0")) {
			Intent intent = new Intent();
			intent.setClass(ReaderActivity.this, UserActivity.class);
			intent.putExtra("cid", userString);
			intent.putExtra("type", 1);
			startActivity(intent);
		}
	}

	public void onReadComment(View v){

		Intent intent = new Intent();
		intent.putExtra("cid",cid+"");
		intent.putExtra("ClassName", className);
		String ttl=titleView.getText().toString();
		if (ttl.length()<=5)
			intent.putExtra("title", ttl + "");
		else intent.putExtra("title", ttl.substring(0,4) + "...");
		intent.setClass(ReaderActivity.this, CommentActivity.class);
		startActivity(intent);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reader, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_share){
			onShareClick(null);
		}else if (id==R.id.action_delete_reader){
			if ((AVUser.getCurrentUser().get("level") == 0) || (AVUser.getCurrentUser().get("level") == 3) || (AVUser.getCurrentUser().getObjectId().equals(thisWriterAvuer.getObjectId()))) {

				AVQuery<AVObject> deleteQuery = new AVQuery<AVObject>("main");
				deleteQuery.getInBackground(cid, new GetCallback<AVObject>() {
					@Override
					public void done(AVObject avObject, AVException e1) {
						if (e1 == null) {
							avObject.deleteInBackground(new DeleteCallback() {
								@Override
								public void done(AVException e2) {
									if (e2 != null)
										Snackbar.make(fab, "删除未完成", Snackbar.LENGTH_SHORT).show();
								}
							});
						}
					}
				});

				readAvo.deleteInBackground(new DeleteCallback() {
					@Override
					public void done(AVException e) {
						if (e == null) {
							Toast.makeText(ReaderActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
							ReaderActivity.this.finish();
						} else
							Snackbar.make(fab, "删除失败", Snackbar.LENGTH_SHORT).show();
					}
				});


			}else Snackbar.make(fab, "没有权限", Snackbar.LENGTH_SHORT).show();
		}else if (id==R.id.action_jingxuan_reader){
			if ((AVUser.getCurrentUser().get("level") == 0) || (AVUser.getCurrentUser().get("level") == 3)) {

				AVObject postb = new AVObject("main");
				postb.put("idstring", cid);
				postb.put("class", className);
				postb.saveInBackground(new SaveCallback() {
				@Override
				public void done(AVException e) {
						if (e == null) {
							Snackbar.make(fab, "成功！", Snackbar.LENGTH_SHORT).show();
						} else
							Snackbar.make(fab, "出错啦！", Snackbar.LENGTH_SHORT).show();
					}
				});
			} else Snackbar.make(fab, "没有权限", Snackbar.LENGTH_SHORT).show();
		}
		return super.onOptionsItemSelected(item);
	}


}
