package babybear.akbquiz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Welcome2 extends Activity {
	private static String envFilePath = null;

	private static final String KEY_APPVER_CODE = "app_ver_code";
	private static final String KEY_APPVER_NAME = "app_ver_name";
	private static final String KEY_DATAVER = "database_ver";
	private static final String KEY_SDK_CODE = "api_level";
	private static final String KEY_SDK_NAME = "android_ver";
	private static final String KEY_DEVICE = "device";
	private static final String KEY_BRADN = "brand";

	public static final int DATA_VER = 2;

	private boolean isSavingEnvFile = false;
	private boolean isSavingDatabase = false;
	private boolean isInitializing = false;

	Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fanhuashe1);
		envFilePath = Environment.getExternalStorageDirectory().getPath()
				+ "/Android/data/" + getPackageName() + "/env.json";

		verCheck();
		logo();
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 在欢迎界面屏蔽BACK键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return false;
	}

	/**
	 * 输出Logcat到文件
	 */
	public static void saveReport(Context ctx) {
		Log.d("", "正在输出日志文件");

		try {
			Properties mDeviceCrashInfo = collectCrashDeviceInfo(ctx);
			Process process;
			process = Runtime.getRuntime().exec("logcat -d | grep "
					+ android.os.Process.myPid());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			long timestamp = System.currentTimeMillis();
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath()
					+ "/Android/data/"
					+ ctx.getPackageName()
					+ "/crash-" + timestamp + ".crashreport");
			FileOutputStream os = new FileOutputStream(file);
			mDeviceCrashInfo.store(os, (new Date()).toString());

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			bw.close();

			// ctx..post();
			// Toast.makeText(ctx, "输出日志文件为:"+file.getPath(),
			// Toast.LENGTH_SHORT).show();

			Log.d("", "输出日志文件为:" + file.getPath());
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取设备的信息
	 * 
	 * @param ctx
	 * @return
	 */
	public static Properties collectCrashDeviceInfo(Context ctx) {
		Properties mDeviceCrashInfo = new Properties();
		final String VERSION_NAME = "versionName";
		final String VERSION_CODE = "versionCode";
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				mDeviceCrashInfo.put(VERSION_NAME,
						pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE,
						Integer.toString(pi.versionCode));
			}
		}
		catch (NameNotFoundException e) {
			Log.e("", "Error while collect package info", e);
		}
		// 使用反射来收集设备信息.在Build类中包含各种设备信息,
		// 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), field.get(null)
						.toString());
				// Log.d(TAG, field.getName() + " : " + field.get(null));
			}
			catch (Exception e) {
				Log.e("", "Error while collect crash info", e);
			}

		}
		return mDeviceCrashInfo;

	}

	/**
	 * 检查是否是首次运行 数据库版本是否是最新
	 */
	private void verCheck() {
		new Thread() {
			public void run() {
				File envFile = new File(envFilePath);
				if (envFile.exists()) {
					SharedPreferences sp_cfg = getSharedPreferences("config",
							Context.MODE_PRIVATE);
					if (!sp_cfg.getBoolean(Database.KEY_normal_exit, false)) {
						new Thread() {
							@Override
							public void run() {
								saveReport(Welcome2.this);
							}
						}.start();
					}

					Editor editor = sp_cfg.edit();
					editor.putBoolean(Database.KEY_normal_exit, false);
					editor.commit();
					
					try {
						BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
						String jsonString = buffer.readLine();
						buffer.close();
						JSONObject obj = new JSONObject(jsonString);
						int dataver = obj.getInt(KEY_DATAVER);
						if (dataver < Database.quizdb_ver) {
							saveEnvFile(envFile);
							copyDatabase();
						}

					}
					catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					envFile.getParentFile().mkdirs();
					saveEnvFile(envFile);
				}
				if (!(new File(Database.databasePath)).exists()) {
					Log.d("", "database file not exists");
					copyDatabase();
					firstRun();
				}
			}

		}.start();

	}

	/**
	 * 首次运行 配置设置文件
	 */
	private void firstRun() {
		Log.d("", "Initializing");
		isInitializing = true;

		SharedPreferences sp_cfg = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Editor cfg_Editor = sp_cfg.edit();
		cfg_Editor.putBoolean(Database.KEY_switch_bg, true);
		cfg_Editor.putBoolean(Database.KEY_switch_sound, true);
		cfg_Editor.putBoolean(Database.KEY_switch_vibration, true);
		cfg_Editor.putInt(Database.KEY_vol_bg, 10);
		cfg_Editor.putInt(Database.KEY_vol_sound, 10);
		cfg_Editor.putString(Database.ColName_playlist, "{}");
		cfg_Editor.putBoolean(Database.KEY_use_custom_background, false);
		cfg_Editor.putInt(Database.KEY_tips_info, 0);
		cfg_Editor.putInt(Database.KEY_tips_quiz, 0);

		cfg_Editor.commit();
		isInitializing = false;
	}

	/**
	 * 保存环境配置文件
	 * 
	 * @param file 环境配置文件
	 */
	private void saveEnvFile(File file) {
		Log.d("", "SavingEnvFile");
		isSavingEnvFile = true;

		PackageInfo info;
		try {
			info = this.getPackageManager()
					.getPackageInfo(this.getPackageName(), 0);
			int verCode = info.versionCode;
			String verName = info.versionName;

			JSONObject obj = new JSONObject();
			obj.put(KEY_APPVER_CODE, verCode);
			obj.put(KEY_APPVER_NAME, verName);
			obj.put(KEY_DATAVER, Database.quizdb_ver);

			obj.put(KEY_DEVICE, Build.PRODUCT);
			obj.put(KEY_BRADN, Build.BRAND);
			obj.put(KEY_SDK_CODE, Build.VERSION.SDK_INT);
			obj.put(KEY_SDK_NAME, Build.VERSION.RELEASE);

			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			writer.write(obj.toString());
			writer.flush();
			writer.close();

			isSavingEnvFile = false;
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 复制数据库
	private void copyDatabase() {
		Log.d("", "SavingDatabase");
		isSavingDatabase = true;

		AssetManager am = this.getAssets();
		File fileout = new File(Database.databasePath);
		if (fileout.exists()) {
			fileout.delete();
		}
		File parent = fileout.getParentFile();
		if (!parent.exists()) {
			Log.d("", parent.getPath() + " is not exist");
			parent.mkdirs();
		}

		try {
			InputStream is = am.open("q.db");

			fileout.createNewFile();
			FileOutputStream os = new FileOutputStream(fileout);

			int temp = 0;
			temp = is.read();
			while (temp != -1) {
				os.write(temp);
				temp = is.read();
			};

			os.flush();
			os.close();
			is.close();

			isSavingDatabase = false;

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * LOGO的进入动画
	 */
	private void logo() {
		setContentView(R.layout.fanhuashe1);

		View petal = findViewById(R.id.petal);
		Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.logo_petal);
		anim1.setFillEnabled(true); // 启动Fill保持
		anim1.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		petal.setAnimation(anim1);
		anim1.startNow();

		View text = findViewById(R.id.text);
		Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.logo_text);
		anim2.setFillEnabled(true); // 启动Fill保持
		anim2.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		text.setAnimation(anim2);

		anim2.startNow();
		View smalltext =findViewById(R.id.smalltext);
		Animation anim3 = AnimationUtils.loadAnimation(this,
				R.anim.logo_smalltext);
		anim3.setFillEnabled(true); // 启动Fill保持
		anim3.setFillAfter(true); // 设置动画的最后一帧是保持在View上面
		smalltext.setAnimation(anim3);
		anim3.startNow();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				logo_fadeout();
			}
		}, anim3.getDuration() + anim3.getStartOffset());

	}

	/**
	 * LOGO的淡出动画
	 */
	private void logo_fadeout() {
		ViewGroup layout = (ViewGroup) findViewById(R.id.full);
		Animation anim3 = AnimationUtils.loadAnimation(this,
				R.anim.wait_fade_out);
		//anim3.setFillEnabled(true); // 启动Fill保持
		//anim3.setFillAfter(true); // 设置动画的最后一帧是保持在View上面

		layout.setAnimation(anim3);
		anim3.startNow();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				pregame();
			}
		}, anim3.getDuration() + anim3.getStartOffset());
	}

	/**
	 * 进入游戏之前的动画
	 */
	private void pregame() {
		setContentView(R.layout.activity_welcome);
		//ViewGroup layout = (ViewGroup) findViewById(R.id.full);

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

//		anim.setFillEnabled(true);
//		anim.setFillAfter(true);
		anim.startNow();

		Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.twinkling);
		TextView clickToStart = (TextView) findViewById(R.id.click_to_start);
		clickToStart.setAnimation(anim1);
		anim1.startNow();

		findViewById(R.id.full).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isInitializing && !isSavingDatabase && !isSavingEnvFile) {
					twink();
				}
			}

		});
	}

	/**
	 * 闪烁的动画
	 */
	private void twink() {
		Animation anim2 = AnimationUtils.loadAnimation(this,
				R.anim.twinkling_quick);

		findViewById(R.id.click_to_start).setAnimation(anim2);

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(Welcome2.this, MainMenu.class);
				startActivity(intent);
				Welcome2.this.finish();
			}
		}, anim2.getDuration());
		anim2.startNow();
	}
}
