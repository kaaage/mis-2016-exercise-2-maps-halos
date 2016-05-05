package de.uniweimar.kaaage.mis_2016_exercise_2_maps_halos;

import com.google.android.gms.maps.SupportMapFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 * The solution described here was used - http://stackoverflow.com/questions/14013002/google-maps-android-api-v2-detect-touch-on-map
 */
public class MySupportMapFragment extends SupportMapFragment {
	public View mOriginalContentView;
	public TouchableWrapper mTouchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
		mTouchView = new TouchableWrapper(getActivity());
		mTouchView.addView(mOriginalContentView);
		return mTouchView;
	}

	@Override
	public View getView() {
		return mOriginalContentView;
	}
}