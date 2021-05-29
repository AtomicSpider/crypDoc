package sanatoverhere.crypdoc.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import sanatoverhere.crypdoc.R;
import sanatoverhere.crypdoc.activities.MainActivity;
import sanatoverhere.crypdoc.database.Entry;

public class EntryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Entry> entries;

    private final String TAG = EntryAdapter.class.getSimpleName();

    public EntryAdapter(Context mContext, List<Entry> entries) {
        this.mContext = mContext;
        this.entries = entries;
    }

    public void setData(List<Entry> entryList) {
        this.entries = entryList;
        notifyDataSetChanged();
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView text;

        public TextViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            text = (TextView) v.findViewById(R.id.text);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public AppCompatImageView img;

        public ImageViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            img = (AppCompatImageView) v.findViewById(R.id.img);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
            default:
                View tv = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_text, parent, false);
                return new TextViewHolder(tv);
            case 1:
                View iv = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image, parent, false);
                return new ImageViewHolder(iv);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (entries.get(position).getType().equals("text"))
            return 0;
        else
            return 1;
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Entry entry = entries.get(position);
        switch (holder.getItemViewType()) {
            case 0:
                TextViewHolder textViewHolder = (TextViewHolder) holder;
                textViewHolder.title.setText(entry.getTitle());

                String file_content = getFileContent(entry);
                textViewHolder.text.setText(file_content);
                textViewHolder.text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mContext).createOrEditEntry("text", entry, file_content, null, null);
                    }
                });
                break;
            case 1:
                ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                imageViewHolder.title.setText(entry.getTitle());
                imageViewHolder.img.setImageResource(R.drawable.placeholder);

                Bitmap bitmap = getImageContent(entry);
                if (bitmap != null) {
                    imageViewHolder.img.setImageBitmap(bitmap);
                }

                imageViewHolder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mContext).createOrEditEntry("image", entry, null, bitmap, null);
                    }
                });
        }
    }

    private Bitmap getImageContent(Entry entry) {
        try {
//            Bitmap bitmap = BitmapFactory.decodeFile(entry.getFilename());
//            return bitmap;

            FileInputStream fis = mContext.openFileInput(entry.getFilename());
            Bitmap bitmap = BitmapFactory.decodeStream(fis, null, null);
            return bitmap;
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.toString());
            return null;
        }
    }

    private String getFileContent(Entry entry) {
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(entry.getFilename());
        } catch (FileNotFoundException e) {
            return "ERR";
        }
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            return "ERR";
        }
    }
}