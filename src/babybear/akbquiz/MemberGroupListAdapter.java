package babybear.akbquiz;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * 可收起的 按组分的列表 每个child有一个CheckBox
 * @author BabyBeaR
 *
 */
public class MemberGroupListAdapter extends BaseExpandableListAdapter {
	Context context;
	int childViewId;
	int groupViewId;
	String isCheckedKey;
	int checkBoxId;
	ArrayList<Map<String, String>> groupList;
	ArrayList<ArrayList<Map<String, String>>> childList;
	String groupTitleKey;
	String childTitleKey;
	int groupTitleId;
	int childTitleId;
		
	/**
	 * 构造函数
	 * @param context 上下文环境
	 * @param groupList	分组的List
	 * @param groupTitleKey group项要显示的内容在groupList中的Key
	 * @param groupViewId group项的布局文件
	 * @param groupTitleId group项的布局文件中TextView的ID
	 * @param childList 成员的List
	 * @param childTitleKey	child项要显示的内容在childList中的Key
	 * @param childViewId child项的布局文件
	 * @param childTitleId child项的布局文件中TextView的ID
	 * @param isCheckedKey child项是否已选的字段在childList中的Key
	 * @param checkerId	child项的布局文件中CheckBox的ID
	 */
	public MemberGroupListAdapter(Context context,
			ArrayList<Map<String, String>> groupList, String groupTitleKey,
			int groupViewId, int groupTitleId,
			ArrayList<ArrayList<Map<String, String>>> childList,
			String childTitleKey, int childViewId, int childTitleId,
			String isCheckedKey, int checkerId) {
		this.context = context;
		this.childViewId = childViewId;
		this.groupViewId = groupViewId;
		this.checkBoxId = checkerId;
		this.groupList = groupList;
		this.childList = childList;
		this.groupTitleKey = groupTitleKey;
		this.childTitleKey = childTitleKey;
		this.groupTitleId = groupTitleId;
		this.isCheckedKey = isCheckedKey;
		this.childTitleId = childTitleId;
	}

	@Override
	public Map<String, String> getChild(int groupPosition, int childPosition) {
		return childList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return (groupPosition << 8) + childPosition+1;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v = convertView;
		v = LayoutInflater.from(getContext()).inflate(
				childViewId, null);
		((TextView) v.findViewById(childTitleId)).setText(childList
				.get(groupPosition).get(childPosition).get(childTitleKey));

		CheckBox cb = ((CheckBox) v.findViewById(checkBoxId));
		cb.setChecked(Boolean.parseBoolean(childList.get(groupPosition)
				.get(childPosition).get(isCheckedKey)));
		return v;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childList.get(groupPosition).size();
	}

	@Override
	public Map<String, String> getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition<<8;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v = convertView;
		v = LayoutInflater.from(getContext()).inflate(
				groupViewId, null);
		((TextView) v.findViewById(groupTitleId)).setText(groupList.get(
				groupPosition).get(groupTitleKey));
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public Context getContext() {
		return context;

	}
}
