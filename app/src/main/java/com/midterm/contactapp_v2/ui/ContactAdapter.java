package com.midterm.contactapp_v2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.midterm.contactapp_v2.R;
import com.midterm.contactapp_v2.data.Contact;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contacts = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    public ContactAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact, listener);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateContacts(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView ivAvatar;
        private final TextView tvName;
        private final TextView tvPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }

        public void bind(Contact contact, OnItemClickListener listener) {
            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());

            if (contact.getAvatarPath() != null && !contact.getAvatarPath().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(new File(contact.getAvatarPath()))
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(contact));
        }
    }
}