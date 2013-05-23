package com.gnod.geekr.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.gnod.geekr.R;
import com.gnod.geekr.tool.ImageHelper;
import com.gnod.geekr.tool.StringUtils;
import com.gnod.geekr.tool.ToastHelper;
import com.gnod.geekr.tool.fetcher.BaseFetcher;
import com.gnod.geekr.tool.fetcher.BaseFetcher.FetchCompleteListener;
import com.gnod.geekr.tool.fetcher.StatusFetcher;

public class PostStatusActivity extends BaseActivity {

	private static final int SEND_REPLY = 0;
	private static final int SEND_RETWEET = 1;
	private static final int SEND_COMMENT = 2;
	private static final int SEND_NEW = 3;

	private static final int TYPE_AT = 0;
	private static final int TYPE_PICK_PIC = 1;
	private static final int TYPE_TAKE_PIC = 2;

	private View btnAtView;
	private View btnTopicView;
	private View btnEmotionView;
	private CheckBox retweetCheckBox;
	private EditText editor;
	private TextView btnWordCount;
	private String statusId;
	private ProgressDialog progressDialog;

	private int sendType;
	private String commentId;
	private View btnLocView;
	private View btnPicView;
	private View emotionLayout;
	private GridView emotionGridView;
	private View imageLayout;
	private ImageView imageView;
	private Uri takePicUri;
	private String type;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);

		initView();
		registerListener();
		bindView();
	}

	private void initView() {

		editor = (EditText) findViewById(R.id.view_post_edittext);
		retweetCheckBox = (CheckBox) findViewById(R.id.checkbox_retweet);
		btnEmotionView = findViewById(R.id.btn_post_emotion);
		btnTopicView = findViewById(R.id.btn_post_topic);
		btnAtView = findViewById(R.id.btn_post_at);
		btnPicView = findViewById(R.id.btn_post_pic);
		btnLocView = findViewById(R.id.btn_post_location);
		btnWordCount = (TextView) findViewById(R.id.btn_post_words_count);

		imageLayout = findViewById(R.id.layout_post_image);
		imageView = (ImageView) findViewById(R.id.view_post_image);

		emotionLayout = findViewById(R.id.layout_emotion);
		emotionGridView = (GridView) findViewById(R.id.grid_emotion_thumb);

		progressDialog = new ProgressDialog(PostStatusActivity.this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_cmt, menu);
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& emotionLayout.getVisibility() == View.VISIBLE) {
			emotionLayout.setVisibility(View.GONE);
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_BACK && editor.getText().length() > 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("确定放弃当前操作?");
			builder.setPositiveButton("确定", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("取消", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_cmt_send:
			if (editor.getText().length() > 140) {
				ToastHelper.show("字数超过140");
			} else if (editor.getText().length() == 0
					&& !type.equalsIgnoreCase("retweet")) {
				ToastHelper.show("内容为空");
			} else {
				send();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void registerListener() {
		editor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int remainCount = 140 - s.length();
				btnWordCount.setText(String.valueOf(remainCount));
			}
		});
		editor.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (emotionLayout.getVisibility() == View.VISIBLE) {
					emotionLayout.setVisibility(View.GONE);
					return true;
				}
				return false;
			}
		});
		btnWordCount.setOnClickListener(clickListener);
		btnEmotionView.setOnClickListener(clickListener);
		btnTopicView.setOnClickListener(clickListener);
		btnAtView.setOnClickListener(clickListener);
		retweetCheckBox.setOnClickListener(clickListener);
		btnPicView.setOnClickListener(clickListener);
		btnLocView.setOnClickListener(clickListener);
		imageView.setOnClickListener(clickListener);

		emotionGridView.setAdapter(new ImageAdapter(this));
		emotionGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CharSequence emotionTag = (CharSequence) view.getTag();
				int start = editor.getSelectionStart();
				int end = editor.getSelectionEnd();
				int startPos = Math.min(start, end);
				int endPos = startPos + emotionTag.length();

				editor.getText().replace(Math.min(start, end),
						Math.max(start, end), emotionTag);

				// convert emotion tag to thumb
				InputStream input;
				try {
					input = getAssets().open(
							"smileys" + File.separator
									+ ImageHelper.emotionsMap.get(emotionTag));
					Bitmap bitmap = BitmapFactory.decodeStream(input);
					bitmap = Bitmap.createScaledBitmap(bitmap, 42, 42, true);

					ImageSpan imageSpan = new ImageSpan(
							PostStatusActivity.this, bitmap);
					editor.getText().setSpan(imageSpan, startPos, endPos,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void togleSoftInput(boolean flag) {
		InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (flag) {
			manager.showSoftInput(editor, 0);
		} else {
			manager.hideSoftInputFromWindow(editor.getWindowToken(), 0);
		}
	}

	private void bindView() {
		Intent intent = getIntent();
		String content = intent.getStringExtra("Content");
		if (!StringUtils.isNullOrEmpty(content)) {
			editor.setText(content);
			editor.setSelection(0);

			btnWordCount.setText(String.valueOf(140 - content.length()));
		}
		type = intent.getStringExtra("Type");
		if (type.equalsIgnoreCase("comment")) {
			sendType = SEND_COMMENT;
			setTitle("评论");
			retweetCheckBox.setText("同时转发");
			retweetCheckBox.setVisibility(View.VISIBLE);
		} else if (type.equalsIgnoreCase("retweet")) {
			sendType = SEND_RETWEET;
			setTitle("转发");
			retweetCheckBox.setText("同时评论");
			retweetCheckBox.setVisibility(View.VISIBLE);
		} else if (type.equalsIgnoreCase("replyComment")) {
			sendType = SEND_REPLY;
			setTitle("回复评论");
			retweetCheckBox.setText("同时转发");
			retweetCheckBox.setVisibility(View.GONE);
		} else if (type.equalsIgnoreCase("PostStatus")) {
			sendType = SEND_NEW;
			setTitle("发微博");
			btnPicView.setVisibility(View.VISIBLE);
			retweetCheckBox.setVisibility(View.GONE);
			return;
		}

		commentId = intent.getStringExtra("CommentID");
		statusId = intent.getStringExtra("StatusID");
		if (StringUtils.isNullOrEmpty(statusId)) {
			throw new NullPointerException(
					"the invoke activity should include extra value 'StatusID'");
		}
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnWordCount) {
				if (editor.getText().length() == 0)
					return;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						PostStatusActivity.this);
				builder.setMessage("确认清除文字？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								editor.setText("");
								btnWordCount.setText("140");
								dialog.dismiss();
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			} else if (v == btnEmotionView) {
				if (emotionLayout.getVisibility() == View.VISIBLE)
					emotionLayout.setVisibility(View.GONE);
				else {
					togleSoftInput(false);
					emotionLayout.setVisibility(View.VISIBLE);
				}

			} else if (v == btnTopicView) {
				int start = editor.getSelectionStart();
				int end = editor.getSelectionEnd();
				editor.getText().replace(Math.min(start, end),
						Math.max(start, end), "##");
				editor.setSelection(Math.min(start, end) + 1);
			} else if (v == btnAtView) {
				Intent intent = new Intent(PostStatusActivity.this,
						AtUserActivity.class);
				intent.putExtra("Type", "AtUser");
				startActivityForResult(intent, TYPE_AT);
			} else if (v == btnPicView) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());
				String[] items = { "本地相册", "立即拍照" };
				builder.setItems(items, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent pickIntent = new Intent(Intent.ACTION_PICK);
							pickIntent.setType("image/*");
							startActivityForResult(pickIntent, TYPE_PICK_PIC);
							break;
						case 1:
							Intent takeIntent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							File file = new File(File.separator + "sdcard"
									+ File.separator + "geekr_"
									+ System.currentTimeMillis() + ".jpg");
							takePicUri = Uri.fromFile(file);
							takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,
									takePicUri);
							startActivityForResult(takeIntent, TYPE_TAKE_PIC);
							break;
						default:
							break;
						}
					}
				});
				builder.show();

			} else if (v == btnLocView) {

			} else if (v == imageView) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						v.getContext());
				builder.setMessage("是否移除当前图片");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						imageView.setTag(null);
						imageLayout.setVisibility(View.GONE);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
			}
		}

	};

	private void send() {
		StatusFetcher fetcher = new StatusFetcher();
		
		String content = editor.getText().toString();
		if (sendType == SEND_COMMENT) {
			if (retweetCheckBox.isChecked()) {
				// 评论同时转发 == 转发同时评论当前微博
				fetcher.retweetStatus(statusId, content, true, fetchListener);
			} else {
				fetcher.commentStatus(statusId, content, false, fetchListener);
			}
		} else if (sendType == SEND_REPLY) {
			fetcher.replyComment(commentId, statusId, content, fetchListener);
		} else if (sendType == SEND_RETWEET) {
			fetcher.retweetStatus(statusId, content, retweetCheckBox.isChecked(),
					fetchListener);
		} else if (sendType == SEND_NEW) {
			String filePath = (String) imageView.getTag();
			fetcher.sendStatus(content, filePath, "0.0", "0.0", fetchListener);
		}
		progressDialog.setMessage("正在发送...");
		progressDialog.show();

	}
	
	private FetchCompleteListener fetchListener =  new FetchCompleteListener() {
		@Override
		public void fetchComplete(int state, int errorCode, Object obj) {
			if(state == BaseFetcher.FETCH_SUCCEED_NEWS) {
				progressDialog.dismiss();
				if (sendType == SEND_COMMENT)
					ToastHelper.show("发送成功");
				else if (sendType == SEND_RETWEET)
					ToastHelper.show("转发成功");
				finish();
			} else {
				progressDialog.dismiss();
				ToastHelper.show("操作失败");
			}
		}
	};

	class ImageAdapter extends BaseAdapter {

		private Context mContext;
		private String[] thumbArray;

		public ImageAdapter(Context context) {
			mContext = context;
			thumbArray = ImageHelper.emotionsMap.keySet().toArray(new String[] {});
		}

		@Override
		public int getCount() {
			return thumbArray.length;
		}

		@Override
		public Object getItem(int position) {
			return thumbArray[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(60, 60));
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}
			String source = ImageHelper.emotionsMap.get(thumbArray[position]);
			InputStream input;
			try {
				input = mContext.getAssets().open(
						"smileys" + File.separator + source);
				Bitmap bitmap = BitmapFactory.decodeStream(input);
				imageView.setImageBitmap(bitmap);
				imageView.setTag(thumbArray[position]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return imageView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case TYPE_AT:
			if (resultCode == RESULT_OK) {
				String atTag = "@" + data.getStringExtra("Name") + " ";
				editor.append(atTag);
			}
			break;
		case TYPE_PICK_PIC:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				try {
					InputStream input = getContentResolver().openInputStream(
							uri);
					Bitmap bitmap = BitmapFactory.decodeStream(input);
					imageView.setImageBitmap(bitmap);
					imageLayout.setVisibility(View.VISIBLE);
					imageView.setTag(getRealPathFromURI(uri));
				} catch (FileNotFoundException e) {
					Log.e("error", e.getMessage());
				}
			}
			break;
		case TYPE_TAKE_PIC:
			if (resultCode == RESULT_OK) {
				if (takePicUri != null) {
					try {
						InputStream stream = getContentResolver()
								.openInputStream(takePicUri);
						Bitmap bitmap = BitmapFactory.decodeStream(stream);
						imageView.setImageBitmap(bitmap);
						imageLayout.setVisibility(View.VISIBLE);
						imageView.setTag(takePicUri.getPath());
					} catch (FileNotFoundException e) {
						Log.e("error", e.getMessage());
					}
				}
			}
		default:
			break;
		}
	}

	private String getRealPathFromURI(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(index);
	}
}
