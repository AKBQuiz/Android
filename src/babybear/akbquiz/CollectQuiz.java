package babybear.akbquiz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CollectQuiz extends Activity {
	private QuizContainder quiz, lastquiz;

	private boolean isSending = false;
	Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.collect_quiz);
	}

	@Override
	public void onStart() {
		super.onStart();
		handler = new Handler();
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {

		lastquiz = quiz = new QuizContainder();

		OnClickListener l = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.akb_toggle:
					quiz.akb = ((ToggleButton) v).isChecked();
					break;
				case R.id.ske_toggle:
					quiz.ske = ((ToggleButton) v).isChecked();
					break;
				case R.id.nmb_toggle:
					quiz.nmb = ((ToggleButton) v).isChecked();
					break;
				case R.id.jkt_toggle:
					quiz.jkt = ((ToggleButton) v).isChecked();
					break;
				case R.id.hkt_toggle:
					quiz.hkt = ((ToggleButton) v).isChecked();
					break;
				case R.id.ngzk_toggle:
					quiz.ngzk = ((ToggleButton) v).isChecked();
					break;
				// case R.id.sdn_toggle:
				// quiz.sdn=((ToggleButton)v).isChecked();
				// break;
				case R.id.snh_toggle:
					quiz.snh = ((ToggleButton) v).isChecked();
					break;

				case R.id.cancel:
					CollectQuiz.this.finish();
					break;

				case R.id.submit:
					if (isSending) {
						return;
					}
					quiz.question = ((EditText) findViewById(R.id.question))
							.getText().toString();
					quiz.editor = ((EditText) findViewById(R.id.editor))
							.getText().toString();
					quiz.options[0] = ((EditText) findViewById(R.id.answer))
							.getText().toString();
					quiz.options[1] = ((EditText) findViewById(R.id.wrong1))
							.getText().toString();
					quiz.options[2] = ((EditText) findViewById(R.id.wrong2))
							.getText().toString();
					quiz.options[3] = ((EditText) findViewById(R.id.wrong3))
							.getText().toString();

					if (quiz == lastquiz
 || quiz.isNewQuiz(quiz, lastquiz)) {
						if (quiz.isUseable(CollectQuiz.this)) {
							
							isSending = true;
							Toast.makeText(CollectQuiz.this, R.string.collect_sending, Toast.LENGTH_LONG).show();
							new Thread() {
								@Override
								public void run() {
									submit();
								}
							}.start();
						}
					}
					break;
				}
			}
		};

		ToggleButton akb = (ToggleButton) findViewById(R.id.akb_toggle);
		akb.setOnClickListener(l);

		ToggleButton ske = (ToggleButton) findViewById(R.id.ske_toggle);
		ske.setOnClickListener(l);

		ToggleButton nmb = (ToggleButton) findViewById(R.id.nmb_toggle);
		nmb.setOnClickListener(l);

		ToggleButton hkt = (ToggleButton) findViewById(R.id.hkt_toggle);
		hkt.setOnClickListener(l);

		ToggleButton ngzk = (ToggleButton) findViewById(R.id.ngzk_toggle);
		ngzk.setOnClickListener(l);

		// ToggleButton sdn = (ToggleButton) findViewById(R.id.sdn_toggle);
		// sdn.setOnClickListener(l);
		// setView(sdn,SDN48IsChoosed);
		//
		ToggleButton jkt = (ToggleButton) findViewById(R.id.jkt_toggle);
		jkt.setOnClickListener(l);

		ToggleButton snh = (ToggleButton) findViewById(R.id.snh_toggle);
		snh.setOnClickListener(l);

		((Button) findViewById(R.id.submit)).setOnClickListener(l);
		((Button) findViewById(R.id.cancel)).setOnClickListener(l);

		RatingBar diff = (RatingBar) findViewById(R.id.difficulty);
		diff.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
			@Override
			public void onRatingChanged(RatingBar arg0, float rating,
					boolean fromUser) {
				if (!fromUser) {
					return;
				}
				quiz.difficulty = (int) (rating*2);

			}

		});

	}
	
	/**
	 * 提交
	 */
	private void submit() {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(
					"http://fanhuashe.sinaapp.com/quiz/quizcollect/addquiz.php");
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);

			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			String postContent = "editor=" + URLEncoder.encode(quiz.editor,"utf-8")
					+ "&groups=" + quiz.getGroup() + "&difficulty="
					+ quiz.difficulty + "&question="
					+ URLEncoder.encode(quiz.question,"utf-8") + "&options="
					+ quiz.getOptions();

			Log.d("cq", "postContent: " + postContent);
			dos.write(postContent.getBytes());
			dos.flush();
			dos.close();

			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"utf-8"));

			String line = br.readLine();
			Log.d("submit", line);
			if (line.equalsIgnoreCase("success")) {
				lastquiz = quiz;
				quiz = new QuizContainder();

				handler.post(new Runnable() {

					@Override
					public void run() {
						reset();
						isSending = false;
						Toast.makeText(CollectQuiz.this,
								R.string.collect_success,
								Toast.LENGTH_LONG).show();
					}

				});
			} else {
				handler.post(new Runnable() {

					@Override
					public void run() {
						isSending = false;
						Toast.makeText(CollectQuiz.this,
								R.string.collect_fail,
								Toast.LENGTH_LONG).show();
					}

				});

			}

			while ((line = br.readLine()) != null) {
				Log.d("submit", line);
			}
			br.close();
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			handler.post(new Runnable() {
				@Override
				public void run() {
					isSending = false;
					Toast.makeText(CollectQuiz.this,
							R.string.collect_fail,
							Toast.LENGTH_LONG)
							.show();
				}

			});
		}
	}

	
	/**
	 * 重置
	 */
	void reset() {
		lastquiz = quiz;
		quiz = new QuizContainder();

		((EditText) findViewById(R.id.question)).setText("");
		((EditText) findViewById(R.id.answer)).setText("");
		((EditText) findViewById(R.id.wrong1)).setText("");
		((EditText) findViewById(R.id.wrong2)).setText("");
		((EditText) findViewById(R.id.wrong3)).setText("");

		((ToggleButton) findViewById(R.id.akb_toggle)).setChecked(false);
		((ToggleButton) findViewById(R.id.ske_toggle)).setChecked(false);
		((ToggleButton) findViewById(R.id.nmb_toggle)).setChecked(false);
		((ToggleButton) findViewById(R.id.hkt_toggle)).setChecked(false);
		((ToggleButton) findViewById(R.id.jkt_toggle)).setChecked(false);
		((ToggleButton) findViewById(R.id.snh_toggle)).setChecked(false);
		// ((ToggleButton)findViewById(R.id.sdn_toggle)).setChecked(false);

		((RatingBar) findViewById(R.id.difficulty)).setRating(0);
	}

	
	/**
	 * 内部类 包含一道题目的全部信息
	 * @author BabyBeaR
	 *
	 */
	class QuizContainder {
		public int difficulty = 0;
		public String editor;
		public String question;
		public String[] options = new String[4];
		public boolean akb = false, ske = false, nmb = false, hkt = false,
				sdn = false, snh = false, jkt = false, ngzk = false;

		static final int LIMIT_TOP_EDITOR = 32;
		static final int LIMIT_LOW_EDITOR = 2;
		static final int LIMIT_TOP_QUESTION = 200;
		static final int LIMIT_LOW_QUESTION = 7;
		static final int LIMIT_TOP_ANSWER = 50;
		static final int LIMIT_LOW_ANSWER = 2;

		/**
		 * 获取本题目groups数组的JSON字符串
		 * @return String JSON化的字符串
		 */
		public String getGroup() {
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(akb ? 1 : 0);
			sb.append(",");
			sb.append(ske ? 1 : 0);
			sb.append(",");
			sb.append(nmb ? 1 : 0);
			sb.append(",");
			sb.append(hkt ? 1 : 0);
			sb.append(",");
			sb.append(ngzk ? 1 : 0);
			sb.append(",");
			sb.append(sdn ? 1 : 0);
			sb.append(",");
			sb.append(jkt ? 1 : 0);
			sb.append(",");
			sb.append(snh ? 1 : 0);
			sb.append("]");

			return sb.toString();
		}

		
		/**
		 * 获取本题目选项数组的JSON字符串
		 * @return String JSON化的字符串
		 * @throws UnsupportedEncodingException 
		 */
		public String getOptions() throws UnsupportedEncodingException {
			StringBuffer sb = new StringBuffer();
			sb.append("[\"");
			sb.append(URLEncoder.encode(options[0],"utf-8"));
			sb.append("\",\"");
			sb.append(URLEncoder.encode(options[1],"utf-8"));
			sb.append("\",\"");
			sb.append(URLEncoder.encode(options[2],"utf-8"));
			sb.append("\",\"");
			sb.append(URLEncoder.encode(options[3],"utf-8"));
			sb.append("\"]");
			return sb.toString();
		}

		
		/**
		 * 比较两个问题是否相同
		 * 判断条件是 问题的答案完全相同(不分大小写)
		 * @param q1 要比较的问题1
		 * @param q2 要比较的问题2
		 * @return true两个问题不同是一个新问题  false两个问题相同
		 */
		boolean isNewQuiz(QuizContainder q1, QuizContainder q2) {
			if (q1.question.equalsIgnoreCase(q2.question)) {
				if(q1.options[0].equalsIgnoreCase(q2.options[0])) {
					return false;
				}
			}
			return true;
		}

		
		/**
		 * 问题是否符合要求
		 * @param context Context对象 只是为了发送Toast
		 * @return true 符合 false 不符合
		 */
		boolean isUseable(Context context) {
			if (this.difficulty <= 0 || this.difficulty > 10) {
				Toast.makeText(context,
						R.string.collect_err_diffculty,
						Toast.LENGTH_SHORT).show();
				return false;
			}
			if (this.editor.length() > LIMIT_TOP_EDITOR
					|| this.editor.length() < LIMIT_LOW_EDITOR) {

				Toast.makeText(
						context,
						getString(R.string.collect_err_editor,
								LIMIT_LOW_EDITOR,
								LIMIT_TOP_EDITOR),
						Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			if (this.question.length() > LIMIT_TOP_QUESTION
					|| this.question.length() < LIMIT_LOW_QUESTION) {
				Toast.makeText(
						context,
						getString(R.string.collect_err_question,
								LIMIT_LOW_QUESTION,
								LIMIT_TOP_QUESTION),
						Toast.LENGTH_SHORT)
						.show();
				return false;
			}
			for (int i = 0; i < 4; i++) {
				if (this.options[i].length() > LIMIT_TOP_ANSWER
						|| this.options[i].length() < LIMIT_LOW_ANSWER) {
					Toast.makeText(
							context,
							getString(R.string.collect_err_options,
									LIMIT_LOW_ANSWER,
									LIMIT_TOP_ANSWER),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			return true;
		}
	}
}