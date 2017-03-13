package com.sen5.ocup.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sen5.ocup.R;
import com.sen5.ocup.struct.Device;

/**
 * @author caoxia
 * @version ：2015年1月28日 下午2:03:54
 *
 * 类说明 : 设备列表适配器
 */
public class DeviceListAdapter extends BaseAdapter {

	protected static final String TAG = "DeviceListAdapter";
	private Context mContext;
	private ArrayList<Device> data_devices;

	public DeviceListAdapter(Context mContext, ArrayList<Device> data_devices) {
		super();
		this.mContext = mContext;
		this.data_devices = data_devices;

	}

	@Override
	public int getCount() {
		return data_devices.size();
	}

	@Override
	public Device getItem(int position) {
		return data_devices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (null == convertView) {
			holder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_device, null);
			holder.tv_devicename = (TextView) convertView.findViewById(R.id.tv_devicename);
			holder.tv_deviceaddr = (TextView) convertView.findViewById(R.id.tv_deviceaddr);
			holder.tv_device_connectstate = (TextView) convertView.findViewById(R.id.tv_device_connectstate);
			holder.tv_device_rssi = (TextView) convertView.findViewById(R.id.tv_device_rssi);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		if (null != holder.tv_devicename) {
			holder.tv_device_rssi.setText(data_devices.get(position).getRssi());
			holder.tv_devicename.setText(data_devices.get(position).getName());
			holder.tv_deviceaddr.setText(data_devices.get(position).getAddr());
			if (data_devices.get(position).isIsconnect()) {
				holder.tv_device_connectstate.setText(mContext.getString(R.string.disconnect));
			} else {
				holder.tv_device_connectstate.setText(mContext.getString(R.string.connect));
			}
		} else {
			Log.d(TAG, "null ========== holder.tv_devicename");
		}
		return convertView;
	}

	class Holder {
		private TextView tv_device_connectstate;
		private TextView tv_deviceaddr;
		private TextView tv_devicename;
		private TextView tv_device_rssi;
	}

}
