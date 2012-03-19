package obd.manu;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class Class_Notifier extends BroadcastReceiver {

	public static final String BROADCAST_TRIGGER_CHANGED = "ch.amana.android.cputuner.triggerChanged";
	public static final String BROADCAST_PROFILE_CHANGED = "ch.amana.android.cputuner.profileChanged";
	public static final String BROADCAST_DEVICESTATUS_CHANGED = "ch.amana.android.cputuner.deviceStatusChanged";

	public static final int NOTIFICATION_ID = 1;
	private final NotificationManager notificationManager;
	private final Context context;
	private String contentTitle;
	private PendingIntent contentIntent;
	private CharSequence lastContentText;

	private static Class_Notifier instance;
	private Notification notification;
	private int icon;

	public static Notification startStatusbarNotifications(Context ctx) {
		if (instance == null) {
			instance = new Class_Notifier(ctx);
		}
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_TRIGGER_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_PROFILE_CHANGED));
		ctx.registerReceiver(instance, new IntentFilter(BROADCAST_DEVICESTATUS_CHANGED));
		instance.notifyStatus();
		return instance.notification;
	}

	public static void stopStatusbarNotifications(Context ctx) {
		try {
			ctx.unregisterReceiver(instance);
			instance.notificationManager.cancel(NOTIFICATION_ID);
		} catch (Throwable e) {
		}
		instance = null;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}
		notifyStatus();
		//		if (SettingsStorage.getInstance(context).hasWidget()) {
		//			// the widget checks if it exists
		//			ProfileAppwidgetProvider.updateView(context);
		//		}
	}

	public  Class_Notifier(final Context ctx) {
		super();
		this.context = ctx.getApplicationContext();
		String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = (NotificationManager) ctx.getSystemService(ns);
	}

	private void notifyStatus() {
		/*PowerProfiles powerProfiles = PowerProfiles.getInstance(context);
		CharSequence profileName = powerProfiles.getCurrentProfileName();
		if (!PowerProfiles.UNKNOWN.equals(profileName)) {
			StringBuffer sb = new StringBuffer(25);
			String contentText = null;
			if (SettingsStorage.getInstance().isEnableProfiles()) {
				sb.append(context.getString(R.string.labelCurrentProfile));
				sb.append(" ").append(PowerProfiles.getInstance().getCurrentProfileName());
				if (PowerProfiles.getInstance().isManualProfile()) {
					sb.append(" (").append(context.getString(R.string.msg_manual_profile)).append(")");
				}
				sb.append(" - ").append(context.getString(R.string.labelCurrentBattery)).append(" ");
				sb.append(powerProfiles.getBatteryLevel()).append(context.getString(R.string.percent));
				if (PulseHelper.getInstance(context).isPulsing()) {
					int res = PulseHelper.getInstance(context).isOn() ? R.string.labelPulseOn : R.string.labelPulseOff;
					sb.append(" - ").append(context.getString(res));
				}
				contentText = sb.toString();
			} else {
				contentText = context.getString(R.string.msg_cpu_tuner_not_running);
			}
			if (contentText == null || contentText.equals(lastContentText)) {
				return;
			}*/
		String contentText = "Manu";
			lastContentText = contentText;
			contentTitle = context.getString(R.string.app_name);
			Notification notification = getNotification(contentText);
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			try {
				notificationManager.notify(NOTIFICATION_ID, notification);
			} catch (Exception e) {
				//Logger.e("Cannot notify " + notification);
			}
		
	}

	private Notification getNotification(CharSequence contentText) {
		boolean isDisplayNotification = true; //SettingsStorage.getInstance().isStatusbarNotifications();
		int iconNew = R.drawable.icon;
	/*	if (!SettingsStorage.getInstance().isEnableProfiles()) {
			iconNew = R.drawable.icon_red;
		} else if (PowerProfiles.getInstance().isManualProfile()) {
			iconNew = R.drawable.icon_yellow;
		} */
		if (isDisplayNotification || notification == null || icon != iconNew) {
			if (!isDisplayNotification) {
				contentText = "";
			}
			icon = iconNew;
			notification = new Notification(icon, contentText, System.currentTimeMillis());
			//contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, Frame_OBD_Data_Logger.getInstanceCount(), NOTIFICATION_ID);

			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
		}
		return notification;
	}

}