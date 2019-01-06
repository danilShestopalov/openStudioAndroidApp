package com.yuyakaido.android.cardstackview.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.RewindAnimationSetting;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CardStackListener {

    private DrawerLayout drawerLayout;

    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;
    private CardStackView cardStackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigation();
        setupCardStackView();
        setupButton();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCardDragging(Direction direction, float ratio) {
        Log.d("CardStackView", "onCardDragging: d = " + direction.name() + ", r = " + ratio);
    }

    @Override
    public void onCardSwiped(Direction direction) {
        Log.d("CardStackView", "onCardSwiped: p = " + manager.getTopPosition() + ", d = " + direction);
        if (manager.getTopPosition() == adapter.getItemCount() - 5) {
            paginate();
        }
    }

    @Override
    public void onCardRewound() {
        Log.d("CardStackView", "onCardRewound: " + manager.getTopPosition());
    }

    @Override
    public void onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled:" + manager.getTopPosition());
    }

    private void setupNavigation() {
        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        // NavigationView
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.reload:
                        reload();
                        break;
                    case R.id.add_one_spot_at_first:
                        addFirst(1);
                        break;
                    case R.id.add_two_spots_at_first:
                        addFirst(2);
                        break;
                    case R.id.add_one_spot_at_last:
                        addLast(1);
                        break;
                    case R.id.add_two_spots_at_last:
                        addLast(2);
                        break;
                    case R.id.remove_one_spot_at_first:
                        removeFirst(1);
                        break;
                    case R.id.remove_two_spots_at_first:
                        removeFirst(2);
                        break;
                    case R.id.remove_one_spot_at_last:
                        removeLast(1);
                        break;
                    case R.id.remove_two_spots_at_last:
                        removeLast(2);
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setupCardStackView() {
        initialize();
    }

    private void setupButton() {
        View skip = findViewById(R.id.skip_button);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Left)
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .build();
                manager.setSwipeAnimationSetting(setting);
                cardStackView.swipe();
            }
        });

//        View rewind = findViewById(R.id.rewind_button);
//        rewind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RewindAnimationSetting setting = new RewindAnimationSetting.Builder()
//                        .setDirection(Direction.Bottom)
//                        .setDuration(200)
//                        .setInterpolator(new DecelerateInterpolator())
//                        .build();
//                manager.setRewindAnimationSetting(setting);
//                cardStackView.rewind();
//            }
//        });

        View like = findViewById(R.id.like_button);
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                        .setDirection(Direction.Right)
                        .setDuration(200)
                        .setInterpolator(new AccelerateInterpolator())
                        .build();
                manager.setSwipeAnimationSetting(setting);
                cardStackView.swipe();
            }
        });
    }

    private void initialize() {
        manager = new CardStackLayoutManager(getApplicationContext(), this);
        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.HORIZONTAL);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(true);
        adapter = new CardStackAdapter(this, createSpots());
        cardStackView = findViewById(R.id.card_stack_view);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
    }

    private void paginate() {
        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = new ArrayList<Spot>() {{
            addAll(adapter.getSpots());
            addAll(createSpots());
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void reload() {
        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = createSpots();
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void addFirst(final int size) {
        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = new ArrayList<Spot>() {{
            addAll(adapter.getSpots());
            for (int i = 0; i < size; i++) {
                add(manager.getTopPosition(), createSpot());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void addLast(final int size) {
        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = new ArrayList<Spot>() {{
            addAll(adapter.getSpots());
            for (int i = 0; i < size; i++) {
                add(createSpot());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeFirst(final int size) {
        if (adapter.getSpots().isEmpty()) {
            return;
        }

        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = new ArrayList<Spot>() {{
            addAll(adapter.getSpots());
            for (int i = 0; i < size; i++) {
                remove(manager.getTopPosition());
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private void removeLast(final int size) {
        if (adapter.getSpots().isEmpty()) {
            return;
        }

        List<Spot> oldList = adapter.getSpots();
        List<Spot> newList = new ArrayList<Spot>() {{
            addAll(adapter.getSpots());
            for (int i = 0; i < size; i++) {
                remove(size() - 1);
            }
        }};
        SpotDiffCallback callback = new SpotDiffCallback(oldList, newList);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        adapter.setSpots(newList);
        result.dispatchUpdatesTo(adapter);
    }

    private Spot createSpot() {
        return new Spot("Danil", "ОПИСАНИЕ", "https://unsplash.com/photos/iepNMi_GrdY");
    }

    private List<Spot> createSpots() {
        List<Spot> spots = new ArrayList<>();
        spots.add(new Spot("Бэкэнд разработчик\n" + "Умная кормушка для кота\n",
                "Умный, здоровый и эффективный способ дистанционно кормить вашего кота всегда свежей едой.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d18/e46f720f675a/picture_for_the_app_1.jpg?extra=MRrFvvbWMVhYGoyq3ULiEf_6qd5Lpq6PfV1BhtUAFBF_5QJePwkvFwbMKFOXAEzJOOFT99zi0FiwZABFJ2_cyiGntOKyqMCeWdhZJpXnQy0T6zEJBg2AZ7-9-essyIo3w6ZAV-OmnrDdqbceOErO8xeU"));
        spots.add(new Spot("Фронтэнд разработчик\n" +
                "Сайт медицинского стартапа\n", "Проект специализирующийся на разработке скрининг–тестов по определению рака на ранних стадиях.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d17/ec87fd552a99/picture_for_the_app_2.jpg?extra=QXl-RBZlI7tE-23ekbj23g6TLDjBKqEYtoaVXI9LDorCjARpm4byCJQcFDFt05gsNYhsvmsVLFn1U5N6tbRp5vOyVoiGQJ8olR__xTPXUK94mP4BPttmno-MGrKIcv7u3XCI3zf_hpV6UGoeh-vWvDsG"));
        spots.add(new Spot("Бэкэнд разработчик\n" +
                "Приложение для заказа такси\n", "На текущий момент признан одним из главных конкурентов Uber на Ближнем Востоке.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d5/8cae4790dbfc/picture_for_the_app_3.jpg?extra=viW0LL6vJjXZRLwYQPmVLquAYfa4tWVI1GB7s8qxZFC2zSSEoyF1_vMDluF_ckSaI70WmhEGIJ1VnsJrJCwhynEOL5RqbL0N6JVeHV-PhLN6ikm69p0zPzWQY-WVQuLrTNoriGcgEWlKoO8G7jf9AyO9"));
        spots.add(new Spot("Бэкэнд разработчик\n" + "Умная кормушка для кота\n",
                "Умный, здоровый и эффективный способ дистанционно кормить вашего кота всегда свежей едой.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d18/e46f720f675a/picture_for_the_app_1.jpg?extra=MRrFvvbWMVhYGoyq3ULiEf_6qd5Lpq6PfV1BhtUAFBF_5QJePwkvFwbMKFOXAEzJOOFT99zi0FiwZABFJ2_cyiGntOKyqMCeWdhZJpXnQy0T6zEJBg2AZ7-9-essyIo3w6ZAV-OmnrDdqbceOErO8xeU"));
        spots.add(new Spot("Фронтэнд разработчик\n" +
                "Сайт медицинского стартапа\n", "Проект специализирующийся на разработке скрининг–тестов по определению рака на ранних стадиях.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d17/ec87fd552a99/picture_for_the_app_2.jpg?extra=QXl-RBZlI7tE-23ekbj23g6TLDjBKqEYtoaVXI9LDorCjARpm4byCJQcFDFt05gsNYhsvmsVLFn1U5N6tbRp5vOyVoiGQJ8olR__xTPXUK94mP4BPttmno-MGrKIcv7u3XCI3zf_hpV6UGoeh-vWvDsG"));
        spots.add(new Spot("Бэкэнд разработчик\n" +
                "Приложение для заказа такси\n", "На текущий момент признан одним из главных конкурентов Uber на Ближнем Востоке.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d5/8cae4790dbfc/picture_for_the_app_3.jpg?extra=viW0LL6vJjXZRLwYQPmVLquAYfa4tWVI1GB7s8qxZFC2zSSEoyF1_vMDluF_ckSaI70WmhEGIJ1VnsJrJCwhynEOL5RqbL0N6JVeHV-PhLN6ikm69p0zPzWQY-WVQuLrTNoriGcgEWlKoO8G7jf9AyO9"));
        spots.add(new Spot("Бэкэнд разработчик\n" + "Умная кормушка для кота\n",
                "Умный, здоровый и эффективный способ дистанционно кормить вашего кота всегда свежей едой.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d18/e46f720f675a/picture_for_the_app_1.jpg?extra=MRrFvvbWMVhYGoyq3ULiEf_6qd5Lpq6PfV1BhtUAFBF_5QJePwkvFwbMKFOXAEzJOOFT99zi0FiwZABFJ2_cyiGntOKyqMCeWdhZJpXnQy0T6zEJBg2AZ7-9-essyIo3w6ZAV-OmnrDdqbceOErO8xeU"));
        spots.add(new Spot("Фронтэнд разработчик\n" +
                "Сайт медицинского стартапа\n", "Проект специализирующийся на разработке скрининг–тестов по определению рака на ранних стадиях.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d17/ec87fd552a99/picture_for_the_app_2.jpg?extra=QXl-RBZlI7tE-23ekbj23g6TLDjBKqEYtoaVXI9LDorCjARpm4byCJQcFDFt05gsNYhsvmsVLFn1U5N6tbRp5vOyVoiGQJ8olR__xTPXUK94mP4BPttmno-MGrKIcv7u3XCI3zf_hpV6UGoeh-vWvDsG"));
        spots.add(new Spot("Бэкэнд разработчик\n" +
                "Приложение для заказа такси\n", "На текущий момент признан одним из главных конкурентов Uber на Ближнем Востоке.",
                "https://psv4.userapi.com/c848024/u211160486/docs/d5/8cae4790dbfc/picture_for_the_app_3.jpg?extra=viW0LL6vJjXZRLwYQPmVLquAYfa4tWVI1GB7s8qxZFC2zSSEoyF1_vMDluF_ckSaI70WmhEGIJ1VnsJrJCwhynEOL5RqbL0N6JVeHV-PhLN6ikm69p0zPzWQY-WVQuLrTNoriGcgEWlKoO8G7jf9AyO9"));
        return spots;
    }

}
