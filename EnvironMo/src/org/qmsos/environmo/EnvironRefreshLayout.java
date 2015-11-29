package org.qmsos.environmo;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class EnvironRefreshLayout extends SwipeRefreshLayout {

	private ScrollView scrollView = null;
	
	public EnvironRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public EnvironRefreshLayout(Context context) {
		super(context);
	}

	@Override
	public boolean canChildScrollUp() {
		if (scrollView != null)
			return scrollView.canScrollVertically(-1);
		else {
			return super.canChildScrollUp();
		}
	}


	public ScrollView getScrollView() {
		return scrollView;
	}


	public void setScrollView(ScrollView scrollView) {
		this.scrollView = scrollView;
	}

}
