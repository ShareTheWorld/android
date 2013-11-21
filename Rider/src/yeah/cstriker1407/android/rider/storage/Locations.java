package yeah.cstriker1407.android.rider.storage;

import java.io.Serializable;
import java.util.Date;

import yeah.cstriker1407.android.rider.R;
import yeah.cstriker1407.android.rider.storage.Locations.LocDesc.LocTypeEnum;
import yeah.cstriker1407.android.rider.utils.GPSUtils;
import yeah.cstriker1407.android.rider.utils.TimeUtils;
import android.os.Parcel;
import android.os.Parcelable;
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
	
	public LocDesc getLocDes(String locAddr, double latitude, double longitude,
			float accuracy, float direction, Date currDate,LocTypeEnum typeEnum)
	{
		return new LocDesc(locAddr, latitude, longitude, accuracy, direction, currDate,typeEnum);
	}
	
	public SpeedInfo calcSpeedInfo(Date currDate, double latitude, double longitude)
	{
		if (null == startDate)
		{//��һ�θ���λ�õ�ʱ�򱣴�ʱ��
			startDate = currDate;
		}
		if (lastLoc.isInValid())
		{//�ϴ�����Ч��ַ�������ε�ַ���ƽ�ȥ��Ӧ��ֻ��ȥһ��
			lastLoc.update(latitude, longitude, currDate);
		}
		
		//���㵥���ٶ�
		float singleDistanceM = GPSUtils.calcDistance(lastLoc.latitude,lastLoc.longitude,latitude,longitude);
		int singleMS = TimeUtils.msBetweenDates(lastLoc.locDate, currDate);
		int singleSpeedM = 0;
		if (singleMS > 0)
		{
			singleSpeedM = (int) (singleDistanceM * 1000 / singleMS);
		}
//		Log.d(TAG, "distace:" + singleDistanceM + "  ms:" + singleMS + "  speedinM:"+ singleSpeedM);
		
		//����ƽ���ٶ�
		totalDistanceM += singleDistanceM;
		int totalMS = TimeUtils.msBetweenDates(startDate, currDate);
		int averageSpeedM = 0;
		if (totalMS > 0)
		{
			averageSpeedM = (int) (totalDistanceM * 1000 / totalMS);
		}		
		//����
		lastLoc.update(latitude, longitude, currDate);
		
		return new SpeedInfo(currDate, singleSpeedM, 0, 0, averageSpeedM, totalDistanceM, totalMS/1000);
	}
	
	
	public static class LocDesc implements Serializable
	{
		private static final long serialVersionUID = 5948064472859405460L;
		
		public static enum LocTypeEnum
		{
			GPS(R.drawable.loc_gps),
			NET(R.drawable.loc_net),
			FAIL(R.drawable.loc_fail);
			private LocTypeEnum(int imageId) {
				this.imageId = imageId;
			}
			private int imageId;
			public int getImageId() {
				return imageId;
			}
		};
		@Deprecated
		public String locAddr = null;
		public double latitude = 118;
		public double longitude = 32;
		public float accuracy = 0;
		public float direction = 0;
		public Date currDate = null;
		public LocTypeEnum typeEnum = LocTypeEnum.FAIL;
		
		public LocDesc()
		{}

		private LocDesc(String locAddr, double latitude, double longitude,
				float accuracy, float direction, Date currDate,
				LocTypeEnum typeEnum) {
			super();
			this.locAddr = locAddr;
			this.latitude = latitude;
			this.longitude = longitude;
			this.accuracy = accuracy;
			this.direction = direction;
			this.currDate = currDate;
			this.typeEnum = typeEnum;
		}
		@Override
		public String toString() {
			return "LocDesc [locAddr=" + locAddr + ", latitude=" + latitude
					+ ", longitude=" + longitude + ", accuracy=" + accuracy
					+ ", direction=" + direction + ", currDate=" + currDate
					+ ", typeEnum=" + typeEnum + "]";
		}
	}  
	
	public static class SpeedInfo implements Serializable
	{
		private static final long serialVersionUID = 6217979544567843027L;
		
		public Date currDate = null;
		public int singleSpeedM = 0;//��ǰ�ٶ� m/s
		public int maxSpeedM = 0;//����ٶ� m/s
		public int minSpeedM = 0;//��С�ٶ� m/s
		public int averageSpeedM = 0;//ƽ���ٶ� m/s
		public int totalDistanceM = 0;//����� m
		public int totalSeconds = 0;//��ʱ�� s
		
		public SpeedInfo()
		{
			
		}
		
		public SpeedInfo(Date currDate, int singleSpeedM, int maxSpeedM,
				int minSpeedM, int averageSpeedM, int totalDistanceM,
				int totalSeconds) {
			super();
			this.currDate = currDate;
			this.singleSpeedM = singleSpeedM;
			this.maxSpeedM = maxSpeedM;
			this.minSpeedM = minSpeedM;
			this.averageSpeedM = averageSpeedM;
			this.totalDistanceM = totalDistanceM;
			this.totalSeconds = totalSeconds;
		}

		@Override
		public String toString() {
			return "SpeedInfo [currDate=" + currDate + ", singleSpeedM="
					+ singleSpeedM + ", maxSpeedM=" + maxSpeedM
					+ ", minSpeedM=" + minSpeedM + ", averageSpeedM="
					+ averageSpeedM + ", totalDistanceM=" + totalDistanceM
					+ ", totalSeconds=" + totalSeconds + "]";
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
