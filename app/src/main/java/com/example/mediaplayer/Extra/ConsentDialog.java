package com.example.mediaplayer.Extra;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.example.mediaplayer.FilesListActivity;
import com.example.mediaplayer.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Locale;

public class ConsentDialog extends DialogFragment {

    private int mode;
    private String name;
    private String path;
    private List<Uri> mediaUris;

    private AppCompatButton acceptB;
    private Button cancelB;

    private LinearLayout deleteLayout;
    private LinearLayout renameLayout;

    private TextView titleText;
    private TextView contentText;
    private EditText editText;

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
        registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == FilesListActivity.RESULT_OK) {
                Toast.makeText(requireContext(), "File deleted successfully!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(requireContext(), "Delete permission denied", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

    public ConsentDialog(int mode, List<Uri> uri, String name, String path) {
        this.mode = mode;
        this.mediaUris = uri;
        this.name = name;
        this.path = path;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_consent, container, false);

        deleteLayout = view.findViewById(R.id.delete_layout);
        renameLayout = view.findViewById(R.id.rename_layout);
        titleText = view.findViewById(R.id.titleText);
        contentText = view.findViewById(R.id.filesText);
        editText = view.findViewById(R.id.editText);
        acceptB = view.findViewById(R.id.accept_button);
        cancelB = view.findViewById(R.id.decline_button);

        if (mode == 1 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            String title = mediaUris.size() > 1 ? "Delete these files permanently?" : "Delete this file permanently?";
            String file = mediaUris.size() == 1 ? name : mediaUris.size() + " files";
            titleText.setText(title);
            contentText.setText(file);
            renameLayout.setVisibility(View.GONE);
            deleteLayout.setVisibility(View.VISIBLE);
        }
        else if (mode == 2) {
            String title = "Rename this file?";
            titleText.setText(title);
            editText.setText(getFileNameWithoutExtension(name));
            deleteLayout.setVisibility(View.GONE);
            renameLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (mediaUris.contains(null)) {
            Toast.makeText(requireContext(), "Invalid media URI, Refresh this List", Toast.LENGTH_LONG).show();
            dismiss();
        }

        if (mode == 1) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                view.setVisibility(View.VISIBLE);

                acceptB.setOnClickListener(v -> {
                    deleteMediaFiles_A10B();
                    dismiss();
                });

                cancelB.setOnClickListener(v -> {
                    Toast.makeText(requireContext(), "Delete permission denied", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
            else {
                // Android 11A -> hide UI and launch consent
                view.setVisibility(View.GONE);
                deleteMediaFiles_A11A();
            }
        }

        else {
            acceptB.setOnClickListener(v -> {
                dismiss();
            });

            cancelB.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Rename permission denied", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void deleteMediaFiles_A10B() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            int totalDel = 0;
            for (Uri uri : mediaUris) {
                try {
                    int deleted = contentResolver.delete(uri, null, null);
                    if (deleted > 0) totalDel++;
                }
                catch (SecurityException e) {
                    Toast.makeText(requireContext(), "Missing permission to delete this file", Toast.LENGTH_LONG).show();
                }
            }
            if (totalDel > 0) {
                Toast.makeText(requireContext(), "Files deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete files.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Toast.makeText(requireContext(), "Error deleting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("deleteerror", "deleteMediaFile: " + e.getMessage());
        }
    }

    public void deleteMediaFiles_A11A() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, mediaUris);
                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
                deleteLauncher.launch(intentSenderRequest);
            }
        }
        catch (Exception e) {
            Toast.makeText(requireContext(), "Error deleting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("deleteerror", "deleteMediaFile: " + e.getMessage());
        }
    }

    public String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(lastDot);
        } else {
            return "";
        }
    }

    public String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(0, lastDot);
        } else {
            return fileName;
        }
    }

    private void performDelete(Uri uriToDelete) {
        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            int deletedRows = contentResolver.delete(uriToDelete, null, null);
            if (deletedRows > 0) {
                Toast.makeText(requireContext(), "File deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Deletion failed after permission: File not found or other issue.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error re-attempting delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
