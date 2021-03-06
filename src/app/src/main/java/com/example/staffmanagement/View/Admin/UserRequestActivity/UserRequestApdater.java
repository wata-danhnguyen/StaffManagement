package com.example.staffmanagement.View.Admin.UserRequestActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.staffmanagement.Model.Entity.Request;
import com.example.staffmanagement.Model.Entity.StateRequest;
import com.example.staffmanagement.R;
import com.example.staffmanagement.View.Admin.DetailRequestUser.DetailRequestUserActivity;
import com.example.staffmanagement.View.Ultils.CheckNetwork;
import com.example.staffmanagement.View.Ultils.Constant;
import com.example.staffmanagement.View.Ultils.GeneralFunc;
import com.example.staffmanagement.ViewModel.Admin.UserRequestViewModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UserRequestApdater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private UserRequestViewModel mViewModel;
    private final int ITEM_VIEW_TYPE = 1;

    public UserRequestApdater(Context context, UserRequestViewModel mViewModel) {
        WeakReference<Context> weak = new WeakReference<>(context);
        this.mContext = weak.get();
        this.mViewModel = mViewModel;
    }


    @Override
    public int getItemViewType(int position) {
        int LOADING_VIEW_TYPE = 2;
        return mViewModel.getRequestList().get(position) == null ? LOADING_VIEW_TYPE : ITEM_VIEW_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_VIEW_TYPE) {
            view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.item_user_request, parent, false);
            return new ViewHolder(view);
        } else {
            view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.view_load_more, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LoadingViewHolder) {
            return;
        }
        final ViewHolder viewHolder = (ViewHolder) holder;
        final int idState = mViewModel.getRequestList().get(position).getStateRequest().getId();

        viewHolder.setTxtName(mViewModel.getRequestList().get(position).getNameOfUser());
        viewHolder.setTxtTitle(mViewModel.getRequestList().get(position).getTitle());
        viewHolder.setTxtDateTime(GeneralFunc.convertMilliSecToDateString(mViewModel.getRequestList().get(position).getDateTime()));
        viewHolder.getTxtRequestState().setText(mViewModel.getRequestList().get(position).getStateRequest().getName());

        String text = (String) viewHolder.getTxtRequestState().getText();
        if ("Waiting".equals(text)) {
            viewHolder.getTxtRequestState().setTextColor(Color.parseColor("#ED7B00"));
        } else if ("Decline".equals(text)) {
            viewHolder.getTxtRequestState().setTextColor(Color.parseColor("#CF0018"));
        } else if ("Accept".equals(text)) {
            viewHolder.getTxtRequestState().setTextColor(Color.parseColor("#3ABF00"));
        }
        viewHolder.getView().setOnClickListener(view -> {
            Intent intent = new Intent(mContext, DetailRequestUserActivity.class);
            intent.putExtra(Constant.REQUEST_DATA_INTENT, mViewModel.getRequestList().get(position));
            intent.putExtra(Constant.STATE_NAME_INTENT, String.valueOf(viewHolder.getTxtRequestState().getText()));
            ((Activity) mContext).startActivityForResult(intent, 123);
        });

        viewHolder.getImvDecline().setOnClickListener(view -> {
            if (CheckNetwork.checkInternetConnection(mContext)) {
                viewHolder.getTxtRequestState().setText("Decline");
                viewHolder.getTxtRequestState().setTextColor(mContext.getResources().getColor(R.color.colorDecline));
                mViewModel.getRequestList().get(position).setStateRequest(new StateRequest(3,"Decline"));
                mViewModel.updateRequest( mViewModel.getRequestList().get(position));
                notifyItemChanged(position);
            }
        });

        viewHolder.getImvAccept().setOnClickListener(view -> {
            if (CheckNetwork.checkInternetConnection(mContext)) {
                viewHolder.getTxtRequestState().setText("Accept");
                viewHolder.getTxtRequestState().setTextColor(mContext.getResources().getColor(R.color.colorAccept));
                mViewModel.getRequestList().get(position).setStateRequest(new StateRequest(2,"Accept"));
                mViewModel.updateRequest( mViewModel.getRequestList().get(position));
                notifyItemChanged(position);
            }
        });
    }

    public void setData(List<Request> listLoadMore) {
        List<Request> newList = new ArrayList<>();
        newList.addAll(mViewModel.getRequestList());
        newList.addAll(listLoadMore);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserRequestDiffUtilCallback(mViewModel.getRequestList(),newList));
        diffResult.dispatchUpdatesTo(this);
        mViewModel.getRequestList().clear();
        mViewModel.getRequestList().addAll(newList);
    }

    @Override
    public int getItemCount() {
        return mViewModel.getRequestList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtTitle, txtDateTime;
        private TextView txtRequestState;
        private ImageView imvDecline, imvAccept;
        private View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            txtName = itemView.findViewById(R.id.textViewStaffName);
            txtTitle = itemView.findViewById(R.id.textViewRequestName);
            txtDateTime = itemView.findViewById(R.id.textViewRequestDateTime);
            txtRequestState = itemView.findViewById(R.id.textViewRequestState);
            imvAccept = view.findViewById(R.id.imvAccept);
            imvDecline = view.findViewById(R.id.imvDecline);
        }


        public void setTxtName(String txtName) {
            this.txtName.setText(txtName);
        }

        public void setTxtTitle(String txtTitle) {
            this.txtTitle.setText(txtTitle);
        }

        public void setTxtDateTime(String txtDateTime) {
            this.txtDateTime.setText(txtDateTime);
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public TextView getTxtRequestState() {
            return txtRequestState;
        }

        public ImageView getImvDecline() {
            return imvDecline;
        }

        public ImageView getImvAccept() {
            return imvAccept;
        }
    }

}
