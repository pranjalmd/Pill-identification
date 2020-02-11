package edu.asu.cc.medicare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PillRecyclerViewAdapter extends RecyclerView.Adapter<PillRecyclerViewAdapter.ViewHolder> {
    private List<PillPrescription> mData;
    private LayoutInflater mInflator;
    public PillAdapterListener listener;

    PillRecyclerViewAdapter(Context context, List<PillPrescription> data, PillAdapterListener listener) {
        this.mInflator = LayoutInflater.from(context);
        this.mData = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflator.inflate(R.layout.recyclerview_patient_prescription_pill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pill_name.setText(mData.get(position).proprietaryName);
        holder.pill_ndc11_code.setText(mData.get(position).ndc11Code);
        holder.morning_count.setText(mData.get(position).morning);
        holder.noon_count.setText(mData.get(position).noon);
        holder.evening_count.setText(mData.get(position).evening);
        holder.bedtime_count.setText(mData.get(position).bedtime);
        holder.tv_refill.setText("Refill: "+ mData.get(position).refillDate);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_prescription_pill_proprietary_name)
        TextView pill_name;

        @BindView(R.id.tv_prescription_pill_ndc11code)
        TextView pill_ndc11_code;

        @BindView(R.id.tv_pill_schedule_morning)
        TextView morning_count;

        @BindView(R.id.tv_pill_schedule_noon)
        TextView noon_count;

        @BindView(R.id.tv_pill_schedule_evening)
        TextView evening_count;

        @BindView(R.id.tv_pill_schedule_bedtime)
        TextView bedtime_count;

        @BindView(R.id.tv_refill)
        TextView tv_refill;

        @BindView(R.id.bt_pill_reminder)
        Button bt_pill_reminder;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            bt_pill_reminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.remindOnClick(v, getAdapterPosition());
                }
            });
        }
    }

    public interface PillAdapterListener {
        void remindOnClick(View v, int position);
    }
}
