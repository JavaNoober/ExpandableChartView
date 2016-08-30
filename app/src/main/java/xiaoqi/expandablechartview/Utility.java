package xiaoqi.expandablechartview;

import android.content.Context;

/**
 * Created by xiaoqi on 2016/8/30.
 */
public class Utility {
	public static int dip2px(Context context, float px) {
		final float scale = getScreenDensity(context);
		return (int) (px * scale + 0.5);
	}

	private static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}
}
