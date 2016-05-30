package org.qmsos.weathermo.widget;

import org.qmsos.weathermo.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;

/**
 * Draw circles as indicators of a ViewPager.
 * 
 *
 */
public class DotViewPagerIndicator extends View implements OnPageChangeListener {

	private ViewPager mViewPager;
	private OnPageChangeListener mListener;
	
	private float mRadius;
	private float mPadding;
	private Paint mSelectedPaint;
	private Paint mUnselectedPaint;
	
	private int mCurrent;
	
	public DotViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		final Resources res = getResources();
		final float defaultRadius = res.getDimension(R.dimen.default_radius);
		final float defaultPadding = res.getDimension(R.dimen.default_padding);
		final int defaultSelectedColor = 
				ContextCompat.getColor(getContext(), R.color.default_selected_color);
		final int defaultUnselectedColor = 
				ContextCompat.getColor(getContext(), R.color.default_unselected_color); 
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.DotViewPagerIndicator, defStyleAttr, 0);
		
		mRadius = a.getDimension(R.styleable.DotViewPagerIndicator_radius, defaultRadius);
		mPadding = a.getDimension(R.styleable.DotViewPagerIndicator_padding, defaultPadding);
		
		mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSelectedPaint.setColor(
				a.getColor(R.styleable.DotViewPagerIndicator_selectedColor, defaultSelectedColor));
		
		mUnselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mUnselectedPaint.setColor(
				a.getColor(R.styleable.DotViewPagerIndicator_unselectedColor, defaultUnselectedColor));
		
		a.recycle();
	}

	public DotViewPagerIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DotViewPagerIndicator(Context context) {
		this(context, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mViewPager == null) {
			return;
		}
		final int count = mViewPager.getAdapter().getCount();
		if (count == 0 || count == 1) {
			return;
		}
		if (mCurrent >= count) {
			mCurrent = count - 1;
			return;
		}
		
		for (int i = 0; i < count; i++) {
			float cx = i * ( 2 * mRadius + mPadding) + mRadius;
			float cy = mRadius;
			if (i == mCurrent) {
				canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
			} else {
				canvas.drawCircle(cx, cy, mRadius, mUnselectedPaint);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = getWidth(widthMeasureSpec);
		int measuredHeight = getHeight(heightMeasureSpec);
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		mCurrent = position;
		invalidate();
		
		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	public void setViewPager(ViewPager viewPager) {
		if (this.mViewPager == viewPager) {
			return;
		}
		if (viewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter.");
		}
		this.mViewPager = viewPager;
		this.mViewPager.addOnPageChangeListener(this);
		dataChanged();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.mListener = listener;
	}

	public void dataChanged() {
		if (!isInLayout()) {
			requestLayout();
		}
		invalidate();
	}
	
	private int getWidth(int measureSpec) {
		int rawWidth;
		if (mViewPager != null) {
			int count = mViewPager.getAdapter().getCount();
			rawWidth = (int) (2 * count * mRadius + (count - 1) * mPadding
					+ 1 + getPaddingLeft() + getPaddingRight());
		} else {
			rawWidth = 0;
		}
		
		int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        switch (specMode) {
        case MeasureSpec.EXACTLY:
        	return specSize;
        case MeasureSpec.AT_MOST:
        	if (mViewPager != null) {
        		return Math.min(rawWidth, specSize);
        	} else {
        		return specSize;
        	}
        case MeasureSpec.UNSPECIFIED:
        default:
			return rawWidth;
        }
	}
	
	private int getHeight(int measureSpec) {
		int rawHeight = (int) (2 * mRadius + 1 + getPaddingTop() + getPaddingBottom());
		
		int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        
        switch (specMode) {
        case MeasureSpec.EXACTLY:
        	return specSize;
        case MeasureSpec.AT_MOST:
        	return Math.min(rawHeight, specSize);
        case MeasureSpec.UNSPECIFIED:
        default:
			return rawHeight;
        }
	}

}
