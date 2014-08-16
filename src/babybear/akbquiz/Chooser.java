package babybear.akbquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Chooser extends Activity {

	private static final String PreferenceName_Quiz = "quiz";

	public static final String KeyName_difficulty = "difficulty";
	public static final String KeyName_mode = "gamemode";
	public static final String KeyName_groups = "groups";

	public static final int GAMEMODE_NORMAL = 0;
	public static final int GAMEMODE_CHALLENGE = 1;

	private static boolean[] GroupIsChoosed = new boolean[Database.GroupOrder_LIMIT];
	// private static int difficulty = 1;

	private boolean isChanged = false;
	private int gameMode = GAMEMODE_NORMAL;

	@Override
	public void onCreate(Bundle InstanceState) {
		super.onCreate(InstanceState);
		setContentView(R.layout.chooser);

		initdata();
		initWidget();

	}

	/**
	 * 拉取数据
	 */

	private void initdata() {
		SharedPreferences sp_quiz = getSharedPreferences(PreferenceName_Quiz,
				Context.MODE_PRIVATE);

		// difficulty = sp_quiz.getInt(KeyName_difficulty, 1);
		for (int i = 0; i < GroupIsChoosed.length; i++) {
			GroupIsChoosed[i] = sp_quiz.getBoolean(Database.GroupNames[i],
					false);
		}
		gameMode = sp_quiz.getInt(KeyName_mode, GAMEMODE_NORMAL);
	}

	/**
	 * 初始化界面
	 */
	private void initWidget() {
		ToggleButton[] groupTButton = new ToggleButton[Database.GroupOrder_LIMIT];
		groupTButton[Database.GroupOrder_AKB48] = (ToggleButton) findViewById(R.id.akb_toggle);
		groupTButton[Database.GroupOrder_SKE48] = (ToggleButton) findViewById(R.id.ske_toggle);
		groupTButton[Database.GroupOrder_NMB48] = (ToggleButton) findViewById(R.id.nmb_toggle);
		groupTButton[Database.GroupOrder_HKT48] = (ToggleButton) findViewById(R.id.hkt_toggle);
		groupTButton[Database.GroupOrder_NGZK46] = (ToggleButton) findViewById(R.id.ngzk_toggle);
		groupTButton[Database.GroupOrder_JKT48] = (ToggleButton) findViewById(R.id.jkt_toggle);
		groupTButton[Database.GroupOrder_SNH48] = (ToggleButton) findViewById(R.id.snh_toggle);

		for (int i = 0; i < groupTButton.length; i++) {
			ToggleButton toggleButton = groupTButton[i];
			if (toggleButton == null) {
				continue;
			}
			toggleButton.setOnClickListener(l);
			toggleButton.setChecked(GroupIsChoosed[i]);

		}

		// RatingBar diff = (RatingBar) findViewById(R.id.difficulty);
		// diff.setRating(difficulty / 2.0f);
		// diff.setOnRatingBarChangeListener(new
		// RatingBar.OnRatingBarChangeListener() {
		//
		// @Override
		// public void onRatingChanged(RatingBar arg0,
		// float rating,
		// boolean fromUser) {
		// if (!fromUser) {
		// return;
		// }
		// difficulty = (int) (rating * 2);
		// isChanged = true;
		//
		// }
		//
		// });

		switch (gameMode) {
		case GAMEMODE_NORMAL:
			((RadioButton) findViewById(R.id.gamemode_normal)).setChecked(true);
			((RadioButton) findViewById(R.id.gamemode_challenge)).setChecked(false);
			break;

		case GAMEMODE_CHALLENGE:
			((RadioButton) findViewById(R.id.gamemode_challenge)).setChecked(true);
			((RadioButton) findViewById(R.id.gamemode_normal)).setChecked(false);
			break;
		}

		RadioGroup gamemodeGroup = (RadioGroup) findViewById(R.id.gamemode);
		gamemodeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.gamemode_challenge:
					gameMode = GAMEMODE_CHALLENGE;
					break;

				case R.id.gamemode_normal:
					gameMode = GAMEMODE_NORMAL;
					break;
				}
			}

		});

		findViewById(R.id.game_start).setOnClickListener(l);

		SharedPreferences sp_cfg = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		if (sp_cfg.getBoolean(Database.KEY_use_custom_background, false)) {
			findViewById(R.id.chooser_body).setBackgroundDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory()
					.getPath()
					+ "/Android/data/" + getPackageName() + "/custom_bg.png"));
		}
	}

	/**
	 * 保存设置
	 */
	private void save() {
		Editor e_quiz = getSharedPreferences(PreferenceName_Quiz,
				Context.MODE_PRIVATE).edit();
		for (int i = 0; i < GroupIsChoosed.length; i++) {
			e_quiz.putBoolean(Database.GroupNames[i], GroupIsChoosed[i]);
		}
		e_quiz.putInt(KeyName_mode, gameMode);
		// e_quiz.putInt(KeyName_difficulty, difficulty);

		e_quiz.commit();
	}

	/**
	 * 本界面所有元素的OnClickListener
	 */
	OnClickListener l = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.akb_toggle:
				GroupIsChoosed[Database.GroupOrder_AKB48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.ske_toggle:
				GroupIsChoosed[Database.GroupOrder_SKE48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.nmb_toggle:
				GroupIsChoosed[Database.GroupOrder_NMB48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.hkt_toggle:
				GroupIsChoosed[Database.GroupOrder_HKT48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			// case R.id.snd_toggle:
			// SDN48IsChoosed = !SDN48IsChoosed;
			// setView(R.id.sdn_toggle, SDN48IsChoosed);
			// isChanged = true;
			// break;

			case R.id.ngzk_toggle:
				GroupIsChoosed[Database.GroupOrder_NGZK46] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.jkt_toggle:
				GroupIsChoosed[Database.GroupOrder_JKT48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.snh_toggle:
				GroupIsChoosed[Database.GroupOrder_SNH48] = ((ToggleButton) v).isChecked();
				isChanged = true;
				break;

			case R.id.game_start:
				if (isChanged) {
					save();
				}
				boolean flag = false;
				for (int i = 0; i < GroupIsChoosed.length; i++) {
					if (GroupIsChoosed[i]) {
						flag = true;
						break;
					}
				}

				if (flag) {
					Intent intent = new Intent(Chooser.this, Quiz.class);

					intent.putExtra(KeyName_groups, GroupIsChoosed);
					// intent.putExtra(KeyName_difficulty, difficulty);
					intent.putExtra(KeyName_mode, gameMode);

					startActivityForResult(intent, gameMode);
				} else {
					Toast.makeText(Chooser.this,
							R.string.chooser_err_unchoosed,
							Toast.LENGTH_LONG).show();
				}

				break;
			}
		}

	};

	@Override
	protected void
			onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Intent result = new Intent(data);
			setResult(RESULT_OK, result);
			finish();
		}
	}

	public boolean onKeydown(int keyCode, KeyEvent e) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isChanged) {
				save();
			}
			finish();
			break;
		}

		return super.onKeyDown(keyCode, e);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

}
