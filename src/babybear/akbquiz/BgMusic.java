package babybear.akbquiz;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BgMusic extends Service {
	private static final String TAG = "BgMusic";
	public static final int MODE_LOOP = 0, MODE_RANDOM = 1, MODE_SINGLE = 2;

	private MediaPlayer player;
	private AudioManager am;
	private ActivityManager activityManager;
	private int PlayingNo = 0;
	private int playlistLength = 0;

	private int mode = MODE_LOOP;
	private Uri[] playlist;
	private Random r = new Random();
	private boolean isOn = true;
	private boolean isRunning;
	private boolean isScreenOn;
	private String packageName;

	static BGHandler bgHandler;
	private BroadcastReceiver receiverScreen;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	OnCompletionListener CompL = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			switch (mode) {
			case MODE_LOOP:
				PlayingNo++;
				if (PlayingNo >= playlistLength) {
					PlayingNo = 0;
				}
				break;
			case MODE_RANDOM:
				PlayingNo = r.nextInt(playlistLength);
				break;
			case MODE_SINGLE:

				break;
			}
			player.release();
			Log.d(TAG, "playnext isPlayNo = " + PlayingNo);

			try {
				if (playlistLength > 0) {
					player = MediaPlayer.create(BgMusic.this,
							playlist[PlayingNo]);
					player.setLooping(false);
					player.setOnCompletionListener(CompL);
					if (isOn) {
						player.start();
					}
				}
			}
			catch (IllegalArgumentException e) {
				errorToast(e);
				e.printStackTrace();
			}
			catch (SecurityException e) {
				errorToast(e);
				e.printStackTrace();
			}
			catch (IllegalStateException e) {
				errorToast(e);
				e.printStackTrace();
			}

			player.setOnCompletionListener(CompL);
			player.setOnErrorListener(ErrL);

			Log.d(TAG, "play start");
		}

	};

	OnErrorListener ErrL = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Toast.makeText(BgMusic.this,
					"Unexpected media player error.",
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Unexpected media player error.");
			return false;
		}
	};

	void errorToast(Exception e) {
		Toast.makeText(BgMusic.this,
				"Unexpected error in: " + TAG + " . error:" + e.getMessage(),
				Toast.LENGTH_SHORT).show();
	}

	public boolean isAppOnForeground() {

		// Returns a list of application processes that are running on the
		// device
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null) {
			return false;
		}

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	public void setPlaylist(String uristr) {
		playlistLength = 0;
		try {
			JSONArray arr = new JSONArray(uristr);
			playlistLength = arr.length();
			if (playlistLength == 0) {
				return;
			}
			playlist = new Uri[playlistLength];
			for (int i = 0; i < playlistLength; i++) {
				playlist[i] = Uri.parse(arr.getString(i));
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "++onCreate++");
		super.onCreate();
		bgHandler = new BGHandler(this);
		isRunning = true;
		isScreenOn = true;
		activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "+onStart+");

		isOn = intent.getBooleanExtra(Database.KEY_switch_bg, true);

		setPlaylist(getSharedPreferences("config", Context.MODE_PRIVATE).getString(Database.ColName_playlist,
				""));
		am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,
				intent.getIntExtra(Database.KEY_vol_bg, 10),
				0);

		if (playlist != null) {
			player = MediaPlayer.create(this, playlist[PlayingNo]);
			player.setLooping(false);
			player.setOnCompletionListener(CompL);
			if (isOn) {
				player.start();
			}
		}

		// 注册锁屏广播接收
		receiverScreen = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				final String action = intent.getAction();
				if (Intent.ACTION_SCREEN_ON.equals(action)) {
					Log.d(TAG, "SCREEN ON");
					if (isOn) {
						if (player != null && !player.isPlaying()) {
							player.start();
						}
					}
					isScreenOn = true;
				} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
					Log.d(TAG, "SCREEN OFF");
					if (player != null && player.isPlaying()) {
						player.pause();
					}
					isScreenOn = false;
				}

			}
		};

		registerReceiver(receiverScreen,
				new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(receiverScreen,
				new IntentFilter(Intent.ACTION_SCREEN_OFF));

		// 开始监测线程
		packageName = getPackageName();
		new Thread() {
			@Override
			public void run() {
				Message msg = new Message();
				try {
					while (isRunning) {
						msg = bgHandler.obtainMessage();
						if (isScreenOn && isAppOnForeground()) {
							msg.what = BGHandler.PLAY;
						} else {
							msg.what = BGHandler.PAUSE;
						}
						bgHandler.sendMessage(msg);
						Thread.sleep(1000);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		return START_NOT_STICKY ;
	}

	@Override
	public void onDestroy() {
		// Log.d(TAG, "--onDestroy--");

		isRunning = false;
		bgHandler.removeCallbacksAndMessages(null);
		if (player != null) {
			player.release();
		}
		this.unregisterReceiver(receiverScreen);

	}

	static public class BGHandler extends Handler {
		public static final int VOL_CHANGE = 1, SWITCH_CHANGE = 2,
				LOOP_CHANGE = 3, PLAYLIST_CHANGE = 4, PAUSE = -1, PLAY = 0;

		WeakReference<BgMusic> mService;

		public BGHandler(BgMusic service) {
			mService = new WeakReference<BgMusic>(service);
		}

		public BGHandler(BgMusic service, Looper L) {
			super(L);
			mService = new WeakReference<BgMusic>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			BgMusic theService = mService.get();
//			Log.d(BgMusic.TAG, "msg.arg1 : " + msg.arg1 + " msg.what : "
//					+ msg.what);
			switch (msg.what) {
			case PAUSE:
				if (theService.player != null) {
					if (theService.player.isPlaying()) {
						theService.player.pause();
					}
				}
				break;
			case PLAY:
				if (theService.player != null) {
					if (!theService.player.isPlaying()) {
						if (theService.isOn) {
							theService.player.start();
						}
					}
				}
				break;

			case VOL_CHANGE:
				theService.am.setStreamVolume(AudioManager.STREAM_MUSIC,
						msg.arg1,
						0);
				break;
			case SWITCH_CHANGE:
				switch (msg.arg1) {
				case 0:
					theService.isOn = false;
					if (theService.player != null) {
						theService.player.pause();
					}
					break;
				case 1:
					theService.isOn = true;
					if (theService.player != null) {
						theService.player.start();
					}
					break;
				}
				break;
			case LOOP_CHANGE:
				theService.mode = msg.arg1;
				break;
			case PLAYLIST_CHANGE:
				if (theService.player != null) {
					if (theService.player.isPlaying()) {
						theService.player.stop();
					}
					theService.player.release();
				}

				theService.PlayingNo = 0;
				theService.setPlaylist(theService.getSharedPreferences("config",
						Context.MODE_PRIVATE)
						.getString(Database.ColName_playlist, ""));

				if (theService.playlistLength > 0) {
					if (theService.player != null
							|| !theService.player.isPlaying()) {

						theService.player = MediaPlayer.create(theService,
								theService.playlist[theService.PlayingNo]);
						if (theService.isOn) {
							theService.player.start();
						}

						theService.player.setLooping(false);
						theService.player.setOnCompletionListener(theService.CompL);
					}
				}


				break;
			}
			super.handleMessage(msg);

		}
	}

}
