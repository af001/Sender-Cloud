package locateme.technology.xor.locateme.support;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import locateme.technology.xor.locateme.R;

public class AccountsAdapter extends ArrayAdapter<TrackedAccount> {

    public AccountsAdapter(Context context, ArrayList<TrackedAccount> accounts) {
        super(context, 0, accounts);
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final TrackedAccount trackedAccount = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);
        }

        ImageView avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
        TextView name = (TextView) convertView.findViewById(R.id.tv_name);

        Resources res = convertView.getResources();
        int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        LetterTileProvider tileProvider = new LetterTileProvider(getContext());
        Bitmap letterTile = tileProvider.getLetterTile(trackedAccount.nickname, trackedAccount.trackedId, tileSize, tileSize);

        avatar.setImageBitmap(letterTile);
        name.setText(trackedAccount.nickname);

        return convertView;
    }
}
