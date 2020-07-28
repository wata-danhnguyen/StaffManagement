package com.example.staffmanagement.View.Staff.RequestManagement.RequestActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.staffmanagement.Presenter.Staff.StaffRequestPresenter;
import com.example.staffmanagement.View.Staff.Home.StaffHomeActivity;
import com.example.staffmanagement.View.Ultils.Constant;
import com.example.staffmanagement.View.Data.UserSingleTon;
import com.example.staffmanagement.Model.Database.Entity.Request;
import com.example.staffmanagement.View.Main.LogInActivity;
import com.example.staffmanagement.View.Staff.RequestManagement.RequestCrudActivity.StaffRequestCrudActivity;
import com.example.staffmanagement.View.Staff.UserProfile.StaffUserProfileActivity;
import com.example.staffmanagement.R;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaffRequestActivity extends AppCompatActivity implements StaffRequestInterface {
    private Toolbar toolbar;
    private RecyclerView rvRequestList;
    private EditText edtSearch;
    private ProgressDialog mProgressDialog;
    private StaffRequestPresenter mPresenter;
    private StaffRequestListAdapter mAdapter;
    private ArrayList<Request> requestList;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView btnNavigateToAddNewRequest, imvAvatar;
    private Dialog mDialog;
    private TextView txtNameUser, txtEmailInDrawer;
    private final int mNumRow = Constant.NUM_ROW_ITEM_REQUEST_IN_STAFF;
    private ImageView imgClose;
    private static final int REQUEST_CODE_CREATE_REQUEST = 1;
    private static final int REQUEST_CODE_EDIT_REQUEST = 2;
    public static final String ACTION_ADD_NEW_REQUEST = "ACTION_ADD_NEW_REQUEST";
    public static final String ACTION_EDIT_REQUEST = "ACTION_EDIT_REQUEST";

    private boolean isLoading = false, isEndData = false, isShowMessageEndData = false;
    private String searchString = "";
    private Map<String, Object> mCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        mPresenter = new StaffRequestPresenter(this, this);
        packageDataFilter();
        mapping();
        eventRegister();
        setupToolbar();
        setUpListRequest();
        mPresenter.loadHeaderDrawerNavigation(this, imvAvatar, txtNameUser, txtEmailInDrawer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkProfileStateChange();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_staff_request_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_menu_item_staff_request_filter:
                showFilterDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_REQUEST && resultCode == RESULT_OK && data != null) {
            Request request = (Request) data.getSerializableExtra(Constant.REQUEST_DATA_INTENT);
            mPresenter.addNewRequest(request);
        } else if (requestCode == REQUEST_CODE_EDIT_REQUEST && resultCode == RESULT_OK && data != null) {
            Request request = (Request) data.getSerializableExtra(Constant.REQUEST_DATA_INTENT);
            mPresenter.updateRequest(request);
        }
    }

    private void mapping() {
        toolbar = findViewById(R.id.toolbarRequest);
        rvRequestList = findViewById(R.id.recyclerView_RequestList_NonAdmin);
        rvRequestList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        edtSearch = findViewById(R.id.editText_searchRequest_NonAdmin);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_drawer);
        btnNavigateToAddNewRequest = findViewById(R.id.imageView_navigate_to_add_new_request);
        imvAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.imageView_avatar_header_drawer_navigation);
        txtNameUser = mNavigationView.getHeaderView(0).findViewById(R.id.textView_name_header_drawer_navigation);
        txtEmailInDrawer = mNavigationView.getHeaderView(0).findViewById(R.id.textView_email_header_drawer_navigation);
        imgClose = mNavigationView.getHeaderView(0).findViewById(R.id.imageViewClose);
    }

    private void eventRegister() {
        onSearchChangeListener();
        setOnItemDrawerClickListener();
        btnNavigateToAddNewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToAddRequestActivity();
            }
        });

        onScrollRecyclerView();
    }

    private boolean checkProfileStateChange() {
        boolean b = GeneralFunc.checkChangeProfile(this);
        if (b == true) {
            mPresenter.loadHeaderDrawerNavigation(this, imvAvatar, txtNameUser, txtEmailInDrawer);
        }
        return false;
    }

    private void onSearchChangeListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isLoading = true;
                searchString = String.valueOf(charSequence);
                packageDataFilter();
//                mPresenter.findRequest(UserSingleTon.getInstance().getUser().getId(),
//                        String.valueOf(charSequence));
                requestList = new ArrayList<>();
                requestList.add(null);
                mAdapter = new StaffRequestListAdapter(StaffRequestActivity.this, requestList, mPresenter);
                rvRequestList.setAdapter(mAdapter);
                mAdapter.notifyItemInserted(requestList.size() - 1);
                mPresenter.getLimitListRequestForUser(UserSingleTon.getInstance().getUser().getId(), 0, mNumRow, mCriteria);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void packageDataFilter() {
        mCriteria = new HashMap<>();

        mCriteria.put(Constant.SEARCH_NAME_REQUEST_IN_STAFF, searchString);

    }

    private void onScrollRecyclerView() {
        rvRequestList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                loadMore(recyclerView, dy);
            }
        });
    }

    private void loadMore(RecyclerView recyclerView, int dy) {

        LinearLayoutManager ll = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastPosition = ll.findLastVisibleItemPosition();
        if (isLoading == false && lastPosition == requestList.size() - 1) {
            isLoading = true;
            requestList.add(null);
            mAdapter.notifyItemInserted(requestList.size() - 1);
            mPresenter.getLimitListRequestForUser(UserSingleTon.getInstance().getUser().getId(), requestList.size() - 1, mNumRow, mCriteria);
        }
    }

    private void navigateToAddRequestActivity() {
        Intent intent1 = new Intent(StaffRequestActivity.this, StaffRequestCrudActivity.class);
        intent1.setAction(ACTION_ADD_NEW_REQUEST);
        startActivityForResult(intent1, REQUEST_CODE_CREATE_REQUEST);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitle("Request List");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setUpListRequest() {
        isLoading = true;
        requestList = new ArrayList<>();
        mAdapter = new StaffRequestListAdapter(this, requestList, mPresenter);
        rvRequestList.setAdapter(mAdapter);

        // add loading
        requestList.add(null);
        mAdapter.notifyItemInserted(0);
        mPresenter.getLimitListRequestForUser(UserSingleTon.getInstance().getUser().getId(), 0, mNumRow, mCriteria);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void newProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(message);
    }

    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    @Override
    public void setMessageProgressDialog(String message) {
        mProgressDialog.setMessage(message);
    }

    @Override
    public void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void onGetListSuccessfully(ArrayList<Request> list) {
        requestList = new ArrayList<>();
        requestList.addAll(list);
        mAdapter = new StaffRequestListAdapter(this, requestList, mPresenter);
        rvRequestList.setAdapter(mAdapter);
    }

    @Override
    public void onAddNewRequestSuccessfully(Request newItem) {
        requestList.add(newItem);
        mAdapter.notifyDataSetChanged();
        showMessage("Add successfully");
    }

    @Override
    public void onUpdateRequestSuccessfully(Request item) {
        showMessage("Update successfully");
    }

    @Override
    public void onLoadMoreListSuccess(ArrayList<Request> list) {
        requestList.remove(requestList.size() - 1);
        mAdapter.notifyItemRemoved(requestList.size());
        isLoading = false;
        if (list == null || list.size() == 0) {
            if (isShowMessageEndData == false)
                showMessageEndData();
            return;
        }
        if (requestList == null)
            requestList = new ArrayList<>();
        requestList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    private void showMessageEndData() {
        isShowMessageEndData = true;
        showMessage("End data");

        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    isShowMessageEndData = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getRequestCodeEdit() {
        return REQUEST_CODE_EDIT_REQUEST;
    }

    private void showFilterDialog() {
        mDialog = new Dialog(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContentView(R.layout.dialog_staff_request_filter);
        Window window = mDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        TextView btnCancel = mDialog.findViewById(R.id.textView_CloseFilter);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    private void logout() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(StaffRequestActivity.this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constant.SHARED_PREFERENCE_IS_LOGIN);
        editor.remove(Constant.SHARED_PREFERENCE_ID_USER);
        editor.commit();

        startActivity(intent);
        finish();
    }

    private void setOnItemDrawerClickListener() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.item_menu_navigation_drawer_staff_home:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        intent = new Intent(StaffRequestActivity.this, StaffHomeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.item_menu_navigation_drawer_staff_request:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.item_menu_navigation_drawer_staff_profile:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        intent = new Intent(StaffRequestActivity.this, StaffUserProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.item_menu_navigation_drawer_staff_log_out:
                        logout();
                        break;
                }
                return false;
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        });
    }
}