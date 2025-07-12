package com.example.mediaplayer.Extra;

import android.app.Dialog;
import android.graphics.Color;
import android.media.browse.MediaBrowser;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.media3.common.MediaItem;

import com.example.mediaplayer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MyBottomSheet extends BottomSheetDialogFragment {

    public interface BottomSheetDialogListeners {
        void onPlayCLickListener(int position);
        void onInfoClickListener(int position);
        void onRenameClickListener(int position);
        void onDeleteClickListener(int position);
        void onShareClickListener(int position);
    }

    private BottomSheetDialogListeners listener;

    private int pos;
    private String name;

    private TextView title;
    private AppCompatButton playB;
    private AppCompatButton infoB;
    private AppCompatButton renameB;
    private AppCompatButton deleteB;
    private AppCompatButton shareB;


    public MyBottomSheet(int pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        title = view.findViewById(R.id.title);
        playB = view.findViewById(R.id.playB);
        infoB = view.findViewById(R.id.infoB);
        renameB = view.findViewById(R.id.renameB);
        deleteB = view.findViewById(R.id.deleteB);
        shareB = view.findViewById(R.id.shareB);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title.setText(name);

        playB.setOnClickListener(v -> {
            listener.onPlayCLickListener(pos);
            dismiss();
        });

        infoB.setOnClickListener(v -> {
            listener.onInfoClickListener(pos);
            dismiss();
        });

        renameB.setOnClickListener(v -> {
            listener.onRenameClickListener(pos);
            dismiss();
        });

        deleteB.setOnClickListener(v -> {
            listener.onDeleteClickListener(pos);
            dismiss();
        });

        shareB.setOnClickListener(v -> {
            listener.onShareClickListener(pos);
            dismiss();
        });
    }

    public void addBottomSheetDialogListeners(BottomSheetDialogListeners listener) {
        this.listener = listener;
    }
}
