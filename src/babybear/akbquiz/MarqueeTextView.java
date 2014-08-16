package babybear.akbquiz;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * 有飞行效果的Textview
 * 可以轮流显示多个文本
 * 
 * @author BabyBeaR
 */
public class MarqueeTextView extends TextView {
	private ArrayList<String> TextList;

	// 文本列表中正在显示的文本指针
	private int noticeIndex = 0;

	// 当前文本宽度
	private int textWidth = 0;

	// 线程终止标识
	private boolean isPlaying = false;

	// 是否已获取过文本的长度
	private boolean isMeasured = false;

	// 文本飞行速度的控制
	private int frq = 30; // 每次移动间隔时间
	private int dx = 5; // 每次移动的像素数

	//private Handler handler = new Handler();

	public MarqueeTextView(Context context) {
		super(context);
		setVisibility(View.INVISIBLE);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setVisibility(View.INVISIBLE);
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setVisibility(View.INVISIBLE);
	}

	/**
	 * 飞行动画的控制线程
	 */
	private Thread animThread = new Thread() {
		private int currentScrollX; // 目前滚动坐标 px

		public void run() {
			currentScrollX = -getWidth();
			while (isPlaying) {
				try {
					Thread.sleep(frq);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentScrollX += dx; // 每次移动这么多
				if (currentScrollX >= textWidth) { // 如果文本滚动完毕(滚动到view外边)
					// 更换显示为下一行
					noticeIndex++;
					if (noticeIndex >= TextList.size()) {
						noticeIndex = 0;
					}
					MarqueeTextView.this.post(new Runnable() { // 使用post方法才能设置文本
						@Override
						public void run() {
							// setText()后直接使用scrollTo() 会因为还没绘制上去
							// 导致scrollTo()无效 故必须post scrollTo()方法
							// 这会造成有一瞬间scrollX是0
							// 为了不让界面上显示出已更换的文本 先隐藏当前控件
							setVisibility(View.INVISIBLE);
							setText(TextList.get(noticeIndex)); // 更换显示文本
							MarqueeTextView.this.post(new Runnable() { // 使用post方法设置滚动
								@Override
								public void run() {
									scrollTo(-getWidth(), 0); // 设置文本位置为右侧屏幕外
									setVisibility(View.VISIBLE); // 然后显示
								}
							});
						}
					});
					// 更换显示为下一行 END

					currentScrollX = -getWidth(); // 重置滚动坐标
				}else{
					MarqueeTextView.this.post(new Runnable() {
						
						@Override
						public void run() {
							scrollTo(currentScrollX, 0); // 滚动到下一个位置
						}
					});
				}
				
			}
		};
	};

	/**
	 * 设置文本列表
	 * 
	 * @param textList 文本列表
	 */
	public void setTextList(List<String> textList) {
		this.TextList = (ArrayList<String>) textList;
		this.noticeIndex = 0;
		// 将文本显示为文本列表第一项
		setText(TextList.get(noticeIndex));
		MarqueeTextView.this.post(new Runnable() { // 设置文本位置为右侧屏幕外
			@Override
			public void run() {
				scrollTo(-(getWidth()), 0);
				setVisibility(View.VISIBLE);
			}
		});

	}

	/**
	 * 向已有的文本列表中添加一条文本
	 * 添加的文本将在列表的尾部
	 * 如果当前文本列表为空 则创建文本列表且初始化
	 * 
	 * @param text 要添加的文本
	 */
	public void addText(String text) {
		if (this.TextList == null) {
			ArrayList<String> textList = new ArrayList<String>();
			textList.add(text);
			this.setTextList(textList);
		}else{
			this.TextList.add(text);
		}
	}

	/**
	 * 设置文本列表并自动设置飞行速度 然后开始动画
	 * 
	 * @param textList 文本列表
	 */
	public void setTextListAndStart(List<String> textList) {
		setTextList(textList);
		setDxAutomaticlly(5000);
		start();
	}

	/**
	 * 获取当前显示的文本的宽度
	 */
	public int getTextWidth() {
		Paint paint = this.getPaint();
		String str = this.getText().toString();
		return ((int) paint.measureText(str));
	}

	/**
	 * 重载setText方法
	 */
	@Override
	public void setText(CharSequence text, BufferType type) {

		this.isMeasured = false;
		super.setText(text, type);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isMeasured) {// 文字宽度只需在改变文本内容后获取一次
			textWidth = getTextWidth();
			isMeasured = true;
		}
	}

	/**
	 * 开始飞行动画
	 */
	public void start() {
		isPlaying = true;
		animThread.start();
	}

	/**
	 * 停止飞行动画
	 */
	public void stop() {
		isPlaying = false;
	}

	/**
	 * 设置更新频率 建议在80~20之间
	 * 
	 * @param frq 更新频率(ms)
	 */
	public void setSpeed(int frq) {
		this.frq = frq;
	}

	/**
	 * 设置每次更新移动的像素数
	 * 为了在不同密度设备下能够有相同的飞行速度 建议使用setOffsetAutomaticlly()方法
	 * 
	 * @param dx 移动的像素数(px)
	 */
	public void setDx(int dx) {
		this.dx = dx;
	}

	/**
	 * 根据文本框宽度和当前的更新频率计算并设置每次更新移动的像素数目
	 * 
	 * @param time 一个空字符飞完全程所需时间(ms)
	 */
	public void setDxAutomaticlly(int time) {
		int dx = getWidth() / (time / frq);
		setDx(dx);
	}

	/**
	 * 暂停当前的飞行动画
	 * 还不知道使用这个方法会出现什么结果...
	 */
	@Deprecated
	public void pauseAnim() {
		if (animThread.isAlive()) {
			try {
				animThread.wait();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 继续当前的飞行动画
	 * 还不知道使用这个方法会出现什么结果...
	 */
	@Deprecated
	public void resumeAnim() {
		if (animThread.isAlive()) {
			animThread.notifyAll();
		}
	}

}
