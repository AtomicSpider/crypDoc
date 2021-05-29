package sanatoverhere.crypdoc.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import sanatoverhere.crypdoc.R;
import sanatoverhere.crypdoc.adapters.EntryAdapter;
import sanatoverhere.crypdoc.common.Consts;
import sanatoverhere.crypdoc.database.CryptoDatabase;
import sanatoverhere.crypdoc.database.Entry;
import sanatoverhere.crypdoc.database.EntryDao;

import static sanatoverhere.crypdoc.common.Consts.DB_NAME;
import static sanatoverhere.crypdoc.common.Consts.MAX_IMG_SIZE;

public class MainActivity extends AppCompatActivity {

    final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.fab)
    FloatingActionsMenu fab;

    @BindView(R.id.fab_text)
    FloatingActionButton fab_text;

    @BindView(R.id.fab_gallery)
    FloatingActionButton fab_gallery;

    @BindView(R.id.text_count)
    TextView text_count;

    @BindView(R.id.image_count)
    TextView image_count;

    @BindView(R.id.storage)
    TextView storage;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;

    LayoutInflater layoutInflater;
    CryptoDatabase db;
    EntryDao entryDao;

    EntryAdapter entryAdapter;
    String DB_PATH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db = CryptoDatabase.getInstance(this);
        entryDao = db.entryDao();
        entryDao.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
        DB_PATH = getDatabasePath(DB_NAME).getAbsolutePath();

        setListeners();

//        Entry entry = new Entry("title1", "file.jpg", "image");
//        entry.setDate_created(new Date());
//
//        long entry_id = entryDao.insertEntry(entry);
//
//        Log.d(TAG, "Entry inserted: " + entry_id);
//

        setRecyclerView();
        refreshEntries();

        //ToDo: Get all
    }

    private void setRecyclerView() {
        entryAdapter = new EntryAdapter(this, new ArrayList<>());
        recycler_view.setItemAnimator(new DefaultItemAnimator());
        recycler_view.setAdapter(entryAdapter);
    }

    private void setListeners() {
        fab_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                createOrEditEntry("text", null, null, null, null);
            }
        });

        fab_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.collapse();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Consts.SELECT_PICTURE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Consts.SELECT_PICTURE_CODE) {

            Uri selectedImageUri = data.getData();
            createOrEditEntry("image", null, null, null, selectedImageUri);
        }

    }


    private void refreshEntries() {
        //ToDo Debug
        for (String file : getFilesDir().list()) {
            Log.d(TAG, "File: " + file + "      Size: " + file.length() + "B");
        }

        List<Entry> ents = entryDao.getEntryList();
        for (Entry entry : ents) {
            Log.d(TAG, "Entry: " + entry.toString());
        }

        text_count.setText(String.valueOf(entryDao.getEntryCount("text")));
        image_count.setText(String.valueOf(entryDao.getEntryCount("image")));

        try {
            long files_size = getRemainingStorage(getFilesDir());
            long db_size = getRemainingStorage(new File(DB_PATH));
            long total_used_space = files_size + db_size;
            storage.setText(total_used_space / Consts.NUM_BYTES_MB + " MB");
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "ERROR: " + e.toString(), Toast.LENGTH_SHORT).show();
            storage.setText("ERR");
        }
        entryAdapter.setData(entryDao.getEntryList());
    }

    private long getRemainingStorage(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                } else
                    size += getRemainingStorage(file);
            }
        } else if (dir.isFile()) {
            size += dir.length();
        }
        return size;
    }

    public void createOrEditEntry(String type, Entry entry, String text, Bitmap bmp, Uri selectedImageUri) {
        boolean isUpdate = entry != null;
        if (type.equals("text")) {
            View view = layoutInflater.inflate(R.layout.dialog_edit_text, null);

            String POS_BTN_TEXT = "SAVE";

            if (isUpdate) {
                EditText titleView = view.findViewById(R.id.title);
                EditText textView = view.findViewById(R.id.text);
                titleView.setText(entry.getTitle());
                textView.setText(text);
                POS_BTN_TEXT = "UPDATE";
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                    .setView(view)
                    .setCancelable(true)
                    .setBackgroundInsetEnd(5)
                    .setBackgroundInsetStart(5)
                    .setBackgroundInsetBottom(5)
                    .setPositiveButton(POS_BTN_TEXT, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Window view = ((AlertDialog) dialog).getWindow();
                            EditText titleView = view.findViewById(R.id.title);
                            EditText textView = view.findViewById(R.id.text);

                            String title = titleView.getText().toString();
                            String text = textView.getText().toString();

                            if (TextUtils.isEmpty(title)) {
                                Toast.makeText(MainActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String filename = null;
                            if (isUpdate)
                                filename = entry.getFilename();
                            else
                                filename = UUID.randomUUID().toString();

                            try (FileOutputStream fos = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE)) {
                                fos.write(text.getBytes());
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Could not write file", Toast.LENGTH_SHORT).show();
                                return;
                            }


                            if (isUpdate) {
                                entry.setTitle(title);
                                entry.setDate_modified(new Date());
                                entryDao.updateEntry(entry);
                                Log.d(TAG, "Updated");
                            } else {
                                Entry entry = new Entry(title, filename, "text", new Date(), new Date());
                                long entry_id = entryDao.insertEntry(entry);
                                Log.d(TAG, "Inserted with ID: " + entry_id);
                            }

                            refreshEntries();
                        }
                    });
            if (isUpdate) {
                builder.setNegativeButton("SHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Window view = ((AlertDialog) dialog).getWindow();
                        EditText titleView = view.findViewById(R.id.title);
                        EditText textView = view.findViewById(R.id.text);

                        String title = titleView.getText().toString();
                        String text = textView.getText().toString();

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share contents of: " + title);
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(intent, null));
                    }
                })
                        .setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    showDeleteDialog(entry);
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Could not delete, ERROR: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            builder.show();
        } else {
            View view = layoutInflater.inflate(R.layout.dialog_add_image, null);

            String POS_BTN_TEXT = "SAVE";
            AppCompatImageView imgView = view.findViewById(R.id.img);

            if (isUpdate) {
                EditText titleView = view.findViewById(R.id.title);
                titleView.setText(entry.getTitle());
                imgView.setImageBitmap(bmp);
                POS_BTN_TEXT = "UPDATE";
            } else {
                if (null != selectedImageUri) {
                    imgView.setImageURI(selectedImageUri);
                    imgView.setTag(selectedImageUri);
                } else {
                    Toast.makeText(MainActivity.this, "Image could not be selected", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                    .setView(view)
                    .setCancelable(true)
                    .setBackgroundInsetEnd(5)
                    .setBackgroundInsetStart(5)
                    .setBackgroundInsetBottom(5)
                    .setPositiveButton(POS_BTN_TEXT, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Window view = ((AlertDialog) dialog).getWindow();
                            EditText titleView = view.findViewById(R.id.title);
                            AppCompatImageView imgView = view.findViewById(R.id.img);

                            String title = titleView.getText().toString();

                            if (TextUtils.isEmpty(title)) {
                                Toast.makeText(MainActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String filename = null;
                            if (isUpdate)
                                filename = entry.getFilename();
                            else
                                filename = UUID.randomUUID().toString();

                            if (isUpdate) {
                                entry.setTitle(title);
                                entry.setDate_modified(new Date());
                                entryDao.updateEntry(entry);
                                Log.d(TAG, "Updated");
                            } else {
                                Uri selected_uri = (Uri) imgView.getTag();
                                Bitmap bitmap = null;
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), selected_uri);
                                } catch (IOException e) {
                                    Toast.makeText(MainActivity.this, "Could load image from uri", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try (FileOutputStream fos = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE)) {

                                    int outWidth;
                                    int outHeight;
                                    int inWidth = bitmap.getWidth();
                                    int inHeight = bitmap.getHeight();
                                    if (inWidth > inHeight) {
                                        outWidth = MAX_IMG_SIZE;
                                        outHeight = (inHeight * MAX_IMG_SIZE) / inWidth;
                                    } else {
                                        outHeight = MAX_IMG_SIZE;
                                        outWidth = (inWidth * MAX_IMG_SIZE) / inHeight;
                                    }

                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
                                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                                } catch (IOException e) {
                                    Log.d(TAG, e.toString());
                                    Toast.makeText(MainActivity.this, "Could not save the image", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Entry entry = new Entry(title, filename, "image", new Date(), new Date());
                                long entry_id = entryDao.insertEntry(entry);
                                Log.d(TAG, "Inserted with ID: " + entry_id);
                            }
                            refreshEntries();
                        }
                    });
            if (isUpdate) {
                builder.setNegativeButton("SHARE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Window view = ((AlertDialog) dialog).getWindow();
                        EditText titleView = view.findViewById(R.id.title);
                        String title = titleView.getText().toString();
                        AppCompatImageView imgView = view.findViewById(R.id.img);

                        Uri imgUri = getUriFromBitmap(((BitmapDrawable) imgView.getDrawable()).getBitmap());

                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share contents of: " + title);
                        intent.putExtra(Intent.EXTRA_STREAM, imgUri);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, null));
                    }
                })
                        .setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    showDeleteDialog(entry);
                                } catch (Exception e) {
                                    Toast.makeText(MainActivity.this, "Could not delete, ERROR: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            builder.show();
        }
    }

    private void showDeleteDialog(Entry entry) {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete " + entry.getTitle() + " ?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entryDao.deleteEntry(entry);
                        refreshEntries();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        File imagesFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "sanatoverhere.crypdoc.fileprovider", file);
            return uri;
        } catch (IOException e) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
            return null;
        }
    }
}
