package org.happymtb.unofficial.view;

import org.happymtb.unofficial.R;
import org.happymtb.unofficial.fragment.KoSListFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KoSRowView extends LinearLayout {
	TextView mTitle;
	TextView mTime;
	TextView mArea;
	TextView mImgLink;
	TextView mCategory;
	TextView mPrice;
	ImageView mObjectImageView;
	LinearLayout compoundView;
	LinearLayout mRowColor;
	private Boolean mPictureList;
	private SharedPreferences preferences;

	public KoSRowView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		mPictureList = preferences.getBoolean(KoSListFragment.SHOW_IMAGES, true);
	
		if (mPictureList) {
			compoundView = (LinearLayout) inflater.inflate(R.layout.kos_row_picture, this);
			
			mRowColor = (LinearLayout) compoundView.findViewById(R.id.kos_picture_row_color);
			mTitle = (TextView) compoundView.findViewById(R.id.kos_picture_row_title);
			mTime = (TextView) compoundView.findViewById(R.id.kos_picture_row_time);
			mArea = (TextView) compoundView.findViewById(R.id.kos_picture_row_area);
			mCategory = (TextView) compoundView.findViewById(R.id.kos_picture_row_category);
			mPrice = (TextView) compoundView.findViewById(R.id.kos_picture_row_price);						
			mObjectImageView = (ImageView) compoundView.findViewById(R.id.kos_picture_row_image);
		} else {
			compoundView = (LinearLayout) inflater.inflate(R.layout.kos_row, this);
			
			mRowColor = (LinearLayout) compoundView.findViewById(R.id.kos_row_color);	
			mTitle = (TextView) compoundView.findViewById(R.id.kos_row_title);
			mTime = (TextView) compoundView.findViewById(R.id.kos_row_time);
			mArea = (TextView) compoundView.findViewById(R.id.kos_row_area);
			mCategory = (TextView) compoundView.findViewById(R.id.kos_row_category);
			mPrice = (TextView) compoundView.findViewById(R.id.kos_row_price);		
		}		
	}

	public void setBackgroundColor(int color) {
		LinearLayout mRow = (LinearLayout) compoundView
				.findViewById(R.id.kos_row);
		mRow.setBackgroundResource(color);
	}

	public void setObjectImage(Drawable image) {
		if (mObjectImageView != null) {
			if (image == null) {
				image = getResources().getDrawable(R.drawable.no_photo);
			}
			mObjectImageView.setImageDrawable(image);
		}
	}

	public void setRowBackgroundColor(int id) {
		if (mRowColor != null) {
			mRowColor.setBackgroundResource(id);
		}
	}	
	
	public void setTitle(String title) {
		if (mTitle != null) {
			mTitle.setText(title);
		}
	}

	public void setTime(String time) {
		if (mTime != null) {
			mTime.setText(time);
		}
	}

	public void setArea(String area) {
		if (mArea != null) {
			mArea.setText(area);
		}
	}

	public void setImgLink(String imgLink) {
		if (mImgLink != null) {
			mImgLink.setText(imgLink);
		}
	}

	public void setCategory(String category) {
		if (mCategory != null) {
			mCategory.setText(category);
		}
	}

	public void setPrice(String price) {
		if (mPrice != null) {
			mPrice.setText(price);
		}
	}

}
