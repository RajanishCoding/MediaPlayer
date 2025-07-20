package com.example.mediaplayer.Extra;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.example.mediaplayer.FilesListActivity;
import com.example.mediaplayer.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsentDialog extends DialogFragment {

    private int mode;
    private String name;
    private List<Uri> mediaUris;

    private String mediaName;
    private String mediaExtension;
    private String newName;

    private Button acceptB;
    private Button cancelB;

    private LinearLayout deleteLayout;
    private LinearLayout renameLayout;
    private LinearLayout loadingLayout;
    private LinearLayout buttonsLayout;

    private TextView titleText;
    private TextView contentText;
    private EditText editText;

    private final ActivityResultLauncher<IntentSenderRequest> consentLauncher =
        registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == FilesListActivity.RESULT_OK) {
                if (mode == 1) {
                    Toast.makeText(requireContext(), "File Deleted successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else doRename_A11A();
            }
            else {
                if (mode == 1){ Toast.makeText(requireContext(), "Delete permission denied", Toast.LENGTH_SHORT).show();}
                else Toast.makeText(requireContext(), "Rename permission denied", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

    public ConsentDialog(int mode, List<Uri> uri, String name) {
        this.mode = mode;
        this.mediaUris = uri;
        this.name = name;
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
        loadingLayout = view.findViewById(R.id.loadingLayout);
        buttonsLayout = view.findViewById(R.id.buttonsLayout);
        titleText = view.findViewById(R.id.titleText);
        contentText = view.findViewById(R.id.filesText);
        editText = view.findViewById(R.id.editText);
        acceptB = view.findViewById(R.id.accept_button);
        cancelB = view.findViewById(R.id.decline_button);

        loadingLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.VISIBLE);

        if (mode == 1 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            String title = mediaUris.size() > 1 ? "Delete these files permanently?" : "Delete this file permanently?";
            String file = mediaUris.size() > 1 ? mediaUris.size() + " files" : name;
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
            Toast.makeText(requireContext(), "Invalid media, Refresh this List", Toast.LENGTH_LONG).show();
            dismiss();
        }

        if (mode == 1) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                view.setVisibility(View.VISIBLE);

                acceptB.setOnClickListener(v -> {
                    deleteMedia_A10B();
                });

                cancelB.setOnClickListener(v -> {
                    Toast.makeText(requireContext(), "Delete permission denied", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
            else {
                // Android 11A -> hide UI and launch consent
                view.setVisibility(View.GONE);
                deleteMedia_A11A();
            }
        }

        else {
            acceptB.setOnClickListener(v -> {
                mediaExtension = getFileExtension(name);
                mediaName = String.valueOf(editText.getText());
                newName = mediaName.concat(mediaExtension);

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    renameMedia_A10B();
                }
                else {
                    renameMedia_A11A();
                }
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
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85), // 85% screen width
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }


    public void deleteMedia_A10B() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        showLoading();
        executorService.execute(() -> {
            ContentResolver contentResolver = requireContext().getContentResolver();
            int totalDel = 0;
            for (Uri uri : mediaUris) {
                try {
                    int deleted = contentResolver.delete(uri, null, null);
                    if (deleted > 0) totalDel++;
                }
                catch (SecurityException e) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Missing permission to delete a file", Toast.LENGTH_LONG).show();
                    });
                }
            }
            if (totalDel > 0) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Files deleted successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            requireActivity().runOnUiThread(this::dismiss);
        });
    }

    private void deleteMedia_A11A() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, mediaUris);
                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
                consentLauncher.launch(intentSenderRequest);
            }
        }
        catch (Exception e) {
            Toast.makeText(requireContext(), "Error deleting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("deleteerror", "deleteMediaFile: " + e.getMessage());
            dismiss();
        }
    }


    private void renameMedia_A10B() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        showLoading();
        executorService.execute(() -> {
            ContentResolver contentResolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, newName);

            try {
                int r = contentResolver.update(mediaUris.get(0), values, null, null);

                requireActivity().runOnUiThread(() -> {
                    if (r > 0) {
                        Toast.makeText(requireContext(), "File Renamed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Rename failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch (Exception e) {
                Log.e("renamehello", "Error renaming file", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error Renaming file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            requireActivity().runOnUiThread(this::dismiss);
        });
    }

    private void renameMedia_A11A() {
        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PendingIntent pendingIntent = MediaStore.createWriteRequest(contentResolver, mediaUris);
                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
                consentLauncher.launch(intentSenderRequest);
            }
        }
        catch (Exception e) {
            Toast.makeText(requireContext(), "Error renaming file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("renameerror", "renameMediaFile: " + e.getMessage());
            dismiss();
        }
    }

    private void doRename_A11A() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            ContentResolver contentResolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, newName);

            try {
                int r = contentResolver.update(mediaUris.get(0), values, null, null);

                requireActivity().runOnUiThread(() -> {
                    if (r > 0) {
                        Toast.makeText(requireContext(), "File Renamed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Rename failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch (Exception e) {
                Log.e("renamehello", "Error renaming file", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error Renaming file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            requireActivity().runOnUiThread(this::dismiss);
        });
    }

    private void showLoading() {
        String text;
        if (mode == 1) text = mediaUris.size() > 1 ? "Deleting files..." : "Deleting file...";
        else text = "Renaming file...";

        setCancelable(false);
        titleText.setText(text);
        renameLayout.setVisibility(View.GONE);
        deleteLayout.setVisibility(View.GONE);
        buttonsLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(lastDot);
        } else {
            return "";
        }
    }

    private String getFileNameWithoutExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1) {
            return fileName.substring(0, lastDot);
        } else {
            return fileName;
        }
    }
}
