package com.example.staffmanagement.Presenter.Staff;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.staffmanagement.Model.Database.DAL.RequestDbHandler;
import com.example.staffmanagement.Model.Database.DAL.StateRequestDbHandler;
import com.example.staffmanagement.Model.Database.Entity.Request;
import com.example.staffmanagement.Presenter.Staff.Background.MyMessage;
import com.example.staffmanagement.Presenter.Staff.Background.RequestActUiHandler;
import com.example.staffmanagement.View.Data.UserSingleTon;
import com.example.staffmanagement.View.Staff.RequestManagement.RequestActivity.StaffRequestInterface;
import com.example.staffmanagement.View.Ultils.ImageHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaffRequestPresenter {
    private RequestActUiHandler mHandler;
    private Context mContext;
    private StaffRequestInterface mInterface;

    public StaffRequestPresenter(Context mContext, StaffRequestInterface mInterface) {
        this.mContext = mContext;
        this.mInterface = mInterface;
        this.mHandler = new RequestActUiHandler(mInterface);
    }

    public void getAllRequestForUser(final int idUser){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_SHOW_PROGRESS_DIALOG));
                RequestDbHandler db = new RequestDbHandler(mContext);
                ArrayList<Request> list = db.getAllRequestForUser(idUser);
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_UPDATE_LIST,list));
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_DISMISS_PROGRESS_DIALOG));
                mHandler.removeCallbacks(null);
            }
        }).start();
    }

    public void getLimitListRequestForUser(final int idUser, final int offset, final int numRow, final Map<String, Object> criteria){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestDbHandler db = new RequestDbHandler(mContext);
                ArrayList<Request> list = db.getLimitListRequestForUser(idUser,offset,numRow,criteria);
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_ADD_LOAD_MORE_LIST,list));
                mHandler.removeCallbacks(null);
            }
        }).start();
    }

    public void addNewRequest(final Request request){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_SHOW_PROGRESS_DIALOG));
                RequestDbHandler db = new RequestDbHandler(mContext);
                Request req = db.insert(request);
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_DISMISS_PROGRESS_DIALOG));
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_ADD_NEW_REQUEST_SUCCESSFULLY,req));
                mHandler.removeCallbacks(null);
            }
        }).start();
    }

    public void updateRequest(final Request request){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_SHOW_PROGRESS_DIALOG));
                RequestDbHandler db = new RequestDbHandler(mContext);
                db.update(request);
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_UPDATE_REQUEST_SUCCESSFULLY,request));
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_DISMISS_PROGRESS_DIALOG));
                mHandler.removeCallbacks(null);
            }
        }).start();
    }

    public void findRequest(final int idUSer, final String title) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestDbHandler db = new RequestDbHandler(mContext);
                ArrayList<Request> list = db.findRequestByTitle(idUSer, title);
                mHandler.sendMessage(MyMessage.getMessage(RequestActUiHandler.MSG_UPDATE_LIST,list));
            }
        }).start();
    }

    public String getStateNameById(int idState) {
        StateRequestDbHandler db = new StateRequestDbHandler(mContext);
        return db.getStateNameById(idState);
    }

    public void loadHeaderDrawerNavigation(final Context context, final ImageView avatar, final TextView txtName, final TextView txtEmail) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageHandler.loadImageFromBytes(mContext, UserSingleTon.getInstance().getUser().getAvatar(), avatar);
                        txtName.setText(UserSingleTon.getInstance().getUser().getFullName());
                        txtEmail.setText(UserSingleTon.getInstance().getUser().getEmail());
                    }
                });
            }
        }).start();
    }
}