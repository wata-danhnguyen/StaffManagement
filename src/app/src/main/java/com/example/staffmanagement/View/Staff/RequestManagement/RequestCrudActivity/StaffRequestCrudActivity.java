package com.example.staffmanagement.View.Staff.RequestManagement.RequestCrudActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.example.staffmanagement.Model.Entity.Request;
import com.example.staffmanagement.Model.Entity.StateRequest;
import com.example.staffmanagement.R;
import com.example.staffmanagement.View.Data.UserSingleTon;
import com.example.staffmanagement.View.Admin.SendNotificationActivity.Service.Broadcast;
import com.example.staffmanagement.View.Staff.RequestManagement.RequestActivity.StaffRequestActivity;
import com.example.staffmanagement.View.Ultils.CheckNetwork;
import com.example.staffmanagement.View.Ultils.Constant;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.example.staffmanagement.View.Ultils.NetworkState;
import com.example.staffmanagement.ViewModel.Staff.ScreenAddRequestViewModel;

import java.util.Date;

public class StaffRequestCrudActivity extends AppCompatActivity {

    private CheckNetwork mCheckNetwork;
    private EditText edtContent, edtTitle,edtTime,edtState;
    private Toolbar toolbar;
    private String action;
    private Request mRequest;
    private Broadcast mBroadcast;
    private ScreenAddRequestViewModel mViewModel;
    private ProgressDialog mProgressDialog;
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CheckNetwork.checkInternetConnection(StaffRequestCrudActivity.this)) {
                runOnUiThread(() -> {
                    int id = getIntent().getIntExtra("IdRequest", 0);
                    if (id != 0 && CheckNetwork.checkInternetConnection(StaffRequestCrudActivity.this))
                        mViewModel.getRequestById(id);
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.StaffAppTheme);
        mViewModel = ViewModelProviders.of(this).get(ScreenAddRequestViewModel.class);
        setContentView(R.layout.activity_request_crud);
        mapping();
        getDataIntentEdit();
        setupToolBar();
        eventRegister();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBroadcast = new Broadcast();
        IntentFilter filter = new IntentFilter("Notification");
        registerReceiver(mBroadcast, filter);

        mCheckNetwork = new CheckNetwork(this);
        mCheckNetwork.registerCheckingNetwork();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcast);
        mCheckNetwork.unRegisterCheckingNetwork();
        unregisterReceiver(mWifiReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_crud_request_non_admin, menu);
        checkEditAction(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_menu_apply_request_crud_non_admin:
                if (CheckNetwork.checkInternetConnection(StaffRequestCrudActivity.this)) {
                    if (action.equals(StaffRequestActivity.ACTION_ADD_NEW_REQUEST)) {
                        mProgressDialog = new ProgressDialog(StaffRequestCrudActivity.this);
                        mProgressDialog.setMessage("Checking...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                        mViewModel.checkRuleForAddRequest(UserSingleTon.getInstance().getUser().getId());
                    } else {
                        mViewModel.getError().postValue(ScreenAddRequestViewModel.ERROR_ADD_REQUEST.PASS);
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mapping() {
        edtTitle = findViewById(R.id.editText_title_request_non_admin);
        edtContent = findViewById(R.id.editText_content_request_non_admin);
        toolbar = findViewById(R.id.toolbar);
        edtTime = findViewById(R.id.edit_text_time_create);
        edtState = findViewById(R.id.editText_state_request_non_admin);
    }

    private void eventRegister() {
        GeneralFunc.setHideKeyboardOnTouch(this, findViewById(R.id.AddRequest));
        mViewModel.getError().observe(this, error_add_request -> {
            switch (error_add_request) {
                case OVER_LIMIT:
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(StaffRequestCrudActivity.this);
                    builder.setTitle("Warning");
                    builder.setMessage("Your number of request over limit, are you sure to add new request, it can be declined");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Request request = getInputRequest();
                            if (request != null) {
                                request.setStateRequest(new StateRequest(3, "Decline"));
                                Intent data = new Intent();
                                data.putExtra(Constant.REQUEST_DATA_INTENT, request);
                                setResult(RESULT_OK, data);
                                finish();
                            }
                        }
                    });

                    builder.setNegativeButton("CANCEL", (dialogInterface, i) -> {

                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    break;

                case PASS:
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    Request request = getInputRequest();
                    if (request != null) {
                        Intent data = new Intent();
                        data.putExtra(Constant.REQUEST_DATA_INTENT, request);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                    break;
                case NETWORK_ERROR:
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(StaffRequestCrudActivity.this);
                    builder1.setTitle("NETWORK ERROR");
                    builder1.setMessage("Cannot add new request");
                    builder1.setNegativeButton("OK", (dialogInterface, i) -> {

                    });
                    AlertDialog alertDialog1 = builder1.create();
                    alertDialog1.show();
                    break;

            }
        });

        mViewModel.getRequestLD().observe(this, request -> {
            if (request != null) {
                mRequest = request;
                setDataOnView();
            }
        });
    }

    private void setupToolBar() {
        setSupportActionBar(toolbar);
        if (action.equals(StaffRequestActivity.ACTION_ADD_NEW_REQUEST)) {
            toolbar.setTitle("Add new request");
            edtState.setText("Waiting");
            edtTime.setText(GeneralFunc.getCurrentDateTime());

        } else if (action.equals(StaffRequestActivity.ACTION_VIEW_REQUEST)) {
            toolbar.setTitle("View request");
        } else {
            toolbar.setTitle("Edit request");
            setDataOnView();
        }

        toolbar.setNavigationOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void checkEditAction(Menu menu) {
        menu.findItem(R.id.option_menu_apply_request_crud_non_admin).setEnabled(true);
        menu.findItem(R.id.option_menu_apply_request_crud_non_admin).getIcon().setAlpha(200);
        if ((action.equals(StaffRequestActivity.ACTION_EDIT_REQUEST) && mRequest != null && mRequest.getStateRequest().getId() != 1) ||
                action.equals(StaffRequestActivity.ACTION_VIEW_REQUEST)) {
            menu.findItem(R.id.option_menu_apply_request_crud_non_admin).setEnabled(false);
            menu.findItem(R.id.option_menu_apply_request_crud_non_admin).getIcon().setAlpha(0);
        }
    }

    private Request getInputRequest() {
        String title = edtTitle.getText().toString();
        String content = edtContent.getText().toString();
        if (TextUtils.isEmpty(title)) {
            showMessage("Title is empty");
            edtTitle.requestFocus();
            return null;
        }
        if (TextUtils.isEmpty(content)) {
            showMessage("Content is empty");
            edtContent.requestFocus();
            return null;
        }

        Date date = new Date();
        if(!action.equals(StaffRequestActivity.ACTION_ADD_NEW_REQUEST))
            date = new Date(mRequest.getDateTime());
        Request request = new Request(0, UserSingleTon.getInstance().getUser().getId(), title, content, date.getTime(), new StateRequest(1, "Waiting"), UserSingleTon.getInstance().getUser().getFullName());
        if (action.equals(StaffRequestActivity.ACTION_EDIT_REQUEST))
            request.setId(mRequest.getId());

        return request;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setDataOnView() {
        edtTitle.setText(mRequest.getTitle());
        edtContent.setText(mRequest.getContent());
        edtTime.setText(GeneralFunc.convertMilliSecToDateString(mRequest.getDateTime()));
        switch (action){
            case StaffRequestActivity.ACTION_VIEW_REQUEST:
            case StaffRequestActivity.ACTION_EDIT_REQUEST:
                edtState.setText(mRequest.getStateRequest().getName());
                break;
            case StaffRequestActivity.ACTION_ADD_NEW_REQUEST :
                edtState.setText("Waiting");
                break;

        }
        checkStateRequest();
    }

    private void checkStateRequest() {
        if ((mRequest != null && mRequest.getStateRequest().getId() != 1) || action.equals(StaffRequestActivity.ACTION_VIEW_REQUEST)) {
            edtContent.setFocusable(false);
            edtTitle.setFocusable(false);
        }
    }

    private void getDataIntentEdit() {
        action = getIntent().getAction();
        if (action.equals(StaffRequestActivity.ACTION_EDIT_REQUEST))
            mRequest = (Request) getIntent().getSerializableExtra(Constant.REQUEST_DATA_INTENT);
        else if (action.equals(StaffRequestActivity.ACTION_VIEW_REQUEST)) {
            int id = getIntent().getIntExtra("IdRequest", 0);
            if (id != 0 && NetworkState.isNetworkConnected) {
                mViewModel.getRequestById(id);
            } else {
                edtTitle.setText("No internet to load data");
                edtContent.setText("Connect to network to load data again");
                edtTime.setText("");
                checkStateRequest();
            }
        }
    }

}