package babybear.akbquiz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CalendarEditor extends Activity {

	// 系统日历相关数据库的Uri
	private static Uri calanderUri;
	private static Uri calanderEventUri;
	private static Uri calanderRemiderUri;

	// 系统日历相关数据库的常量

	private static String KEY_CALLER_IS_SYNCADAPTER;

	private static String KEY_CALENDAR_NAME; // TEXT
	private static String KEY_CALENDAR_DISPLAY_NAME; // TEXT
	private static String KEY_CALENDAR_ACCOUNT_NAME; // TEXT
	private static String KEY_CALENDAR_ACCOUNT_TYPE; // TEXT
	private static String KEY_CALENDAR_CALENDAR_COLOR; // INTEGER (color value)
	private static String KEY_CALENDAR_ACCESS_LEVEL; // INTEGER
	private static String KEY_CALENDAR_OWNER_ACCOUNT;// String
	private static String VALUE_CALENDAR_ACCOUNT_TYPE; // TEXT
	private static int VALUE_CALENDAR_ACCESS_LEVEL;

	private static String KEY_EVENT_TITLE; // TEXT
	private static String KEY_EVENT_DESCRIPTION; // TEXT
	private static String KEY_EVENT_COLOR; // INTEGER
	private static String KEY_EVENT_ALL_DAY; // INTEGER
	private static String KEY_EVENT_CALENDAR_ID; // INTEGER
	private static String KEY_EVENT_RRULE; // TEXT
	private static String KEY_EVENT_TIMEZONE;// TEXT
	private static String KEY_EVENT_DTSTART; // INTEGER (long millis)
	private static String KEY_EVENT_DTEND; // INTEGER (long millis)
	private static String KEY_REMINDER_EVENT_ID; // INTEGER (foreign key to the
													// Events table)
	private static String KEY_REMINDER_MINUTES; // INTEGER
	private static String KEY_REMINDER_METHOD; // INTEGER
	private static int VALUE_REMINDER_METHOD; // INTEGER

	// 根据不同的系统版本设置不同的常量
	static {
		if (Build.VERSION.SDK_INT >= 14) {
			calanderUri = android.provider.CalendarContract.Calendars.CONTENT_URI;
			calanderEventUri = android.provider.CalendarContract.Events.CONTENT_URI;
			calanderRemiderUri = android.provider.CalendarContract.Reminders.CONTENT_URI;

			KEY_CALLER_IS_SYNCADAPTER = android.provider.CalendarContract.CALLER_IS_SYNCADAPTER;
			KEY_CALENDAR_NAME = android.provider.CalendarContract.Calendars.NAME;
			KEY_CALENDAR_DISPLAY_NAME = android.provider.CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
			KEY_CALENDAR_ACCOUNT_NAME = android.provider.CalendarContract.Calendars.ACCOUNT_NAME;
			KEY_CALENDAR_ACCOUNT_TYPE = android.provider.CalendarContract.Calendars.ACCOUNT_TYPE;
			KEY_CALENDAR_CALENDAR_COLOR = android.provider.CalendarContract.Calendars.CALENDAR_COLOR;
			KEY_CALENDAR_ACCESS_LEVEL = android.provider.CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL;
			KEY_CALENDAR_OWNER_ACCOUNT = android.provider.CalendarContract.Calendars.OWNER_ACCOUNT;
			VALUE_CALENDAR_ACCOUNT_TYPE = android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL;
			VALUE_CALENDAR_ACCESS_LEVEL = android.provider.CalendarContract.Calendars.CAL_ACCESS_OWNER;

			KEY_EVENT_TITLE = android.provider.CalendarContract.Events.TITLE;
			KEY_EVENT_DESCRIPTION = android.provider.CalendarContract.Events.DESCRIPTION;
			KEY_EVENT_COLOR = android.provider.CalendarContract.Events.EVENT_COLOR;
			KEY_EVENT_ALL_DAY = android.provider.CalendarContract.Events.ALL_DAY;
			KEY_EVENT_CALENDAR_ID = android.provider.CalendarContract.Events.CALENDAR_ID;
			KEY_EVENT_RRULE = android.provider.CalendarContract.Events.RRULE;
			KEY_EVENT_TIMEZONE = android.provider.CalendarContract.Events.EVENT_TIMEZONE;
			KEY_EVENT_DTSTART = android.provider.CalendarContract.Events.DTSTART;
			KEY_EVENT_DTEND = android.provider.CalendarContract.Events.DTEND;

			KEY_REMINDER_EVENT_ID = android.provider.CalendarContract.Reminders.EVENT_ID;
			KEY_REMINDER_MINUTES = android.provider.CalendarContract.Reminders.MINUTES;
			KEY_REMINDER_METHOD = android.provider.CalendarContract.Reminders.METHOD;
			VALUE_REMINDER_METHOD = android.provider.CalendarContract.Reminders.METHOD_DEFAULT;
		} else {
			if (Build.VERSION.SDK_INT >= 8) {
				calanderUri = Uri.parse("content://com.android.calendar/calendars");
				calanderEventUri = Uri.parse("content://com.android.calendar/events");
				calanderRemiderUri = Uri.parse("content://com.android.calendar/reminders");

			} else {
				calanderUri = Uri.parse("content://calendar/calendars");
				calanderEventUri = Uri.parse("content://calendar/events");
				calanderRemiderUri = Uri.parse("content://calendar/reminders");
			}
			KEY_CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";

			KEY_CALENDAR_NAME = "name";
			KEY_CALENDAR_DISPLAY_NAME = "calendar_displayName";
			KEY_CALENDAR_ACCOUNT_NAME = "account_name";
			KEY_CALENDAR_ACCOUNT_TYPE = "account_type";
			KEY_CALENDAR_CALENDAR_COLOR = "calendar_color";
			KEY_CALENDAR_ACCESS_LEVEL = "calendar_access_level";
			KEY_CALENDAR_OWNER_ACCOUNT = "ownerAccount";
			VALUE_CALENDAR_ACCOUNT_TYPE = "LOCAL";
			VALUE_CALENDAR_ACCESS_LEVEL = 700;

			KEY_EVENT_TITLE = "title";
			KEY_EVENT_DESCRIPTION = "description";
			KEY_EVENT_COLOR = "eventColor";
			KEY_EVENT_ALL_DAY = "allDay";
			KEY_EVENT_RRULE = "rrule";
			KEY_EVENT_TIMEZONE = "eventTimezone";
			KEY_EVENT_CALENDAR_ID = "calendar_id";
			KEY_EVENT_DTSTART = "dtstart";

			KEY_REMINDER_EVENT_ID = "event_id";
			KEY_REMINDER_MINUTES = "minutes";
			KEY_REMINDER_METHOD = "method";
			VALUE_REMINDER_METHOD = 0;
		}
	}

	// 一些其他键
	private static String KEY_GROUP_TITLE = "group";
	private static String KEY_CHILD_TITLE = "child";
	private static String KEY_CHILD_NAME = "name";
	private static String KEY_CHILD_BIRTH = "birthday";
	private static String KEY_CHILD_ADDED = "isadded";

	// 设置文件及其Editor
	private SharedPreferences sp_cfg;
	private SharedPreferences.Editor editor_cfg;

	// 所有日历 及 选择的日历的ID
	private int calendarId = -1;
	private ArrayList<ContentValues> calList;

	// 格式化数据库中生日用到
	private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
			Locale.getDefault());

	// ExpandableListView 及其 适配器
	private ExpandableListView memberList;
	private MemberGroupListAdapter memberListAdapter;

	// Group 和 childList列表
	private ArrayList<Map<String, String>> groupList = new ArrayList<Map<String, String>>();
	private ArrayList<ArrayList<Map<String, String>>> childList = new ArrayList<ArrayList<Map<String, String>>>();

	// 保存已添加的成员
	private Map<String, HashMap<String, String>> settings;

	// 强制删除模式
	private boolean forceClear = false;
	private int forceClearCounter = 0;

	private ContentResolver cr;
	private Database db;
	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_editor);

		Log.d("CalendarEditor", calanderUri.toString());

		db = new Database(this, Database.DBName_quiz);
		cr = getContentResolver();
		sp_cfg = getSharedPreferences("config", Context.MODE_PRIVATE);
		editor_cfg = sp_cfg.edit();

		calendarId = sp_cfg.getInt(Database.KEY_calendar_id, -1);

		// 这里是调试用代码

		// Cursor cur = cr.query(calanderEventUri, null, null, null, null);
		// cur.moveToFirst();
		//
		// while (cur.moveToNext()) {
		// Log.d("",
		// "_id = "
		// + cur.getInt(cur.getColumnIndex("_id"))
		// + " title = "
		// + cur.getString(cur.getColumnIndex(KEY_EVENT_TITLE)));
		// }
		// cur.close();

		// cr.delete(calanderEventUri.buildUpon().appendPath(""+37).build(),
		// null, null);
		// cr.delete(calanderEventUri.buildUpon().appendPath(""+42).build(),
		// null, null);

		init();
	}

	// 初始化 settings 和两个List的数据 以及widget
	private void init() {
		// widget 初始化
		OnClickListener l = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() != R.id.clear) {
					if (forceClear) {
						forceClear = false;
					}
				}
				switch (v.getId()) {
				case R.id.back:
					finish();
					break;
				case R.id.clear:
					if (forceClear) {
						if (forceClearCounter > 3) {
							forceClear();
						} else {

							forceClearCounter++;
							Toast.makeText(CalendarEditor.this,
									getString(R.string.calendar_forceclear,
											4 - forceClearCounter),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						clearAll();
					}
					break;
				case R.id.calendar_editor_choose:
					getCalander();
					break;
				}
			};
		};
		findViewById(R.id.back).setOnClickListener(l);
		findViewById(R.id.clear).setOnClickListener(l);
		findViewById(R.id.calendar_editor_choose).setOnClickListener(l);

		((Button) findViewById(R.id.clear)).setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (v.getId() == R.id.clear) {
					forceClear = true;
				}

				return false;
			}
		});

		memberList = (ExpandableListView) findViewById(R.id.member_list);

		// 检查是否支持
		Cursor cur = cr.query(calanderUri, null, null, null, null);
		if (cur == null) {
			findViewById(R.id.not_support).setVisibility(View.VISIBLE);
			return;
		}
		cur.close();
		cur = cr.query(calanderEventUri, null, null, null, null);
		if (cur == null) {
			findViewById(R.id.not_support).setVisibility(View.VISIBLE);
			return;
		}
		cur.close();

		// 成员ArrayList 初始化

		HashMap<String, String> group1 = new HashMap<String, String>();
		group1.put(KEY_GROUP_TITLE, Database.GroupName_AKB48);
		groupList.add(group1);

		HashMap<String, String> group2 = new HashMap<String, String>();
		group2.put(KEY_GROUP_TITLE, Database.GroupName_SKE48);
		groupList.add(group2);
		HashMap<String, String> group3 = new HashMap<String, String>();
		group3.put(KEY_GROUP_TITLE, Database.GroupName_NMB48);
		groupList.add(group3);
		HashMap<String, String> group4 = new HashMap<String, String>();
		group4.put(KEY_GROUP_TITLE, Database.GroupName_HKT48);
		groupList.add(group4);
		HashMap<String, String> group5 = new HashMap<String, String>();
		group5.put(KEY_GROUP_TITLE, Database.GroupName_NGZK46);
		groupList.add(group5);
		HashMap<String, String> group6 = new HashMap<String, String>();
		group6.put(KEY_GROUP_TITLE, Database.GroupName_JKT48);
		groupList.add(group6);
		HashMap<String, String> group7 = new HashMap<String, String>();
		group7.put(KEY_GROUP_TITLE, Database.GroupName_SNH48);
		groupList.add(group7);

		for (int i = 0; i < groupList.size(); i++) {
			childList.add(new ArrayList<Map<String, String>>());

		}

		new Thread() {
			@Override
			public void run() {
				loadSetting();

				ArrayList<ContentValues> memberInfo = db.infoQery();
				Log.d("", "member info got");
				for (int i = 0; i < memberInfo.size(); i++) {
					ContentValues info = memberInfo.get(i);
					String str = info.getAsString("birthday");
					if (str == null) {
						continue;
					}
					String tGroup = info.getAsString(KEY_GROUP_TITLE);
					for (int j = 0; j < groupList.size(); j++) {
						if (groupList.get(j)
								.get(KEY_GROUP_TITLE)
								.equalsIgnoreCase(tGroup)) {
							HashMap<String, String> map = new HashMap<String, String>();

							String name = info.getAsString("name");
							String team = info.getAsString("team");
							map.put(KEY_CHILD_TITLE, (team == null ? ""
									: ("Team " + team + " ")) + name);
							map.put(KEY_CHILD_NAME, name);
							map.put(KEY_CHILD_BIRTH,
									info.getAsString("birthday"));
							map.put(KEY_EVENT_TITLE,
									getString(R.string.calendar_event_title,
											name));

							String color = getString(R.color.groupcolor_akb48);
							switch (j) {
							case 0:
								break;
							case 1:
								color = getString(R.color.groupcolor_ske48);
								break;
							case 2:
								color = getString(R.color.groupcolor_nmb48);
								break;
							case 3:
								color = getString(R.color.groupcolor_hkt48);
								break;
							case 4:
								color = getString(R.color.groupcolor_ngzk46);
								break;
							case 5:
								color = getString(R.color.groupcolor_jkt48);
								break;
							case 6:
								color = getString(R.color.groupcolor_snh48);
								break;
							}

							map.put(KEY_EVENT_COLOR, color);
							if (settings != null && settings.containsKey(name)) {
								map.put(KEY_CHILD_ADDED, "true");
							} else {
								map.put(KEY_CHILD_ADDED, "false");
							}

							String comefrom = info.getAsString("comefrom");
							map.put(KEY_EVENT_DESCRIPTION,
									(tGroup == null ? "" : tGroup)
											+ (team == null ? ""
													: getString(R.string.calendar_event_Team,
															"",
															team))
											+ (comefrom == null ? ""
													: getString(R.string.calendar_event_comefrom,
															comefrom))
											+ getString(R.string.calendar_event_title,
													name));
							childList.get(j).add(map);
							break;
						}
					}
				}
				memberListAdapter = new MemberGroupListAdapter(CalendarEditor.this,
						groupList,
						KEY_GROUP_TITLE,
						R.layout.member_list_group,
						R.id.textGroup,
						childList,
						KEY_CHILD_TITLE,
						R.layout.member_list_child,
						R.id.textChild,
						KEY_CHILD_ADDED,
						R.id.isAdded);
				handler.post(new Runnable() {
					@Override
					public void run() {
						memberList.setAdapter(memberListAdapter);
						memberList.setOnChildClickListener(listener);
						findViewById(R.id.calendar_editor_loading).setVisibility(View.GONE);
					}

				});
			}
		}.start();

	}

	@Override
	public void onStart() {
		super.onStart();

		// 设置自定义背景
		if (sp_cfg.getBoolean(Database.KEY_use_custom_background, false)) {
			findViewById(R.id.calender_editor_body).setBackgroundDrawable(Drawable.createFromPath(Environment.getExternalStorageDirectory()
					.getPath()
					+ "/Android/data/" + getPackageName() + "/custom_bg.png"));
		}

	}

	/**
	 * 加载设置 (有哪些成员的生日信息已被添加)
	 */
	private void loadSetting() {
		Log.d("", sp_cfg.getString(Database.KEY_events_added, "{}"));
		try {
			JSONObject events = new JSONObject(sp_cfg.getString(Database.KEY_events_added,
					"{}"));
			if (events.length() == 0) {
				return;
			}
			settings = new HashMap<String, HashMap<String, String>>();

			Log.d("", "get : '" + events.toString() + "' from config");
			for (Iterator<?> it = events.keys(); it.hasNext();) {
				String key = (String) it.next();
				JSONObject item = events.getJSONObject(key);
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("id", Integer.toString(item.getInt("_id")));
				map.put("uri", item.getString("uri"));
				settings.put(key, map);
				Log.d("", "add " + key + " : " + map + "in to settings ");
			}

		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存设置 (有哪些成员的生日信息已被添加)
	 */
	private void saveSetting() {

		JSONObject obj = new JSONObject();
		try {
			if (settings != null && settings.size() > 0) {
				Set<String> keySet = settings.keySet();
				for (String key : keySet) {
					JSONObject item = new JSONObject(settings.get(key));
					obj.put(key, item);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("", obj.toString());
		editor_cfg.putString(Database.KEY_events_added, obj.toString());
		editor_cfg.commit();

	}

	/**
	 * 向日历添加一个生日
	 * 
	 * @param groupPosition 从OnChildClickListener中获取的groupPosition
	 * @param childPosition 从OnChildClickListener中获取的childPosition
	 */
	private void addBirthday(int groupPosition, int childPosition) {
		if (calendarId == -1) {
			Toast.makeText(this,
					R.string.calendar_err_choose,
					Toast.LENGTH_LONG).show();
			getCalander();
			return;
		}
		String str = childList.get(groupPosition)
				.get(childPosition)
				.get(KEY_CHILD_BIRTH);
		if (str == null || str == "") {
			Toast.makeText(this,
					R.string.calendar_err_noinfo,
					Toast.LENGTH_LONG).show();
			return;
		}

		Map<String, String> member = childList.get(groupPosition)
				.get(childPosition);
		HashMap<String, String> map = insertEvent(calendarId, member, true);

		if (map != null) {
			Map<String, String> child = memberListAdapter.getChild(groupPosition,
					childPosition);
			child.put(KEY_CHILD_ADDED, Boolean.toString(true));
			memberListAdapter.notifyDataSetChanged();

			if (settings == null) {
				settings = new HashMap<String, HashMap<String, String>>();
			}
			settings.put(member.get(KEY_CHILD_NAME), map);
			saveSetting();
		}

	}

	//
	/**
	 * 移除名字为一个成员的Event
	 * 
	 * @param name 成员名
	 * @param groupPosition 从OnChildClickListener中获取的groupPosition
	 * @param childPosition 从OnChildClickListener中获取的childPosition
	 */
	protected void removeBirthday(String name,
			int groupPosition,
			int childPosition) {
		Uri uri = Uri.parse(settings.get(name).get("uri"));
		int counter = cr.delete(uri, null, null);
		if (counter > 0) {
			Map<String, String> child = memberListAdapter.getChild(groupPosition,
					childPosition);
			child.put(KEY_CHILD_ADDED, Boolean.toString(false));
			memberListAdapter.notifyDataSetChanged();

			Toast.makeText(this, R.string.calendar_deleted, Toast.LENGTH_SHORT)
					.show();
			settings.remove(name);
			saveSetting();
		}
	}

	/**
	 * 向用户请求一个可用日历的ID 直接设置到calendarId
	 */
	private void getCalander() {
		Cursor cur = cr.query(calanderUri, new String[] { "_id",
				KEY_CALENDAR_NAME,
				KEY_CALENDAR_DISPLAY_NAME }, null, null, null);
		calList = new ArrayList<ContentValues>();
		ArrayList<String> items = new ArrayList<String>();
		if (cur.getCount() > 0) {
			cur.moveToFirst();
			while (cur.moveToNext()) {
				ContentValues t = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, t);
				items.add(cur.getString(cur.getColumnIndex(KEY_CALENDAR_DISPLAY_NAME)));
				calList.add(t);
			}
		}
		cur.close();

		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
			int tid = -1;

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				if (arg1 == DialogInterface.BUTTON_POSITIVE) {
					calendarId = tid;
					editor_cfg.putInt(Database.KEY_calendar_id, calendarId);
					editor_cfg.commit();
					return;
				}
				if (arg1 == DialogInterface.BUTTON_NEGATIVE) {
					return;
				}

				tid = calList.get(arg1).getAsInteger("_id");
			}

		};

		AlertDialog.Builder calchooser_builder = new AlertDialog.Builder(this);
		calchooser_builder.setTitle(R.string.calendar_choose_witch)
				.setPositiveButton(android.R.string.ok, l)
				.setNegativeButton(android.R.string.cancel, l)
				.setSingleChoiceItems(items.toArray(new String[items.size()]),
						-1,
						l);
		calchooser_builder.create().show();

	}

	/**
	 * 添加一个Event
	 * 
	 * @param calId 日历ID
	 * @param member 成员及Event信息
	 * @param makeReminder 是否添加提醒
	 * @return 添加的Event的ID及Uri
	 */
	private HashMap<String, String> insertEvent(int calId,
			Map<String, String> member,
			boolean makeReminder) {
		int id = -1;
		ContentValues event = new ContentValues();

		try {
			Date birthday = fmt.parse(member.get(KEY_CHILD_BIRTH));
			// birthday.setHours(birthday.getHours() + 12);
			Calendar c = Calendar.getInstance();
			c.setTime(birthday);
			Log.d("", member.get(KEY_CHILD_BIRTH)
					+ " to Calendar and toString " + c.toString());
			event.put(KEY_EVENT_CALENDAR_ID, calId);
			event.put(KEY_EVENT_TITLE, member.get(KEY_EVENT_TITLE));
			event.put(KEY_EVENT_DESCRIPTION, member.get(KEY_EVENT_DESCRIPTION));
			event.put(KEY_EVENT_COLOR,
					Color.parseColor(member.get(KEY_EVENT_COLOR)));
			event.put(KEY_EVENT_DTSTART, c.getTimeInMillis()
					+ c.getTimeZone().getRawOffset());
			event.put(KEY_EVENT_ALL_DAY, 1);
			event.put(KEY_EVENT_RRULE, "FREQ=YEARLY");
			event.put(KEY_EVENT_TIMEZONE, c.getTimeZone().getID());
			birthday.setYear(birthday.getYear() + 100);
			event.put(KEY_EVENT_DTEND, birthday.getTime());

		}
		catch (ParseException e) {
			Toast.makeText(this,
					R.string.calendar_err_decodefail,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		}

		Uri eventUri = cr.insert(calanderEventUri, event);
		id = Integer.parseInt(eventUri.getLastPathSegment());
		if (id == -1) {
			Toast.makeText(this,
					R.string.calendar_err_addfail,
					Toast.LENGTH_SHORT).show();
			return null;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("uri", eventUri.toString());
		map.put("_id", Integer.toString(id));

		Toast.makeText(this, R.string.calendar_add_success, Toast.LENGTH_SHORT)
				.show();
		// if (makeReminder) {
		// insertReminder(id);
		// }
		return map;
	}

	/**
	 * 添加一个提醒
	 * 全天事件没有提醒 弃用
	 * 
	 * @param eventId 事件ID
	 * @return 提醒ID
	 */
	@Deprecated
	private int insertReminder(int eventId) {
		if (eventId == -1) {
			return -1;
		}
		ContentValues reminder = new ContentValues();
		reminder.put(KEY_REMINDER_EVENT_ID, eventId);
		reminder.put(KEY_REMINDER_MINUTES, 10);
		reminder.put(KEY_REMINDER_METHOD, VALUE_REMINDER_METHOD);
		int id = -1;
		Uri eventUri = cr.insert(calanderRemiderUri, reminder);
		id = Integer.parseInt(eventUri.getLastPathSegment());
		return id;
	}

	/**
	 * 清除所有本应用添加的事件
	 */
	private void clearAll() {
		if(settings==null)return;
		Set<String> keys = settings.keySet();
		int count = 0;
		for (String key : keys) {
			String uri = settings.get(key).get("uri");
			count += cr.delete(Uri.parse(uri), null, null);
		}
		settings.clear();
		saveSetting();
		Toast.makeText(this,
				getString(R.string.calendar_clear_echo, count),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * 强行删除可能是本应用添加的所有事件
	 * 适用于设置文件丢失
	 */
	private void forceClear() {
		int counter = cr.delete(calanderEventUri, "`" + KEY_EVENT_DESCRIPTION
				+ "` like \"%%"
				+ getString(R.string.calendar_event_title, "%%") + "\"", null);
		Toast.makeText(this,
				getString(R.string.calendar_clear_echo, counter),
				Toast.LENGTH_SHORT).show();
		}

	/**
	 * ExpandableListView用到的 OnChildClickListener
	 */
	private OnChildClickListener listener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent,
				View view,
				int groupPosition,
				int childPosition,
				long id) {
			Boolean isChecked = Boolean.parseBoolean(memberListAdapter.getChild(groupPosition,
					childPosition)
					.get(KEY_CHILD_ADDED));
			if (isChecked) {
				String name = memberListAdapter.getChild(groupPosition,
						childPosition).get(KEY_CHILD_NAME);
				removeBirthday(name, groupPosition, childPosition);
			} else {
				addBirthday(groupPosition, childPosition);
			}
			Log.d("", "group : " + groupPosition + " child : " + childPosition
					+ " been clicked");
			return false;
		}
	};

}
