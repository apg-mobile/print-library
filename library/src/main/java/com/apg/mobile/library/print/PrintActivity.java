package com.apg.mobile.library.print;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PrintActivity extends AppCompatActivity {
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        file = (File) getIntent().getSerializableExtra("file");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
            if (printManager != null) {
                String jobName = "Document_" + file.getName();
                printManager.print(jobName, new PrintDocumentAdapter() {
                    @Override
                    public void onLayout(PrintAttributes printAttributes,
                                         PrintAttributes printAttributes1,
                                         CancellationSignal cancellationSignal,
                                         LayoutResultCallback callback,
                                         Bundle bundle) {

                        if (android.os.Build.VERSION.SDK_INT >=
                                android.os.Build.VERSION_CODES.KITKAT) {

                            if (cancellationSignal.isCanceled()) {
                                callback.onLayoutCancelled();
                                return;
                            }

                            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(file.getName())
                                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                    .build();

                            callback.onLayoutFinished(pdi, true);
                        }
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        finish();
                    }

                    @Override
                    public void onWrite(PageRange[] totalPages,
                                        ParcelFileDescriptor parcelFileDescriptor,
                                        CancellationSignal cancellationSignal,
                                        WriteResultCallback writeResultCallback) {
                        if (android.os.Build.VERSION.SDK_INT >=
                                android.os.Build.VERSION_CODES.KITKAT) {
                            InputStream input = null;
                            OutputStream output = null;

                            try {

                                input = new FileInputStream(file);
                                output = new FileOutputStream(
                                        parcelFileDescriptor.getFileDescriptor());

                                byte[] buf = new byte[1024];
                                int bytesRead;

                                while ((bytesRead = input.read(buf)) > 0) {
                                    output.write(buf, 0, bytesRead);
                                }

                                writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

                            } catch (Exception e) {
                                //Catch exception
                            } finally {
                                try {
                                    input.close();
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }, null);
            } else {
                showNotSupportDialog();
            }
        } else {
            showNotSupportDialog();
        }
    }

    private void showNotSupportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notice")
                .setMessage("Your device is not support." +
                        "\nDo you want to print document in other app?")
                .setCancelable(false)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                    }
                })
                .show();
    }

    public static Intent createIntent(Context context, File file) {
        Intent intent = new Intent(context, PrintActivity.class);
        intent.putExtra("file", file);
        return intent;
    }

    public static Intent createIntent(Context context, Uri uri) {
        return createIntent(context, new File(uri.getPath()));
    }
}