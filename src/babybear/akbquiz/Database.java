package babybear.akbquiz;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class Database {
	public static final String databasePath = "/data/data/babybear.akbquiz/databases/AKBQuiz.db";
	public static final String DBName_quiz = "AKBQuiz.db";
	public static final String DBName_cfg = "config.db";
	public static final String TabName_user = "user";
	public static final String TabName_quiz = "quiz";
	public static final String TabName_member = "member_info";

	// 表`quiz`中各列的序号
	public static final int ColIndex_ID = 0;
	public static final int ColIndex_EDITOR = 1;
	public static final int ColIndex_QUESTION = 2;
	public static final int ColIndex_ANSWER = 3;
	public static final int ColIndex_WRONG1 = 4;
	public static final int ColIndex_WRONG2 = 5;
	public static final int ColIndex_WRONG3 = 6;
	public static final int ColIndex_DIFFICULTY = 7;

	// 队伍代码
	public static final int GroupOrder_LIMIT = 10;
	public static final int GroupOrder_NULL = -1;
	public static final int GroupOrder_AKB48 = 0;
	public static final int GroupOrder_SKE48 = 1;
	public static final int GroupOrder_NMB48 = 2;
	public static final int GroupOrder_HKT48 = 3;
	public static final int GroupOrder_NGZK46 = 6;
	public static final int GroupOrder_SDN48 = 7;
	public static final int GroupOrder_JKT48 = 8;
	public static final int GroupOrder_SNH48 = 9;

	// 队伍名
	public static final String GroupName_AKB48 = "akb48";
	public static final String GroupName_SKE48 = "ske48";
	public static final String GroupName_NMB48 = "nmb48";
	public static final String GroupName_HKT48 = "hkt48";
	public static final String GroupName_NGZK46 = "ngzk46";
	public static final String GroupName_SDN48 = "sdn48";
	public static final String GroupName_JKT48 = "jkt48";
	public static final String GroupName_SNH48 = "snh48";

	// 队伍名数组 需要根据GroupOrder取
	public static final String[] GroupNames = { GroupName_AKB48,
			GroupName_SKE48,
			GroupName_NMB48,
			GroupName_HKT48,
			"null_1",
			"null_2",
			GroupName_NGZK46,
			GroupName_SDN48,
			GroupName_JKT48,
			GroupName_SNH48 };

	public static final String ColName_id = "_id";
	public static final String ColName_username = "username";
	public static final String ColName_user_identity = "user_identity";
	public static final String ColName_extend = "extend";
	public static final String ColName_playlist = "playlist";
	public static final String ColName_createTime = "createTime";
	public static final String ColName_counter_correct = "counter_correct";
	public static final String ColName_counter_wrong = "counter_wrong";
	public static final String ColName_time_played = "time_played";
	public static final String ColName_EDITOR = "editor";
	public static final String ColName_QUESTION = "question";
	public static final String ColName_ANSWER = "answer";
	public static final String ColName_WRONG1 = "wrong_1";
	public static final String ColName_WRONG2 = "wrong_2";
	public static final String ColName_WRONG3 = "wrong_3";
	public static final String ColName_DIFFICULTY = "difficulty";

	public static final String KEY_switch_bg = "switch_bg";
	public static final String KEY_vol_bg = "vol_bg";
	public static final String KEY_switch_sound = "switch_sound";
	public static final String KEY_vol_sound = "vol_sound";
	public static final String KEY_switch_vibration = "switch_vibration";
	public static final String KEY_use_custom_background = "custom_background";
	public static final String KEY_bgm_loopmode = "bg_loopmode";
	public static final String KEY_calendar_id = "calendar_choosed";
	public static final String KEY_events_added = "events_added";
	public static final String KEY_tips_info = "tips_info";
	public static final String KEY_tips_quiz = "tips_quiz";
	public static final String KEY_normal_exit = "normal_exit";

	//
	public static final String IDTag_weibo = "weibo_";

	// 数据库版本
	public static final int userdb_ver = 2;
	public static final int quizdb_ver = 2;

	// 问题类型
	public final static int QuizType_Normal = 0;
	public final static int QuizType_Birthday = 1;
	public final static int QuizType_Team = 2;
	public final static int QuizType_Comefrom = 3;

	// 决定各类题目出现的几率
	public final static int QUIZ_RAND_MAX = 100;
	public final static int QUIZ_DIVIDE_1 = 15;
	public final static int QUIZ_DIVIDE_2 = 30;
	public final static int QUIZ_DIVIDE_3 = 45;

	private String DBname = null;
	private Context context = null;
	private DatabaseHelper dbh = null;
	private SQLiteDatabase db = null;

	private String[] useableComefrom = null;
	private String[] useableBirthday = null;
	private String[] useableTeam = null;

	private SimpleDateFormat fmt;
	private SimpleDateFormat outputDateFormat;

	/**
	 * @param ctx
	 *            Context
	 * @param name
	 *            数据库名
	 */
	public Database(Context ctx, String name) {
		context = ctx;
		DBname = name;
		fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		outputDateFormat = new SimpleDateFormat(ctx.getString(R.string.database_datefmt),
				Locale.getDefault());

		String[] dblist = context.databaseList();
		for (String dbname : dblist) {
			Log.d("", dbname);
		}
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context,
				String name,
				CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (DBname.equals(Database.DBName_cfg)) {
				// CursorFactory factory = new CursorFactory();
				// db.create(factory);
				Date t = new Date();
				Toast.makeText(context,
						R.string.database_building,
						Toast.LENGTH_SHORT).show();
				db.execSQL("CREATE TABLE user ("
						+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ ColName_username + " TEXT , " + ColName_user_identity
						+ " TEXT , " + ColName_extend + " INTEGER, "
						+ ColName_createTime + " DATETIME , "
						+ ColName_counter_correct + " INTEGER DEFAULT 0, "
						+ ColName_counter_wrong + " INTEGER DEFAULT 0, "
						+ ColName_time_played + " INTEGER DEFAULT 0)");

				ContentValues values = new ContentValues();
				values.put(ColName_username, "default");
				values.put(ColName_createTime, t.getTime());
				values.put(ColName_extend, 0);
				db.insert(TabName_user, null, values);
			}

		}

		@Override
		public void
				onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (DBname.equals(DBName_cfg)) {
				switch (oldVersion) {
				case 1:
					String sql = "ALTER TABLE `user` ADD `"
							+ ColName_user_identity + "` TEXT ;";
					db.execSQL(sql);
					break;
				}
			}

		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}
	}

	/**
	 * 向数据库中添加一个用户
	 * 
	 * @param username
	 *            用户名
	 */
	public long addUser(String username) {
		return addUser(username, "");
	}

	/**
	 * @param username
	 * @param identity
	 * @return
	 */
	public long addUser(String username, String identity) {
		if (!DBname.equals(DBName_cfg)) {
			return -1;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Date t = new Date();
		// db.execSQL("INSERT INTO user (username , switch_bg , vol_bg , switch_sound , vol_sound , switch_vibration , extend , createTime ) VALUES ('"+username+"' , 1 , 10 , 1 , 10 , 1 , '' ,"+t.getTime()+")");
		ContentValues values = new ContentValues();
		values.put(ColName_username, username);
		values.put(ColName_user_identity, identity);
		values.put(ColName_createTime, t.getTime());

		long n = db.insert(TabName_user, null, values);
		Toast.makeText(context,
				context.getString(R.string.database_user_created, username),
				Toast.LENGTH_SHORT).show();

		db.close();
		dbh.close();
		return n;
	}

	/**
	 * 从数据库中删除一个用户
	 * 
	 * @param _id
	 *            用户_id
	 */
	public void removeUser(int _id) {
		if (!DBname.equals(DBName_cfg)) {
			return;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		db.delete(TabName_user, "_id = " + _id, null);

		Toast.makeText(context,
				context.getString(R.string.database_user_removed, "id = " + _id),
				Toast.LENGTH_SHORT)
				.show();
		db.close();
		dbh.close();
	};

	/**
	 * 从数据库中删除一个用户
	 * 
	 * @param username
	 *            用户名
	 */
	public void removeUser(String username) {
		if (!DBname.equals(DBName_cfg)) {
			return;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		db.delete(TabName_user, ColName_username + " = " + username, null);

		Toast.makeText(context,
				context.getString(R.string.database_user_removed, username),
				Toast.LENGTH_SHORT).show();
		db.close();
		dbh.close();
	}

	/**
	 * 更新用户信息
	 * 
	 * @param id
	 *            用户_id
	 * @param values
	 *            要更改的键值对
	 */
	public void updateInfo(int id, ContentValues values) {
		if (!DBname.equals(DBName_cfg)) {
			return;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();
		// values.getAsBoolean(key)
		db.update(TabName_user, values, "_id=" + id, null);

		db.close();
		dbh.close();
	}

	/**
	 * 设置当前用户
	 * 
	 * @param userId
	 *            用户_id
	 */
	public boolean setCurrentUser(int userId) {
		if (!DBname.equals(DBName_cfg)) {
			return false;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put("extend", userId);
		int n = db.update(TabName_user, values, "_id=1", null);

		db.close();
		dbh.close();
		if (n < 1) {
			return false;
		}
		return true;
	}

	/**
	 * 自动获取当前用户的信息
	 * 
	 * @return 用户数据的键值对
	 */
	public int getCurrentUser() {
		if (!DBname.equals(DBName_cfg)) {
			return 0;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Cursor defaultUser = db.query(TabName_user,
				new String[] { "extend" },
				"_id=1",
				null,
				null,
				null,
				null);
		defaultUser.moveToFirst();
		int userid = defaultUser.getInt(0);

		defaultUser.close();
		db.close();
		dbh.close();
		return userid;
	}

	/**
	 * 获取所有用户列表
	 * 
	 * @return 用户列表 包括int _id ，Sting username 和一个 Boolean isChoosed
	 */
	public ArrayList<ContentValues> userListQuery() {
		if (!DBname.equals(DBName_cfg)) {
			return null;
		}
		dbh = new DatabaseHelper(context, DBname, null, userdb_ver);
		db = dbh.getWritableDatabase();

		Cursor cur = db.query(TabName_user,
				null,
				"_id>1",
				null,
				null,
				null,
				null);

		ArrayList<ContentValues> userlist = new ArrayList<ContentValues>();
		if (cur.moveToFirst()) {
			do {
				ContentValues user = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, user);
				user.put("isChoosed", false);
				userlist.add(user);
			} while (cur.moveToNext());
		} else {
			userlist = null;
		}

		cur.close();
		db.close();
		dbh.close();
		return userlist;
	}

	/**
	 * 完全随机的获取n个题目 调试用 已弃用
	 * 
	 * @param n
	 *            题目数量
	 * @return 题目内容
	 */
	/*
	 * @Deprecated
	 * public ArrayList<ContentValues> QuizQuery(int n) {
	 * if (!DBname.equals(DBName_quiz)) {
	 * return null;
	 * }
	 * dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
	 * db = dbh.getWritableDatabase();
	 * Cursor cur = db.query(TabName_quiz,
	 * null,
	 * null,
	 * null,
	 * null,
	 * null,
	 * "random()",
	 * "0," + n);
	 * ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();
	 * if (cur.moveToFirst()) {
	 * do {
	 * ContentValues quiz = new ContentValues();
	 * DatabaseUtils.cursorRowToContentValues(cur, quiz);
	 * quizlist.add(quiz);
	 * } while (cur.moveToNext());
	 * } else {
	 * quizlist = null;
	 * }
	 * cur.close();
	 * db.close();
	 * dbh.close();
	 * return quizlist;
	 * }
	 * //
	 */
	/**
	 * 通过难度和团体获取 20个题目 因难度不可用 还未启用
	 * 
	 * @param difficulty 难度
	 * @param groups 相关的团
	 * @return 题目内容
	 */
	/*
	 * public ArrayList<ContentValues> QuizQuery(int difficulty, String[]
	 * groups) {
	 * if (!DBname.equals(DBName_quiz)) {
	 * return null;
	 * }
	 * dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
	 * db = dbh.getWritableDatabase();
	 * String selection = "(";
	 * // String [] selectionArgs = new String [groups.length];
	 * for (int i = 0; i < groups.length; i++) {
	 * if (i > 0) {
	 * selection += " OR ";
	 * }
	 * selection += groups[i] + "=1";
	 * }
	 * selection += ") AND difficulty = " + difficulty;
	 * Log.d("Database", "selection is : " + selection);
	 * Cursor cur = db.query(TabName_quiz,
	 * null,
	 * selection,
	 * groups,
	 * null,
	 * null,
	 * "random()",
	 * "0,20");;
	 * ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();
	 * if (cur.moveToFirst()) {
	 * do {
	 * ContentValues quiz = new ContentValues();
	 * DatabaseUtils.cursorRowToContentValues(cur, quiz);
	 * quizlist.add(quiz);
	 * } while (cur.moveToNext());
	 * } else {
	 * quizlist = null;
	 * }
	 * Log.d("Database", "get " + quizlist.size() + " rows");
	 * cur.close();
	 * db.close();
	 * dbh.close();
	 * return quizlist;
	 * }
	 * //
	 */
	/**
	 * 根据团体获取20个题目
	 * 
	 * @param groups 相关的团
	 * @return 题目内容
	 * @throws ParseException
	 */
	public ArrayList<ContentValues> QuizQuery(String[] groups) {
		if (!DBname.equals(DBName_quiz)) {
			return null;
		}

		if (groups.length == 0) {
			return null;
		}

		// dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = context.openOrCreateDatabase(DBName_quiz,
				Context.MODE_PRIVATE,
				null);

		// 获取每种题目需要多少个
		int[] quizCounts = this.getCountEachType(20);
		loadOptions();
		// 一般问题
		String selection = "";
		// String [] selectionArgs = new String [groups.length];
		for (int i = 0; i < groups.length; i++) {
			if (i > 0) {
				selection += " OR ";
			}
			selection += groups[i] + "=1";
		}
		Log.d("Database", "selection is : " + selection);

		Cursor cur = db.query(TabName_quiz,
				null,
				selection,
				null,
				null,
				null,
				"random()",
				"0," + quizCounts[QuizType_Normal]);

		ArrayList<ContentValues> quizlist = new ArrayList<ContentValues>();

		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, quiz);
					quizlist.add(quiz);
				} while (cur.moveToNext());
			}
		}

		// 其他问题
		
		selection = "`is_show` AND ( ";
		for (int i = 0; i < groups.length; i++) {
			if (i > 0) {
				selection += " OR ";
			}
			selection += "`group` like \"" + groups[i] + "\"";
		}
		selection += " )";
		cur = db.query(TabName_member,
				null,
				selection,
				null,
				null,
				null,
				"random()",
				"0,"
						+ (quizCounts[QuizType_Birthday]
								+ quizCounts[QuizType_Comefrom] + quizCounts[QuizType_Team]));

		// 记录其他问题有几个
		int counter = 0;

		Random r = new Random();
		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					if (counter <= quizCounts[QuizType_Birthday]) {
						String team = "";
						String tempString = cur.getString(cur.getColumnIndex("group"));
						if (tempString != null) {
							team += tempString;
						}
						tempString = cur.getString(cur.getColumnIndex("team"));
						if (tempString != null) {
							team += " Team " + tempString;
						}

						quiz.put(Database.ColName_QUESTION,
								context.getString(R.string.database_t_q_birthday,
										cur.getString(cur.getColumnIndex("name")),
										team));
						try {
							Date birth = fmt.parse(cur.getString(cur.getColumnIndex("birthday")));
							String birthday = outputDateFormat.format(birth);;
							quiz.put(Database.ColName_ANSWER, birthday);
							int i = 1;
							List<String> opt = new ArrayList<String>(Arrays.asList(useableBirthday));
							while (i < 4) {
								int t = r.nextInt(opt.size());
								if (opt.get(t).equals(birthday)) {
									opt.remove(t);
								} else {
									quiz.put("wrong_" + i, opt.get(t));
									opt.remove(t);
									i++;
								}
							}
						}
						catch (ParseException e) {
							e.printStackTrace();
						}

					} else if (counter <= quizCounts[QuizType_Comefrom]
							+ quizCounts[QuizType_Birthday]) {

						String team = "";
						String tempString = cur.getString(cur.getColumnIndex("group"));
						if (tempString != null) {
							team += tempString;
						}
						tempString = cur.getString(cur.getColumnIndex("team"));
						if (tempString != null) {
							team += " Team " + tempString;
						}

						quiz.put(Database.ColName_QUESTION,
								context.getString(R.string.database_t_q_comefrom,
										cur.getString(cur.getColumnIndex("name")),
										team));
						String comefrom = cur.getString(cur.getColumnIndex("comefrom"));
						quiz.put(Database.ColName_ANSWER, comefrom);
						int i = 1;
						ArrayList<String> opt = new ArrayList<String>(Arrays.asList(useableComefrom));
						while (i < 4) {
							int t = r.nextInt(opt.size());
							if (opt.get(t).equals(comefrom)) {
								opt.remove(t);
							} else {
								quiz.put("wrong_" + i, opt.get(t));
								opt.remove(t);
								i++;
							}
						}

					} else if (counter <= quizCounts[QuizType_Comefrom]
							+ quizCounts[QuizType_Birthday]
							+ quizCounts[QuizType_Team]) {
						quiz.put(Database.ColName_QUESTION,
								context.getString(R.string.database_t_q_team,
										cur.getString(cur.getColumnIndex("name"))));
						String team = cur.getString(cur.getColumnIndex("group"))
								+ " Team "
								+ cur.getString(cur.getColumnIndex("team"));
						quiz.put(Database.ColName_ANSWER, team);
						int i = 1;
						ArrayList<String> opt = new ArrayList<String>(Arrays.asList(useableTeam));
						while (i < 4) {
							int t = r.nextInt(opt.size());
							if (opt.get(t).equals(team)) {
								opt.remove(t);
							} else {
								quiz.put("wrong_" + i, opt.get(t));
								opt.remove(t);
								i++;
							}
						}
					}

					quizlist.add(quiz);
					counter++;
				} while (cur.moveToNext());
			}
		}

		cur.close();
		db.close();
		// dbh.close();
		return quizlist;
	};

	/**
	 * 查询成员信息
	 * 
	 * @return
	 */
	public ArrayList<ContentValues> infoQery() {
		if (!DBname.equals(DBName_quiz)) {
			return null;
		}

		// dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = context.openOrCreateDatabase(DBName_quiz,
				Context.MODE_PRIVATE,
				null);;
		Cursor cur = db.query(TabName_member, new String[] { "`_id`",
				"`name`",
				"`comefrom`",
				"`nickname`",
				"`group`",
				"`team`",
				"`birthday`" }, null, null, null, null, null);
		ArrayList<ContentValues> memberlist = new ArrayList<ContentValues>();

		if (cur.getCount() > 0) {
			if (cur.moveToFirst()) {
				do {
					ContentValues quiz = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, quiz);
					memberlist.add(quiz);
				} while (cur.moveToNext());
			}
		}
		cur.close();
		db.close();
		// dbh.close();
		return memberlist;
	}

	/**
	 * 获取小贴士
	 * FIXME ID不连续的时候 offset+1会重复某个题目
	 * 
	 * @param type 类型
	 * @param offset 防止重复的offset
	 * @return 小贴士的内容
	 */
	public String getTips(int type, int offset) {
		if (!DBname.equals(DBName_quiz)) {
			return null;
		}
		// dbh = new DatabaseHelper(context, DBname, null, quizdb_ver);
		db = context.openOrCreateDatabase(DBName_quiz,
				Context.MODE_PRIVATE,
				null);
		String sql;
		Cursor cur;
		String tips = null;
		int maxid = 0;

		switch (type) {
		case QuizType_Birthday:
			sql = "SELECT MAX(_id) FROM " + TabName_member;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();
			maxid = cur.getInt(cur.getColumnIndex("MAX(_id)"));
			cur.close();

			sql = "SELECT * FROM `" + TabName_member + "` WHERE `_id` > "
					+ offset % maxid;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();
			try {
				Date birth = fmt.parse(cur.getString(cur.getColumnIndex("birthday")));
				String birthday = outputDateFormat.format(birth);
				tips = context.getString(R.string.database_t_tips_birthday,
						cur.getString(cur.getColumnIndex("name")),
						birthday);
			}
			catch (ParseException e) {
				e.printStackTrace();
				cur.close();
				db.close();
				dbh.close();
			}
			break;
		case QuizType_Comefrom:
			sql = "SELECT MAX(_id) FROM " + TabName_member;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();
			maxid = cur.getInt(cur.getColumnIndex("MAX(_id)"));
			cur.close();

			sql = "SELECT * FROM `" + TabName_member + "` WHERE `_id` > "
					+ offset % maxid;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();

			tips = context.getString(R.string.database_t_tips_comefrom,
					cur.getString(cur.getColumnIndex("name")),
					cur.getString(cur.getColumnIndex("comefrom")));

			break;
		case QuizType_Normal:
			sql = "SELECT MAX(_id) FROM " + TabName_quiz;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();
			maxid = cur.getInt(cur.getColumnIndex("MAX(_id)"));
			cur.close();

			sql = "SELECT * FROM `" + TabName_quiz + "` WHERE `_id` > "
					+ offset % maxid;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();

			tips = context.getString(R.string.database_t_tips_normal,
					cur.getString(cur.getColumnIndex(ColName_QUESTION)),
					cur.getString(cur.getColumnIndex(ColName_ANSWER)));
			break;
		case QuizType_Team:
			sql = "SELECT MAX(_id) FROM " + TabName_member;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();
			maxid = cur.getInt(cur.getColumnIndex("MAX(_id)"));
			cur.close();

			sql = "SELECT * FROM `" + TabName_member + "` WHERE `_id` > "
					+ offset % maxid;
			cur = db.rawQuery(sql, null);
			cur.moveToFirst();

			tips = context.getString(R.string.database_t_tips_team,
					cur.getString(cur.getColumnIndex("name")),
					cur.getString(cur.getColumnIndex("group")) + " Team "
							+ cur.getString(cur.getColumnIndex("team")));
			break;
		default:
			sql = "SELECT * FROM `" + TabName_member + "` WHERE `_id` > "
					+ offset % maxid;
			cur = db.rawQuery(sql, null);

			tips = "";
			// sb.append(cur.getString(cur.getColumnIndex("name")));
			// sb.append("来自");
			// sb.append(cur.getString(cur.getColumnIndex("comefrom")));

			break;
		}
		cur.close();
		db.close();
		// dbh.close();
		return tips;
	}

	/**
	 * 获取各种类型题目的数量
	 * 
	 * @param totalNum
	 * @return
	 */
	private int[] getCountEachType(int totalNum) {
		int[] counts = { 0, 0, 0, 0, 0 };
		Random r = new Random();
		for (int i = 0; i < totalNum; i++) {
			int t = r.nextInt(QUIZ_RAND_MAX);
			if (t >= QUIZ_DIVIDE_3) {
				counts[QuizType_Normal]++;
			} else {
				if (t < QUIZ_DIVIDE_1) {
					counts[QuizType_Birthday]++;
				} else if (t < QUIZ_DIVIDE_2) {
					counts[QuizType_Comefrom]++;
				} else {
					counts[QuizType_Team]++;
				}
			}
		}
		return counts;

	}

	/**
	 * 加载除了一般题目以外题目的可用选项
	 * 注意 需要db已经打开
	 */
	private void loadOptions() {
		if (db == null || !db.isOpen()) {
			return;
		}
		if (DBname.equals(DBName_quiz)) {
			Cursor cur = db.query("member_info",
					new String[] { "comefrom" },
					"`comefrom` IS NOT NULL AND `is_show`",
					null,
					"comefrom",
					null,
					null);
			useableComefrom = new String[cur.getCount()];
			if (cur.getCount() > 0) {
				int i = 0;
				while (cur.moveToNext()) {
					useableComefrom[i] = cur.getString(cur.getColumnIndex("comefrom"));
					i++;
				}
			}

			cur = db.query("member_info",
					new String[] { "birthday" },
					"`birthday` IS NOT NULL AND `is_show`",
					null,
					"birthday",
					null,
					null);
			useableBirthday = new String[cur.getCount()];
			if (cur.getCount() > 0) {
				int i = 0;
				while (cur.moveToNext()) {
					try {
						Date birth = fmt.parse(cur.getString(cur.getColumnIndex("birthday")));

						useableBirthday[i] = outputDateFormat.format(birth);
					}
					catch (ParseException e) {
						e.printStackTrace();
					}
					i++;
				}
			}
			cur = db.query("member_info",
					new String[] { "`group`||' Team '||`team` AS `team`" },
					"`team` IS NOT NULL AND `is_show`",
					null,
					"team",
					null,
					null);
			useableTeam = new String[cur.getCount()];
			if (cur.getCount() > 0) {
				int i = 0;
				while (cur.moveToNext()) {
					useableTeam[i] = cur.getString(cur.getColumnIndex("team"));
					i++;
				}
			}
		}
	}

	public ArrayList<String> getNotice() {
		db = context.openOrCreateDatabase(DBName_quiz,
				Context.MODE_PRIVATE,
				null);
	
		
		//*/
		Cursor cur = db.query(TabName_member,
				null,
				"STRFTIME('%m-%d',`birthday`) like STRFTIME('%m-%d','now','localtime')",
				null,
				null,
				null,
				null);
		/*/
		Cursor cur = db.query(TabName_member,
			null,
			"STRFTIME('%m-%d',`birthday`) like STRFTIME('%m-%d','2013-04-28')",
			null,
			null,
			null,
			null);
		//*/
		ArrayList<String> noticeList = null;
		if (cur != null) {
			noticeList = new ArrayList<String>();
			while (cur.moveToNext()) {
				String group = cur.getString(cur.getColumnIndex("group"));
				if (group == null) {
					group = "";
				}
				String team = cur.getString(cur.getColumnIndex("team"));
				if (team == null) {
					team = "";
				} else {
					team = "Team " + team;
				}
				noticeList.add(context.getString(R.string.database_t_notice_birthday,
						group,
						team,
						cur.getString(cur.getColumnIndex("name"))));
			}
			cur.close();
		}
		db.close();
		return noticeList;

	}
}
