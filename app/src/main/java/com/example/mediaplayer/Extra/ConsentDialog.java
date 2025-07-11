package com.example.mediaplayer.Extra;

import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.mediaplayer.FilesListActivity;
import com.example.mediaplayer.R;

public class ConsentDialog extends DialogFragment {

    private Uri mediaUri;

    private Uri lastAttemptedDeleteUri;

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
        registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == FilesListActivity.RESULT_OK) {
                if (mediaUri != null) {
                    performDelete(mediaUri);
                    mediaUri = null;
                }
                Toast.makeText(requireContext(), "Delete permission granted. Retrying...", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(requireContext(), "Delete permission denied by user.", Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });

    public ConsentDialog(Uri uri) {
        this.mediaUri = uri;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_consent, container, false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            view.setVisibility(View.VISIBLE);

            view.findViewById(R.id.accept_button).setOnClickListener(v -> {
//                performDelete(lastAttemptedDeleteUri); // Direct delete
                dismiss();
            });

            view.findViewById(R.id.decline_button).setOnClickListener(v -> dismiss());
        }
        else {
            // Android 10+ -> hide UI and launch consent
            view.setVisibility(View.GONE);
            deleteMediaFile(mediaUri);
        }

        return view;
    }

    public void deleteMediaFile(Uri mediaUri) {
        if (mediaUri == null) {
            Toast.makeText(requireContext(), "Invalid media URI.", Toast.LENGTH_SHORT).show();
            return;
        }

        this.mediaUri = mediaUri;

        ContentResolver contentResolver = requireContext().getContentResolver();
        try {
            // Attempt to delete the file using MediaStore API
            int deletedRows = contentResolver.delete(mediaUri, null, null);

            if (deletedRows > 0) {
                Toast.makeText(requireContext(), "File deleted successfully!", Toast.LENGTH_SHORT).show();
            }
            else {
                // This might happen if the file doesn't exist, or permission was implicitly denied on older APIs
                Toast.makeText(requireContext(), "Failed to delete file. It might not exist or permission was an issue.", Toast.LENGTH_LONG).show();
            }
        }
        catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e instanceof RecoverableSecurityException) {
                RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) e;
                IntentSender intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();

                IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                deleteLauncher.launch(intentSenderRequest);
            }
            else {
                // Other SecurityExceptions (e.g., missing READ_EXTERNAL_STORAGE for getting URI, or other issues)
                Toast.makeText(requireContext(), "Permission denied or other security error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e) {
            Toast.makeText(requireContext(), "Error deleting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("deleteerror", "deleteMediaFile: " + e.getMessage());
            e.printStackTrace();
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
