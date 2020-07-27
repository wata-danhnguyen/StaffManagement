package com.example.staffmanagement.View.Admin.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.staffmanagement.R;
import com.example.staffmanagement.View.Admin.MainAdminActivity.MainAdminActivity;
import com.example.staffmanagement.View.Admin.UserRequestActivity.UserRequestActivity;
import com.example.staffmanagement.View.Data.UserSingleTon;
import com.example.staffmanagement.View.Main.LogInActivity;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.example.staffmanagement.View.Ultils.ImageHandler;
import com.google.android.material.navigation.NavigationView;

public class AdminHomeActivity extends AppCompatActivity implements AdminHomeInterface {

    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView txtName,txtMail;
    private ImageView imgAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        mapping();
        eventRegister();
        setDrawerLayout();
    }

    private void mapping() {
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_drawer_admin);
        drawerLayout = findViewById(R.id.drawer_layout);
        txtName = navigationView.getHeaderView(0).findViewById(R.id.textViewName);
        txtMail = navigationView.getHeaderView(0).findViewById(R.id.textViewEmail);
        imgAvatar = navigationView.getHeaderView(0).findViewById(R.id.imageViewAvatar);
    }

    private void setDrawerLayout(){
        txtName.setText(UserSingleTon.getInstance().getUser().getFullName());
        txtMail.setText(UserSingleTon.getInstance().getUser().getEmail());
        ImageHandler.loadImageFromBytes(this,UserSingleTon.getInstance().getUser().getAvatar(),imgAvatar);
    }

    private void eventRegister(){
        setupToolBar();
        setOnItemDrawerClickListener();
    }

    private void setupToolBar(){
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_home_admin,menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void setOnItemDrawerClickListener(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch(item.getItemId()){
                    case R.id.item_menu_navigation_drawer_admin_home:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.item_menu_navigation_drawer_admin_request:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        intent = new Intent(AdminHomeActivity.this, UserRequestActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.item_menu_navigation_drawer_admin_user_list:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        intent = new Intent(AdminHomeActivity.this, MainAdminActivity.class);
                        startActivity(intent);
                        break;

                    case R.id.item_menu_navigation_drawer_admin_profile:
                        drawerLayout.closeDrawer(GravityCompat.START);
//                        intent = new Intent(AdminHomeActivity.this, .class);
//                        startActivity(intent);
                        break;
                    case R.id.item_menu_navigation_drawer_admin_log_out:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        GeneralFunc.logout(AdminHomeActivity.this, LogInActivity.class);
                        break;
                }
                return false;
            }
        });
    }
}