package de.uniweimar.kaaage.mis_2016_exercise_2_maps_halos;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/*
 * The solution described here was used - http://stackoverflow.com/questions/14013002/google-maps-android-api-v2-detect-touch-on-map
 */
public class TouchableWrapper extends FrameLayout {
	UpdateMap updateMap;

	public TouchableWrapper(Context context)
	{
		super(context);

		try
		{
			updateMap = (MapsActivity) context;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement UpdateMap");
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		updateMap.onUpdateMap();
		return super.dispatchTouchEvent(event);
	}

	public interface UpdateMap
	{
		public void onUpdateMap();
	}
}