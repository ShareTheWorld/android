package yeah.cstriker1407.android.rider.storage;

import java.util.Date;

import yeah.cstriker1407.android.rider.utils.GPSUtils;
import yeah.cstriker1407.android.rider.utils.TimeUtils;
import android.util.Log;

public class Locations 
{
	private static final String TAG = "Locations";
	//����===
	private static Locations instance = new Locations();
	public static Locations getInstance()
	{
		return instance;
	}
	private Locations()
	{
		//do nth
	}
	//����===
	
	private InternLocation lastLoc = new InternLocation();
	private int totalDistanceM = 0;//�����(��)
	private Date startDate = null;//��ʼ��ʱ��
	
	/* �ϴε�λ���Ƿ���Ч */
	public void addToTotalDistanceM(int newM)
	{
		if (newM < 0)
		{
			Log.e(TAG, "addToTotalDistanceM Error: " + newM);
			return;
		}
		totalDistanceM += newM;
	}
	
	
	public SpeedInfo calcSpeedInfo(double latitude, double longitude, String addr)
	{
		Date currDate = new Date();
		if (null == startDate)
		{//��һ�θ���λ�õ�ʱ�򱣴�ʱ��
			startDate = currDate;
		}
		if (lastLoc.isInValid())
		{//�ϴ�����Ч��ַ�������ε�ַ���ƽ�ȥ��Ӧ��ֻ��ȥһ��
			lastLoc.update(latitude, longitude, currDate);
		}
		
		//���㵥���ٶ�
		float singleDistanceKM = GPSUtils.calcDistance(lastLoc.latitude,lastLoc.longitude,latitude,longitude);
		int singleMS = TimeUtils.msBetweenDates(lastLoc.locDate, currDate);
		int singleSpeedM = 0;
		if (singleMS > 0)
		{
			singleSpeedM = (int) (singleDistanceKM * 1000000 / singleMS);
		}
		Log.d(TAG, "distace:" + singleDistanceKM + "  ms:" + singleMS + "  speedinM:"+ singleSpeedM);
		
		//����ƽ���ٶ�
		totalDistanceM += singleDistanceKM * 1000;
		int totalMS = TimeUtils.msBetweenDates(startDate, currDate);
		int averageSpeedM = 0;
		if (totalMS > 0)
		{
			averageSpeedM = (int) (totalDistanceM * 1000 / totalMS);
		}		
		//����
		lastLoc.update(latitude, longitude, currDate);
		
		return new SpeedInfo(singleSpeedM, 0, 0, averageSpeedM, totalDistanceM, totalMS/1000, addr);
	}
	
	public static class SpeedInfo
	{
		public int singleSpeedM = 0;//��ǰ�ٶ� m/s
		public int maxSpeedM = 0;//����ٶ� m/s
		public int minSpeedM = 0;//��С�ٶ� m/s
		public int averageSpeedM = 0;//ƽ���ٶ� m/s
		public int totalDistanceM = 0;//����� m
		public int totalSeconds = 0;//��ʱ�� s
		public String locDes = null;
		
		private SpeedInfo(int singleSpeedM, int maxSpeedM, int minSpeedM,
				int averageSpeedM, int totalDistanceM, int totalSeconds,
				String locDes) {
			super();
			this.singleSpeedM = singleSpeedM;
			this.maxSpeedM = maxSpeedM;
			this.minSpeedM = minSpeedM;
			this.averageSpeedM = averageSpeedM;
			this.totalDistanceM = totalDistanceM;
			this.totalSeconds = totalSeconds;
			this.locDes = locDes;
		}

		@Override
		public String toString() {
			return "SpeedInfo [singleSpeedM=" + singleSpeedM + ", maxSpeedM="
					+ maxSpeedM + ", minSpeedM=" + minSpeedM
					+ ", averageSpeedM=" + averageSpeedM + ", totalDistanceM="
					+ totalDistanceM + ", totalSeconds=" + totalSeconds
					+ ", locDes=" + locDes + "]";
		}
	}
	
	
	
	private static class InternLocation
	{
		public boolean isInValid()
		{
			return 0 == latitude && 0 == longitude;
		}
		public double latitude = 0.0;
		public double longitude = 0.0;
		public Date   locDate = null;
		public String locDes = null;
		
		public InternLocation()
		{
		}
		public void update(double latitude, double longitude, Date locDate) 
		{
			this.latitude = latitude;
			this.longitude = longitude;
			this.locDate = locDate;
		}
	}
}
