package yeah.cstriker1407.android.rider.activity;

import java.lang.ref.WeakReference;

import yeah.cstriker1407.android.rider.R;
import yeah.cstriker1407.android.rider.service.LocationService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

public class WelcomeActivity extends Activity {
	private static final String TAG = "WelcomeActivity";
	private static final int MSG_SLEEP = 0;
	private static final int MSG_SERVICE = 1;
	private static final int SLEEP_TIME = 2000;

	private WelcomeActHandler handler = new WelcomeActHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.activity_welcome);
		handler.sendEmptyMessageDelayed(MSG_SERVICE, 500);
		handler.sendEmptyMessageDelayed(MSG_SLEEP, SLEEP_TIME);
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG, "on des called");
		super.onDestroy();
	}

	private static class WelcomeActHandler extends Handler {
		private WeakReference<WelcomeActivity> activity = null;

		public WelcomeActHandler(WelcomeActivity act) {
			super();
			this.activity = new WeakReference<WelcomeActivity>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			WelcomeActivity act = activity.get();
			if (null == act) {
				return;
			}

			switch (msg.what) {
			case MSG_SERVICE:
			{
				LocationService.startLocationService(act);
				break;
			}
			case MSG_SLEEP:
			{
				act.startActivity(new Intent(act, SpeedActivity.class));
				act.finish();
				break;
			}
			default: {
				break;
			}
			}
		}
	}

}