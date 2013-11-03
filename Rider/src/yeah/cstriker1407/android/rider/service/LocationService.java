package yeah.cstriker1407.android.rider.service;

import java.lang.ref.WeakReference;

import yeah.cstriker1407.android.rider.receiver.LocationBroadcast;
import yeah.cstriker1407.android.rider.receiver.WeatherBroadcast;
import yeah.cstriker1407.android.rider.storage.Locations;
import yeah.cstriker1407.android.rider.storage.Locations.LocDesc.LocTypeEnum;
import yeah.cstriker1407.android.rider.utils.BDUtils;
import yeah.cstriker1407.android.rider.utils.HttpUtils;
import yeah.cstriker1407.android.rider.utils.HttpUtils.onHttpResultListener;
import yeah.cstriker1407.android.rider.utils.WeatherUtils;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class LocationService extends Service implements onHttpResultListener 
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
	
	private LocationBinder binder = new LocationBinder();
	public class LocationBinder extends Binder
	{
		public void sendWeatherReq()
		{
			LocationService.this.sendWeatherReq();
		}
	}
	
	private static final String TAG = "LocationService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() 
	{
		Log.e(TAG, "The LocationService Is onCreate");
		super.onCreate();
		initBDLocationSettings();
		registerSensorMgr();
		sendWeatherReq();
		
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
		unregisterSensorMgr();
		cancelWeatherReq();
		
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
					null, location.getLatitude(),location.getLongitude(),location.getRadius(),degree,BDUtils.BD2LocTypeEnum(location));
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
		mLocationClient.setAK(BDUtils.KEY);
	
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setAddrType("nothing");// ���صĶ�λ�������Ҫ��ַ��Ϣ��ʹ��������api��ȡ��ַ��Ϣ��
		option.setCoorType("bd09ll");// ���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02
		option.setScanSpan(BDUtils.SCANSPAN);// ���÷���λ����ļ��ʱ��Ϊ5000ms
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
	
	
	//-------ָ������غ���--------//
	private SensorManager mSensorManager;
	private float degree = 0.0f;
	private SensorEventListener mSensorEventListener = new SensorEventListener() {
        
        @Override
        public void onSensorChanged(SensorEvent event) {
        	degree = event.values[0];
//            Log.d("", "" + degree);
        }
        
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    };
    
    private void registerSensorMgr()
    {
    	if (mSensorManager != null)
    	{
    		return;
    	}
    	
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener,  
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),  
                SensorManager.SENSOR_DELAY_NORMAL);  
    }
	
    private void unregisterSensorMgr()
    {
    	if (null == mSensorManager)
    	{
    		return;
    	}
    	mSensorManager.unregisterListener(mSensorEventListener);
    }
    
    
    
    
    
    //---------����Ԥ����غ���--------//
    private void sendWeatherReq()
    {
    	HttpUtils.sendGetRequest(WeatherUtils.URL_Service, this, 0);
    	handler.sendEmptyMessageDelayed(MSG_WEATHER, WeatherUtils.WEATHER_SPAN);
    }
    private void cancelWeatherReq()
    {
    	handler.removeMessages(MSG_WEATHER);
    }   
    
    
    private static final int MSG_WEATHER = 0;
	public void handleMessage(Message msg) 
	{
		switch (msg.what) {
		case MSG_WEATHER:
		{
			sendWeatherReq();
			break;
		}
		default:
			break;
		}
		
	}
   
	private LocationSerHandler handler = new LocationSerHandler(this);
    private static class LocationSerHandler extends Handler
    {
    	private WeakReference<LocationService> service = null;
    	private LocationSerHandler(LocationService ls) {
			super();
			this.service = new WeakReference<LocationService>(ls);
		}
    	@Override
		public void handleMessage(Message msg) 
		{
    		LocationService ls = service.get();
    		if (ls != null)
    		{
    			ls.handleMessage(msg);
    		}
		}
    }
	@Override
	public void onHttpResult(String result, int id)
	{
		WeatherUtils.WeatherInfo info = WeatherUtils.FmtWeatherFromJson(result);
		
		if (info != null)
		{
			Log.d(TAG, info.toString());
			WeatherBroadcast.sendBroadcast(this, info);
		}
	}
    
}
