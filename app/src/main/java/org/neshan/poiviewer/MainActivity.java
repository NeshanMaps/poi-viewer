package org.neshan.poiviewer;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.carto.styles.AnimationStyle;
import com.carto.styles.AnimationStyleBuilder;
import com.carto.styles.AnimationType;
import com.carto.styles.MarkerStyle;
import com.carto.styles.MarkerStyleBuilder;

import org.neshan.common.model.LatLng;
import org.neshan.mapsdk.MapView;
import org.neshan.mapsdk.internal.utils.BitmapUtils;
import org.neshan.mapsdk.model.Marker;
import org.neshan.poiviewer.adapter.SearchAdapter;
import org.neshan.servicessdk.search.NeshanSearch;
import org.neshan.servicessdk.search.model.Item;
import org.neshan.servicessdk.search.model.NeshanSearchResult;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements SearchAdapter.OnSearchItemListener {

    private static final String BAKERY = "نانوایی";
    private static final String MOSQUE = "مسجد";
    private static final String BANK = "بانک";

    private MapView mapView;
    private Button btnMosques;
    private Button btnBanks;
    private Button btnBakery;

    private List<Item> items;
    private SearchAdapter adapter;
    private Runnable runnable;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapview);
        btnMosques = findViewById(R.id.btn_mosque);
        btnBanks = findViewById(R.id.btn_banks);
        btnBakery = findViewById(R.id.btn_bakery);

        mapView.getSettings().setZoomControlsEnabled(true);

        items = new ArrayList<>();
        adapter = new SearchAdapter(items, this);

        btnMosques.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(mapView.getCameraTargetPosition(), MOSQUE);
            }
        });

        btnBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(mapView.getCameraTargetPosition(), BANK);
            }
        });

        btnBakery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search(mapView.getCameraTargetPosition(), BAKERY);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mapView.getSettings().setZoomControlsEnabled(true);
    }

    private void search(LatLng searchPosition, String text) {
        //TODO: Replace YOUR-API-KEY with your api key
        new NeshanSearch.Builder("YOUR-API-KEY")
                .setLocation(searchPosition)
                .setTerm(text)
                .build().call(new Callback<NeshanSearchResult>() {
                    @Override
                    public void onResponse(Call<NeshanSearchResult> call, retrofit2.Response<NeshanSearchResult> response) {
                        if (response.code() == 403) {
                            Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.body() != null) {
                            mapView.clearMarkers();
                            NeshanSearchResult result = response.body();
                            items = result.getItems();
                            for (Item item : items) {
                                if (text.equals(MOSQUE) && item.getType().equals("mosque")) {
                                    mapView.addMarker(createMarker(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()), R.drawable.mosque));
                                }
                                if (text.equals(BANK) && item.getType().equals("bank")) {
                                    mapView.addMarker(createMarker(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()), R.drawable.bank));
                                }
                                if (text.equals(BAKERY) && item.getType().equals("bakery")) {
                                    mapView.addMarker(createMarker(new LatLng(item.getLocation().getLatitude(), item.getLocation().getLongitude()), R.drawable.bakery));
                                }
                            }
                            if (items != null && !items.isEmpty() && items.get(0) != null) {
                                mapView.moveCamera(new LatLng(items.get(0).getLocation().getLatitude(), items.get(0).getLocation().getLongitude()), .5f);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NeshanSearchResult> call, Throwable t) {
                        Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onSeachItemClick(LatLng latLng) {
        closeKeyBoard();
        adapter.updateList(new ArrayList<Item>());
        mapView.setZoom(16f, 0);
        mapView.moveCamera(latLng, 0);
    }

    private void closeKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private Marker createMarker(LatLng loc, int markerDrawable) {
        // Creating animation for marker. We should use an object of type AnimationStyleBuilder, set
        // all animation features on it and then call buildStyle() method that returns an object of type
        // AnimationStyle
        AnimationStyleBuilder animStBl = new AnimationStyleBuilder();
        animStBl.setFadeAnimationType(AnimationType.ANIMATION_TYPE_SMOOTHSTEP);
        animStBl.setSizeAnimationType(AnimationType.ANIMATION_TYPE_SPRING);
        animStBl.setPhaseInDuration(0.5f);
        animStBl.setPhaseOutDuration(0.5f);
        AnimationStyle animSt = animStBl.buildStyle();

        // Creating marker style. We should use an object of type MarkerStyleCreator, set all features on it
        // and then call buildStyle method on it. This method returns an object of type MarkerStyle
        MarkerStyleBuilder markStCr = new MarkerStyleBuilder();
        markStCr.setSize(20f);
        markStCr.setBitmap(BitmapUtils.createBitmapFromAndroidBitmap(BitmapFactory.decodeResource(getResources(), markerDrawable)));
        // AnimationStyle object - that was created before - is used here
        markStCr.setAnimationStyle(animSt);
        MarkerStyle markSt = markStCr.buildStyle();

        // Creating marker
        return new Marker(loc, markSt);
    }
}