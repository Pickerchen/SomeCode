package com.sen5.ocup.alarm;

import java.util.List;

import com.sen5.ocup.R;
import com.sen5.ocup.gui.*;
import com.sen5.ocup.gui.SwitchView.OnChangedListener;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 闹钟列表适配器
 */
public class AlarmClockAdapter extends BaseAdapter {

	protected static final String TAG = "AlarmClockAdapter";
	private LayoutInflater layoutInflater;
	private Context context;
	private ListView listview;
	private List<Time_show> alarm_time;

	public AlarmClockAdapter(Context context, ListView listview, List<Time_show> alarm_time) {
		this.context = context;
		this.listview = listview;
		this.alarm_time = alarm_time;
		this.layoutInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return alarm_time.size();
	}

	public Object getItem(int position) {
		return alarm_time.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView------------alarm_time.get(position).isFlag()===" + alarm_time.get(position).isFlag());
		ZuJian zuJian = null;
		if (convertView == null) {
			zuJian = new ZuJian();
			convertView = layoutInflater.inflate(R.layout.alarm_clock, null);
			zuJian.alarmTimeView = (TextView) convertView.findViewById(R.id.alarm_time);
			zuJian.SwitchView = (SwitchView) convertView.findViewById(R.id.show_open_close);
			convertView.setTag(zuJian);
		} else {
			zuJian = (ZuJian) convertView.getTag();
		}

		zuJian.SwitchView.set2bluetooth(false);
		zuJian.alarmTimeView.setText(alarm_time.get(position).getTime());
		if (alarm_time.get(position).isFlag() == 1) {
			zuJian.SwitchView.setChecked(zuJian.SwitchView, true);
		} else {
			zuJian.SwitchView.setChecked(zuJian.SwitchView, false);
		}
		zuJian.SwitchView.SetOnChangedListener(new OnChangedListener() {

			@Override
			public void OnChanged(View view, boolean checkState) {
				if (view == null) {
					Log.d(TAG, "SetOnChangedListener--view==null");
				} else if (listview == null) {
					Log.d(TAG, "SetOnChangedListener--listview==null");
				} else {
					try {
						int position = listview.getPositionForView(view);
						Log.d(TAG, "SetOnChangedListener-----------position==" + position);
						if (checkState == true) {
							alarm_time.get(position).setFlag(1);
						} else {
							alarm_time.get(position).setFlag(0);
						}
					} catch (Exception e) {
						Log.d(TAG, "SetOnChangedListener---Exception e=="+e);
					}
					
				}
			}
		});
		return convertView;
	}

	final class ZuJian {
		public TextView alarmTimeView;
		public SwitchView SwitchView;

	}
}
