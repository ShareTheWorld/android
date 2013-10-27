package yeah.cstriker1407.android.rider.service;

import yeah.cstriker1407.android.rider.receiver.LocationBroadcast;
import yeah.cstriker1407.android.rider.storage.Locations;
import yeah.cstriker1407.android.rider.storage.Locations.LocDesc.LocTypeEnum;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationService extends Service 
{
	public static void startLocationService(Context context)
	{
		if (null == context)
		{
			return;
		}
		context = context.getApplicationContext();
		context.startService(new Intent(context, LocationService.class));
	}
	public static void stopLocationService(Context context)
	{
		if (null == context)
		{
			return;
		}
		context = context.getApplicationContext();
		context.stopService(new Intent(context, LocationService.class));
	}
	
	private static final String TAG = "LocationService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() 
	{
		Log.e(TAG, "The LocationService Is onCreate");
		super.onCreate();
		initBDLocationSettings();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		return super.onStartCommand(intent, flags, startId);
		Log.e(TAG, "The LocationService Is onStartCommand");
		startBDLocationFunc();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() 
	{
		Log.e(TAG, "The LocationService Is onDestroy");
		stopBDLocationFunc();
		super.onDestroy();
	}
	
	
	
	
	//---------�ٶȶ�λ��غ���--------//
		
	private LocationClient mLocationClient = null;
	private BDLocationListener bdLocationListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			if (location == null)
				return;
			Locations.LocDesc locDesc = Locations.getInstance().getLocDes(
					null, location.getLatitude(),location.getLongitude(),location.getRadius(),location.getDerect(),BD2LocTypeEnum(location));
			Log.d(TAG,locDesc.toString());
			
			Locations.SpeedInfo speedInfo = Locations.getInstance().calcSpeedInfo(
					location.getLatitude(),
					location.getLongitude());
			Log.d(TAG,speedInfo.toString());
			
			LocationBroadcast.sendBroadcast(LocationService.this, locDesc, speedInfo);

		}
		@Override
		public void onReceivePoi(BDLocation poiLocation) {}
	};
	
	private void initBDLocationSettings() {
		mLocationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
		mLocationClient.registerLocationListener(bdLocationListener); // ע���������
		mLocationClient.setAK("F0488f2ee7d14e2bba215419efb9bff3");
	
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("nothing");// ���صĶ�λ�������Ҫ��ַ��Ϣ��ʹ��������api��ȡ��ַ��Ϣ��
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		option.setScanSpan(5000);// ���÷���λ����ļ��ʱ��Ϊ5000ms
		option.disableCache(true);// ��ֹ���û��涨λ
		option.setPoiExtraInfo(false); // �Ƿ���ҪPOI�ĵ绰�͵�ַ����ϸ��Ϣ
		option.setPriority(LocationClientOption.GpsFirst);
		mLocationClient.setLocOption(option);
	}

	private void startBDLocationFunc() {
		if (mLocationClient != null && (!mLocationClient.isStarted()))
		{
			mLocationClient.start();
			mLocationClient.requestLocation();
		}
	}

	private void stopBDLocationFunc() {
		if (mLocationClient != null && mLocationClient.isStarted()) 
		{
			mLocationClient.stop();
		}
	}
	
	private static Locations.LocDesc.LocTypeEnum BD2LocTypeEnum(BDLocation location) 
	{
		Locations.LocDesc.LocTypeEnum locTypeEnum;
		switch (location.getLocType()) 
		{
			case BDLocation.TypeGpsLocation:
			{
				locTypeEnum = LocTypeEnum.GPS;
				break;
			}
			case BDLocation.TypeNetWorkLocation:
			{
				locTypeEnum = LocTypeEnum.NET;
				break;
			}
			default:
			{
				locTypeEnum = LocTypeEnum.FAIL;
				break;
			}
		}
		return locTypeEnum;
	}
}
