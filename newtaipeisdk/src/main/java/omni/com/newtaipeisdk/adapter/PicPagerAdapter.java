package omni.com.newtaipeisdk.adapter;

import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.WEBVIEW_TITLE;
import static omni.com.newtaipeisdk.NewTaipeiSDKActivity.WEBVIEW_URL;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import omni.com.newtaipeisdk.R;
import omni.com.newtaipeisdk.WebViewActivity;
import omni.com.newtaipeisdk.model.BannerData;

public class PicPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<BannerData> bannerList;

    public PicPagerAdapter(Context mContext,
                           ArrayList<BannerData> bannerList) {
        this.mContext = mContext;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final View layout = layoutInflater.inflate(R.layout.home_image_layout, container, false);
        final ImageView imageView = layout.findViewById(R.id.home_image_layout_iv);
        Glide.with(mContext).load(bannerList.get(position).getB_image()).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra(WEBVIEW_URL, bannerList.get(position).getB_link());
                intent.putExtra(WEBVIEW_TITLE, bannerList.get(position).getB_title());
                mContext.startActivity(intent);
            }
        });
        container.addView(layout);

        return layout;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (int i = 0; i < bannerList.size(); i++) {
            if (!bannerList.get(i).getB_link().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
