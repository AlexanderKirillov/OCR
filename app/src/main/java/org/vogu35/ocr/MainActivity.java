package org.vogu35.ocr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import org.vogu35.ocr.ui.main.AboutFragment;
import org.vogu35.ocr.ui.main.HomeFragment;
import org.vogu35.ocr.ui.main.SettingsFragment;

/**
 * Главное активити приложения.
 */
public class MainActivity extends AppCompatActivity {

    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;

    /**
     * Объект класса Utilities.
     */
    private Utilities utils = new Utilities(this);

    /**
     * Элемент интерфейса - Toolbar.
     */
    private Toolbar toolbar;

    /**
     * Элемент интерфейса - DrawerLayout.
     */
    private DrawerLayout mDrawer;

    /**
     * Элемент интерфейса - ActionBarDrawerToggle.
     */
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Элемент интерфейса - NavigationView.
     */
    private NavigationView nvDrawer;

    /**
     * Метод установки цветовой схемы приложения (темы).
     */
    public void updateTheme() {
        Menu drawerMenu = nvDrawer.getMenu();
        drawerMenu.findItem(R.id.homepage).setChecked(true);

        if (utils.getTheme(getApplicationContext()) == THEME_LIGHT) {
            setTheme(R.style.AppTheme_Light);

            drawerMenu.findItem(R.id.homepage).setIcon(R.drawable.homepage_icon_black);
            drawerMenu.findItem(R.id.settingspage).setIcon(R.drawable.settingspage_icon_black);
            drawerMenu.findItem(R.id.aboutpage).setIcon(R.drawable.aboutpage_icon_black);
            drawerMenu.findItem(R.id.exitpage).setIcon(R.drawable.exitpage_icon_black);
            drawerMenu.findItem(R.id.connectpage).setIcon(R.drawable.connectpage_icon_black);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        } else if (utils.getTheme(getApplicationContext()) == THEME_DARK) {
            setTheme(R.style.AppTheme_Dark);

            nvDrawer.setBackgroundColor(Color.parseColor("#2d2d2d"));
            nvDrawer.setItemTextColor(ColorStateList.valueOf(Color.WHITE));
            drawerMenu.findItem(R.id.homepage).setIcon(R.drawable.homepage_icon_white);
            drawerMenu.findItem(R.id.settingspage).setIcon(R.drawable.settingspage_icon_white);
            drawerMenu.findItem(R.id.aboutpage).setIcon(R.drawable.aboutpage_icon_white);
            drawerMenu.findItem(R.id.exitpage).setIcon(R.drawable.exitpage_icon_white);
            drawerMenu.findItem(R.id.connectpage).setIcon(R.drawable.connectpage_icon_white);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    /**
     * Метод, вызываемый при инициализации активити.
     * В процессе его работы настраиваем интерфейс (Navigation Drawer, ActionBar), устанавливаем цветовую схему приложения
     * и запускаем HomeFragment.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nvDrawer = findViewById(R.id.nvView);
        mDrawer = findViewById(R.id.drawer_layout);

        drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        mDrawer.addDrawerListener(drawerToggle);
        nvDrawer.setItemIconTintList(null);

        setupDrawerContent(nvDrawer);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, HomeFragment.newInstance())
                    .commitNow();
        }

        final SharedPreferences BD = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (BD.contains("OCR_APP_THEME")) {
            updateTheme();
        } else {
            BD.edit().putInt(getApplicationContext().getString(R.string.prefs_theme_key), THEME_LIGHT).apply();
            SharedPreferences.Editor BDEditor = BD.edit();
            BDEditor.putString("OCR_APP_THEME", "ID_184154");
            BDEditor.apply();
            updateTheme();
        }

    }

    /**
     * Метод, вызываемый при двойном нажатии на кнопку назад.
     * При двойном нажатии кнопки назад открывается\закрывается боковое меню.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    /**
     * Метод, вызываемый после onCreate().
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    /**
     * Метод, вызываемый при изменении конфигураии устройства (например, ориентации дисплея).
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Метод, реализующий инициализацию кнопки в ActionBar-е,
     * предназначенной для открытия бокового меню.
     */
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    /**
     * Метод, вызываемый для обработки нажатий пунктов меню.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Метод, вызываемый для настройки бокового меню.
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    /**
     * Метод, предназначенный для предопределения действий при нажатии на определенные пункты бокового меню.
     * При нажатии на пункт бокового меню - запускается соответствующий фрагмент или выполняется определенное действие.
     */
    public void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        int moveAction;
        Class fragmentClass;

        Menu menu = nvDrawer.getMenu();
        menu.findItem(R.id.homepage).setChecked(false);

        switch (menuItem.getItemId()) {
            case R.id.homepage:
                moveAction = 1;
                fragmentClass = HomeFragment.class;
                break;
            case R.id.settingspage:
                moveAction = 1;
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.aboutpage:
                moveAction = 1;
                fragmentClass = AboutFragment.class;
                break;
            case R.id.exitpage:
                moveAction = 0;
                fragmentClass = null;
                new AlertDialog.Builder(this)
                        .setTitle(R.string.exit_question)
                        .setMessage(R.string.exit_q_conf)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                moveTaskToBack(true);
                                MainActivity.super.onBackPressed();
                            }
                        }).create().show();
                break;
            case R.id.connectpage:
                moveAction = 0;
                fragmentClass = null;
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "nowenui@bk.ru", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OCR HELP");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.sending_message)));
                break;

            default:
                moveAction = 0;
                fragmentClass = HomeFragment.class;
        }

        if (moveAction == 1) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                utils.switchFragment(this, fragment);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        menuItem.setChecked(true);

        mDrawer.closeDrawers();
    }
}
