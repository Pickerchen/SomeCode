package com.sen5.ocup.activity;

import java.util.HashMap;
import java.util.Map;

import com.sen5.ocup.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HelpOcupActivity extends BaseActivity implements
		OnScrollListener {
	private ExpandableAdapter expandAdapter;
	private ExpandableListView expandableList;
	private int indicatorGroupHeight;
	private int the_group_expand_position = -1;
	private int count_expand = 0;
	private Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
	private LinearLayout view_flotage = null;
	private TextView group_content = null;
	private ImageView tubiao;
	private String[] qs;
	private String[] as;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_helpocup);
		initData();
		expandAdapter = new ExpandableAdapter(HelpOcupActivity.this);
		expandableList = (ExpandableListView) findViewById(R.id.list);
		View v = new View(this);
		expandableList.addHeaderView(v);
		expandableList.setAdapter(expandAdapter);
		expandableList.setGroupIndicator(null);
		initView();
	}

	private void initData() {
		qs = getResources().getStringArray(R.array.q);
		as = getResources().getStringArray(R.array.a);
		
	}

	public void initView() {
		/**
		 * 监听父节点打开的事件
		 */
		expandableList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				the_group_expand_position = groupPosition;
				ids.put(groupPosition, groupPosition);
				count_expand = ids.size();
				for (int i = 0; i < expandAdapter.getGroupCount(); i++) {
					if(groupPosition != i){
						expandableList.collapseGroup(i);
					}
				}
				expandableList.setSelectedGroup(groupPosition);
			}
		});
		/**
		 * 监听父节点关闭的事件
		 */
		expandableList
				.setOnGroupCollapseListener(new OnGroupCollapseListener() {
					@Override
					public void onGroupCollapse(int groupPosition) {
						ids.remove(groupPosition);
						expandableList.setSelectedGroup(groupPosition);
						count_expand = ids.size();
					}
				});
		view_flotage = (LinearLayout) findViewById(R.id.topGroup);
		view_flotage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				view_flotage.setVisibility(View.GONE);
				expandableList.collapseGroup(the_group_expand_position);
				expandableList.setSelectedGroup(the_group_expand_position);
			}
		});
		group_content = (TextView) findViewById(R.id.content_001);
		tubiao = (ImageView) findViewById(R.id.tubiao);
//		tubiao.setBackgroundResource(R.drawable.btn_browser2);
		//设置滚动事件
		expandableList.setOnScrollListener(this);
		LinearLayout layout_back = (LinearLayout)findViewById(R.id.layout_back);
		layout_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		//防止三星,魅族等手机第一个条目可以一直往下拉,父条目和悬浮同时出现的问题
		if(firstVisibleItem==0){
			view_flotage.setVisibility(View.GONE);
		}
		// 控制滑动时TextView的显示与隐藏
		int npos = view.pointToPosition(0, 0);
		if (npos != AdapterView.INVALID_POSITION) {
			long pos = expandableList.getExpandableListPosition(npos);
			int childPos = ExpandableListView.getPackedPositionChild(pos);
			final int groupPos = ExpandableListView.getPackedPositionGroup(pos);
			if (childPos == AdapterView.INVALID_POSITION) {
				View groupView = expandableList.getChildAt(npos
						- expandableList.getFirstVisiblePosition());
				indicatorGroupHeight = groupView.getHeight();
			}
			
			if (indicatorGroupHeight == 0) {
				return;
			}
			// if (isExpanded) {
			if (count_expand > 0) {
				the_group_expand_position = groupPos;
				group_content.setText(qs[the_group_expand_position]);
				if (the_group_expand_position != groupPos||!expandableList.isGroupExpanded(groupPos)) {
					view_flotage.setVisibility(View.GONE);
				} else {
//					view_flotage.setVisibility(View.VISIBLE);
					view_flotage.setVisibility(View.GONE);
				}
			}
			if (count_expand == 0) {
				view_flotage.setVisibility(View.GONE);
			}
		}

		if (the_group_expand_position == -1) {
			return;
		}
		/**
		 * calculate point (0,indicatorGroupHeight)
		 */
		int showHeight = getHeight();
		MarginLayoutParams layoutParams = (MarginLayoutParams) view_flotage
				.getLayoutParams();
		// 得到悬浮的条滑出屏幕多少
		layoutParams.topMargin = -(indicatorGroupHeight - showHeight);
		view_flotage.setLayoutParams(layoutParams);
	}

	class ExpandableAdapter extends BaseExpandableListAdapter {
		HelpOcupActivity exlistview;
		@SuppressWarnings("unused")
		private int mHideGroupPos = -1;

		public ExpandableAdapter(HelpOcupActivity elv) {
			super();
			exlistview = elv;
		}

		// **************************************
		public Object getChild(int groupPosition, int childPosition) {
			return as[groupPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return as.length/as.length;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.helpchilditem, null);
			}
			final TextView title = (TextView) view
					.findViewById(R.id.child_text);
			title.setText("\n" + as[groupPosition] + "\n\n");
			return view;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflaterGroup = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflaterGroup.inflate(R.layout.helpgroupitem, null);
			}
			TextView title = (TextView) view.findViewById(R.id.itemcontent_001);
			title.setText(getGroup(groupPosition).toString());
			ImageView image = (ImageView) view.findViewById(R.id.tubiao);

			System.out.println("isExpanded----->" + isExpanded);
			if (isExpanded) {
				image.setBackgroundResource(R.drawable.btn_browser2);
			} else {
				image.setBackgroundResource(R.drawable.btn_browser);
			}
			return view;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public Object getGroup(int groupPosition) {
			return qs[groupPosition];
		}

		public int getGroupCount() {
			return qs.length;

		}

		public boolean hasStableIds() {
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		public void hideGroup(int groupPos) {
			mHideGroupPos = groupPos;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	private int getHeight() {
		int showHeight = indicatorGroupHeight;
		int nEndPos = expandableList.pointToPosition(0, indicatorGroupHeight);
		if (nEndPos != AdapterView.INVALID_POSITION) {
			long pos = expandableList.getExpandableListPosition(nEndPos);
			int groupPos = ExpandableListView.getPackedPositionGroup(pos);
			if (groupPos != the_group_expand_position) {
				View viewNext = expandableList.getChildAt(nEndPos
						- expandableList.getFirstVisiblePosition());
				showHeight = viewNext.getTop();
			}
		}
		return showHeight;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
