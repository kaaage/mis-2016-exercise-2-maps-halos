package de.uniweimar.kaaage.mis_2016_exercise_2_maps_halos;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
 *	The official Android Developer site (http://developer.android.com/index.html) was used as a primary source
 */
public class MapsActivity extends FragmentActivity
		implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener, TouchableWrapper.UpdateMap
{
	private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
	private HashMap<Marker, Circle> mc;
	private GoogleMap mMap;
	private EditText editTextMarkerTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		MySupportMapFragment mapFragment = (MySupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		this.editTextMarkerTitle = (EditText) findViewById(R.id.editTextMarkerTitle);
	}

	/*
	 * For smooth circle drawing the solution described here was used
	 * - http://stackoverflow.com/questions/14013002/google-maps-android-api-v2-detect-touch-on-map
	 */
	@Override
	public void onUpdateMap()
	{
		drawCircles();
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		mMap = googleMap;
		mc = new HashMap<Marker, Circle>();

		mc.put(mMap.addMarker(new MarkerOptions().position(new LatLng(50.97794,11.028965)).title("Erfurt")),
				mMap.addCircle(new CircleOptions().center(new LatLng(50.97794,11.028965)).radius(0).strokeColor(Color.RED).strokeWidth(8)));
		mc.put(mMap.addMarker(new MarkerOptions().position(new LatLng(51.0292808,11.4743898)).title("Apolda")),
				mMap.addCircle(new CircleOptions().center(new LatLng(51.0292808,11.4743898)).radius(0).strokeColor(Color.RED).strokeWidth(8)));
		mc.put(mMap.addMarker(new MarkerOptions().position(new LatLng(50.6393039,11.3231428)).title("Saafeld")),
				mMap.addCircle(new CircleOptions().center(new LatLng(50.6393039,11.3231428)).radius(0).strokeColor(Color.RED).strokeWidth(8)));

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String markersString = sharedPrefs.getString("markers", null);

		if (markersString != null)
		{
			Gson gson = new Gson();
			Type markersType = new TypeToken<HashMap<String, LatLng>>()
			{
			}.getType();
			HashMap<String, LatLng> markersMap = gson.fromJson(markersString, markersType);

			for (HashMap.Entry<String, LatLng> i : markersMap.entrySet())
			{
				Marker m = mMap.addMarker(new MarkerOptions().position(i.getValue()).title(i.getKey()));
				Circle c = mMap.addCircle(new CircleOptions().center(i.getValue()).radius(0).strokeColor(Color.RED).strokeWidth(8));

				mc.put(m, c);
			}
		}

		mMap.setOnCameraChangeListener(this);
		mMap.setOnMapLongClickListener(this);

		this.moveToMyLocation();

		this.drawCircles();
	}

	/**
	 * Dispatch onPause() to fragments.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		if (mc.size() == 0)
			return;

		HashMap<String, LatLng> markersMap = new HashMap<>();
		for (HashMap.Entry<Marker, Circle> i : this.mc.entrySet())
		{
			markersMap.put(i.getKey().getTitle(), i.getKey().getPosition());
		}

		Type markersType = new TypeToken<HashMap<String, LatLng>>()
		{
		}.getType();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		Gson gson = new Gson();
		editor.putString("markers", gson.toJson(markersMap, markersType));
		editor.commit();
	}

	protected void drawCircles()
	{
		LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
		LatLng center = bounds.getCenter();

		double left = bounds.southwest.longitude;
		double top = bounds.northeast.latitude;
		double right = bounds.northeast.longitude;
		double bottom = bounds.southwest.latitude;

		LatLng topmiddle = new LatLng(top, center.longitude);
		LatLng bottommiddle = new LatLng(bottom, center.longitude);
		float[] scale = new float[1];
		Location.distanceBetween(topmiddle.latitude, topmiddle.longitude, bottommiddle.latitude, bottommiddle.longitude, scale);

		for (HashMap.Entry<Marker, Circle> i : this.mc.entrySet())
		{
			Marker m = i.getKey();
			Circle c = i.getValue();

			if (!bounds.contains(m.getPosition()))
			{
				ArrayList<Float> dist = new ArrayList<>();

				double y1 = ((left - m.getPosition().longitude) * (center.latitude - m.getPosition().latitude)) / (center.longitude - m.getPosition().longitude) + m.getPosition().latitude;
				double y2 = ((right - m.getPosition().longitude) * (center.latitude - m.getPosition().latitude)) / (center.longitude - m.getPosition().longitude) + m.getPosition().latitude;
				double x1 = ((top - m.getPosition().latitude) * (center.longitude - m.getPosition().longitude)) / (center.latitude - m.getPosition().latitude) + m.getPosition().longitude;
				double x2 = ((bottom - m.getPosition().latitude) * (center.longitude - m.getPosition().longitude)) / (center.latitude - m.getPosition().latitude) + m.getPosition().longitude;

				if (bottom <= y1 && y1 <= top)
				{
					float[] res = new float[1];
					Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude, y1, left, res);
					dist.add(res[0]);
				}
				if (bottom <= y2 && y2 <= top)
				{
					float[] res = new float[1];
					Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude, y2, right, res);
					dist.add(res[0]);
				}
				if (left <= x1 && x1 <= right)
				{
					float[] res = new float[1];
					Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude, top, x1, res);
					dist.add(res[0]);
				}
				if (left <= x2 && x2 <= right)
				{
					float[] res = new float[1];
					Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude, bottom, x2, res);
					dist.add(res[0]);
				}

				c.setRadius(dist.size() > 0 ? Collections.min(dist).floatValue() + scale[0] / 30 : 0);
			}
			else
			{
				c.setRadius(0);
			}
		}
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition)
	{
		this.drawCircles();
	}

	@Override
	public void onMapLongClick(LatLng latLng)
	{
		String title = editTextMarkerTitle.getText().toString();
		if (title.length() > 0)
		{
			this.mc.put(mMap.addMarker(new MarkerOptions().position(latLng).title(title)),
					mMap.addCircle(new CircleOptions().center(latLng).radius(0).strokeColor(Color.RED).strokeWidth(8)));
			editTextMarkerTitle.setText("");
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "Please specify the title", Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void moveToMyLocation()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
					MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
		}
		else
		{
			mMap.setMyLocationEnabled(true);

			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			Criteria criteria = new Criteria();

			Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
			if (location != null)
			{
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(location.getLatitude(), location.getLongitude()), 13));

				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(10).build();
				mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
		switch (requestCode)
		{
			case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					moveToMyLocation();
				}
				else
				{
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
				}

				return;
			}
		}
	}
}
