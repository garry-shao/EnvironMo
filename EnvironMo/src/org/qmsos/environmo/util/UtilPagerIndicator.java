package org.qmsos.environmo.util;

import org.qmsos.environmo.R;

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
public class UtilPagerIndicator extends View implements OnPageChangeListener {

	private ViewPager viewPager;
	private OnPageChangeListener listener;
	
	private float radius;
	private float padding;
	private Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint unselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int current;
	
	public UtilPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		final Resources res = getResources();
		final float defaultRadius = res.getDimension(R.dimen.default_radius);
		final float defaultPadding = res.getDimension(R.dimen.default_padding);
		final int defaultSelectedColor = 
				ContextCompat.getColor(getContext(), R.color.default_selected_color);
		final int defaultUnselectedColor = 
				ContextCompat.getColor(getContext(), R.color.default_unselected_color); 
		
		TypedArray a = context.obtainStyledAttributes(
				attrs, R.styleable.UtilPagerIndicator, defStyleAttr, 0);
		
		radius = a.getDimension(R.styleable.UtilPagerIndicator_radius, defaultRadius);
		padding = a.getDimension(R.styleable.UtilPagerIndicator_padding, defaultPadding);
		selectedPaint.setColor(
				a.getColor(R.styleable.UtilPagerIndicator_selectedColor, defaultSelectedColor));
		unselectedPaint.setColor(
				a.getColor(R.styleable.UtilPagerIndicator_unselectedColor, defaultUnselectedColor));
		
		a.recycle();
	}

	public UtilPagerIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public UtilPagerIndicator(Context context) {
		this(context, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (viewPager == null) {
			return;
		}
		final int count = viewPager.getAdapter().getCount();
		if (count == 0 || count == 1) {
			return;
		}
		if (current >= count) {
			current = count - 1;
			return;
		}
		
		for (int i = 0; i < count; i++) {
			float cx = i * ( 2 * radius + padding) + radius;
			float cy = radius;
			if (i == current) {
				canvas.drawCircle(cx, cy, radius, selectedPaint);
			} else {
				canvas.drawCircle(cx, cy, radius, unselectedPaint);
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
		if (listener != null) {
			listener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if (listener != null) {
			listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		current = position;
		invalidate();
		
		if (listener != null) {
			listener.onPageSelected(position);
		}
	}

	public void setViewPager(ViewPager viewPager) {
		if (this.viewPager == viewPager) {
			return;
		}
		if (viewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter.");
		}
		this.viewPager = viewPager;
		this.viewPager.addOnPageChangeListener(this);
		invalidate();
	}

	public void setOnPageChangeListener(OnPageChangeListener listener) {
		this.listener = listener;
	}
	
	private int getWidth(int measureSpec) {
		int rawWidth;
		if (viewPager != null) {
			int count = viewPager.getAdapter().getCount();
			rawWidth = (int) (2 * count * radius + (count - 1) * padding
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
        	if (viewPager != null) {
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
		int rawHeight = (int) (2 * radius + 1 + getPaddingTop() + getPaddingBottom());
		
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
