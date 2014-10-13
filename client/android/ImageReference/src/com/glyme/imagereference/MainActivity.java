package com.glyme.imagereference;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			switch (position) {
			case 0:
				fragment = new MainSectionFragment();
				break;
			case 1:
				fragment = new HistorySectionFragment();
				break;
			default:
				fragment = new MainSectionFragment();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * Fragment to display main functions
	 */
	public static class MainSectionFragment extends Fragment {

		private static final int RESULT_LOAD_IMAGE = 1;
		private static final int RESULT_TAKE_SHOT = 2;
		private static final int INFO_RECEIVED = 1;

		private View rootView;
		private ImageView iv1;

		private String filePath;

		@Override
		public void onActivityResult(int requestCode, int resultCode,
				Intent data) {
			super.onActivityResult(requestCode, resultCode, data);

			// handle load image result
			if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
					&& null != data) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = rootView.getContext().getContentResolver()
						.query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);
				cursor.close();

				ImageView iv1 = (ImageView) rootView
						.findViewById(R.id.imageView1);
				iv1.setImageBitmap(BitmapFactory.decodeFile(filePath));
			} else if (requestCode == RESULT_TAKE_SHOT
					&& resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				Bitmap img = (Bitmap) bundle.get("data");

				// write to cache dir
				try {
					String path = MainSectionFragment.this.getActivity()
							.getCacheDir().getPath();
					filePath = path + "/" + System.currentTimeMillis() + ".jpg";

					File tmpFile = new File(filePath);
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(tmpFile));

					img.compress(Bitmap.CompressFormat.JPEG, 80, bos);
					bos.flush();
					bos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// set imageview
				ImageView iv1 = (ImageView) rootView
						.findViewById(R.id.imageView1);
				iv1.setImageBitmap(img);
			}
		}

		// handle server response and show matched info
		Handler imageInfoShow = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MainSectionFragment.INFO_RECEIVED:
					// retrive json
					String json = msg.getData().getString("info");

					// find controls
					TextView tvUrl = (TextView) rootView
							.findViewById(R.id.tvUrl);
					TextView tvDesc = (TextView) rootView
							.findViewById(R.id.tvDesc);
					ImageView iv2 = (ImageView) rootView
							.findViewById(R.id.imageView2);

					// parse json
					JSONTokener jt = new JSONTokener(json);
					JSONObject info;
					try {
						info = (JSONObject) jt.nextValue();

						String url = info.optString("url");
						String describe = info.optString("describe");
						String imgbase64 = info.optString("img");

						tvUrl.setText("Url: " + url);
						tvDesc.setText("Describe: " + describe);

						// decode image encoded by base64
						byte[] imgdata = Base64.decode(imgbase64,
								Base64.DEFAULT);
						Bitmap b = BitmapFactory.decodeByteArray(imgdata, 0,
								imgdata.length);
						iv2.setImageBitmap(Bitmap.createScaledBitmap(b, 117, 208, false));

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

		// thread used to upload picture
		Runnable fileUploader = new Runnable() {

			@Override
			public void run() {
				try {
					// create httpclient & httppost
					HttpClient httpClient = new DefaultHttpClient();
					HttpPost request = new HttpPost(
					// "http://gimgdetector.duapp.com/index.php");
							"http://192.168.1.101/IR/index.php");

					// using multipart/form-data
					MultipartEntity reqEntity = new MultipartEntity(
							HttpMultipartMode.BROWSER_COMPATIBLE);

					// add img part
					reqEntity.addPart("img", new FileBody(new File(filePath),
							"image/*"));
					request.setEntity(reqEntity);

					// waiting for response
					HttpResponse response = httpClient.execute(request);

					// read response
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();
					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}

					// send message
					Message msg = new Message();
					msg.what = MainSectionFragment.INFO_RECEIVED;
					Bundle b = new Bundle();
					b.putString("info", s.toString());
					msg.setData(b);
					imageInfoShow.sendMessage(msg);

					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Button btnSelectPic = (Button) rootView
					.findViewById(R.id.btnSelectPic);
			Button btnUploadPic = (Button) rootView
					.findViewById(R.id.btnUploadPic);
			Button btnShot = (Button) rootView.findViewById(R.id.btnShot);

			iv1 = (ImageView) rootView.findViewById(R.id.imageView1);
			// start intent to select image
			btnSelectPic.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, RESULT_LOAD_IMAGE);
				}
			});

			btnUploadPic.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new Thread(fileUploader).start();
				}
			});

			btnShot.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(
							"android.media.action.IMAGE_CAPTURE");
					startActivityForResult(intent, RESULT_TAKE_SHOT);
				}
			});
			return rootView;
		}
	}

	/**
	 * Fragment to display history panel
	 */
	public static class HistorySectionFragment extends Fragment {

		public HistorySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_history,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText("好吧");
			return rootView;
		}
	}
}
