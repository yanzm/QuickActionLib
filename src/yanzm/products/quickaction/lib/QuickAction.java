package yanzm.products.quickaction.lib;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * create QuickAction window
 * 
 * @author Yuki Anzai
 */
public class QuickAction extends PopupWindowForQuickAction {
	private View rootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private Animation mTrackAnim;
	private final LayoutInflater inflater;
	private final Context context;

	public static final int STYLE_BUTTON = 1;
	public static final int STYLE_LIST = 2;

	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;

	private int itemLayoutId;
	private int layoutStyle;
	private int animStyle;

	private boolean animateTrack;
	private ViewGroup mTrack;
	private ScrollView scroller;

	private ArrayList<ActionItem> actionList;

	/**
	 * Constructor
	 * 
	 * @param anchor
	 *            {@link View} on where the popup should be displayed
	 */
	public QuickAction(View anchor, int layoutId, int layoutStyle, int itemLayoutId) {
		super(anchor);

		actionList = new ArrayList<ActionItem>();

		context  = anchor.getContext();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(itemLayoutId == -1) {
			switch(layoutStyle) {
			case STYLE_LIST:
				itemLayoutId = R.layout.action_item_list;
				break;
			case STYLE_BUTTON:
			default:
				itemLayoutId = R.layout.action_item_btn;
				break;
			}
		}

		this.itemLayoutId = itemLayoutId;
		
		setLayoutId(layoutId, layoutStyle);

		animStyle   = ANIM_AUTO;

		// for button style
		setAnimTrack(R.anim.rail, new Interpolator() {
			public float getInterpolation(float t) {
				// Pushes past the target area, then snaps back into place.
				// Equation for graphing: 1.2 - ((x * 1.6) - 1.1)^2
				final float inner = (t * 1.55f) - 1.1f;

				return 1.2f - inner * inner;
			}
		});
	}
	
	/**
	 * Constructor
	 * 
	 * @param anchor
	 *            {@link View} on where the popup should be displayed
	 */
	public QuickAction(View anchor, int layoutId, int layoutStyle) {
		this(anchor, layoutId, layoutStyle, -1);
	}
	
	/**
	 * Constructor
	 * 
	 * @param anchor
	 *            {@link View} on where the popup should be displayed
	 */
	public QuickAction(View anchor) {
		 this(anchor, R.layout.quickaction, STYLE_BUTTON, R.layout.action_item_btn);
	}

	
	
	/**
	 * Animate track
	 * 
	 * @param animateTrack 
	 * 		flag to animate track
	 */
	public void setAnimTrackEnabled(boolean animateTrack) {
		this.animateTrack = animateTrack;
	}
	
	/**
	 * Animate track
	 * 
	 * @param animId
	 * 		resource id of animation
	 * @param interpolator
	 * 		interpolator of animation
	 */
	public void setAnimTrack(int animId, Interpolator interpolator) {
		mTrackAnim = AnimationUtils.loadAnimation(anchor.getContext(), animId);
		if(interpolator != null)
			mTrackAnim.setInterpolator(interpolator);
	}
	
	/**
	 * Set animation style
	 * 
	 * @param animStyle
	 *            animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	/**
	 * Set layout style
	 * 
	 * @param layoutStyle
	 *            layout style, default is set to STYLE_BUTTON
	 */
	public void setLayoutStyle(int layoutStyle) {
		switch (layoutStyle) {
		case STYLE_LIST:
			setLayoutId(R.layout.popup, layoutStyle);
			break;
			
		case STYLE_BUTTON:
		default:
			setLayoutId(R.layout.quickaction, layoutStyle);
			break;
		}
	}

	/**
	 * Set layout style
	 * 
	 * @param layoutStyle
	 *            layout style, default is set to STYLE_BUTTON
	 */
	public void setLayoutId(int layoutId, int layoutStyle) {
		this.layoutStyle = layoutStyle;

		rootView = (ViewGroup) inflater.inflate(layoutId, null);

		mArrowDown 	= (ImageView) rootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) rootView.findViewById(R.id.arrow_up);
		
		setContentView(rootView);	    

		mTrack 		= (ViewGroup) rootView.findViewById(R.id.tracks);

		switch (layoutStyle) {
			case STYLE_LIST:
				scroller	= null;		
				setAnimTrackEnabled(false);
				break;
				
			case STYLE_BUTTON:
			default:
				scroller	= (ScrollView) rootView.findViewById(R.id.scroller);
				setAnimTrackEnabled(true);
				break;
		}
	}
	
	public void setItemLayoutId(int itemLayoutId) {
		this.itemLayoutId = itemLayoutId;
	}

	/**
	 * Add action item
	 * 
	 * @param action
	 *            {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		actionList.add(action);
	}

	/**
	 * Show popup window
	 */
	public void show() {
		switch (layoutStyle) {
			case STYLE_LIST:
				showListStyle();
				break;
			case STYLE_BUTTON:
			default:
				showButtonStyle();
				break;
		}
	}

	/**
	 * Show popup window
	 */
	private void showButtonStyle() {
		preShow();

		int[] location = new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], 
				location[0] + anchor.getWidth(), 
				location[1] + anchor.getHeight());

		rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth  = rootView.getMeasuredWidth();
		int rootHeight = rootView.getMeasuredHeight();

		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		// int screenHeight = windowManager.getDefaultDisplay().getHeight();

		int xPos = (screenWidth - rootWidth) / 2;
		int yPos = anchorRect.top - rootHeight;

		boolean isOnTop = true;

		// display on bottom
		if (rootHeight > anchorRect.top) {
			yPos = anchorRect.bottom;
			isOnTop = false;
		}

		showArrow(((isOnTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX());

		setAnimationStyle(screenWidth, anchorRect.centerX(), isOnTop);

		createActionList();

		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);

		if (animateTrack)
			mTrack.startAnimation(mTrackAnim);
	}

	/**
	 * Show popup window. Popup is automatically positioned, on top or bottom of
	 * anchor view.
	 * 
	 */
	private void showListStyle() {
		preShow();

		int xPos, yPos;

		int[] location = new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], 
				location[0] + anchor.getWidth(), 
				location[1] + anchor.getHeight());

		createActionList();

		rootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootHeight = rootView.getMeasuredHeight();
		int rootWidth  = rootView.getMeasuredWidth();

		int screenWidth  = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();

		// automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left - (rootWidth - anchor.getWidth());
		} 
		else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth / 2);
			} else {
				xPos = anchorRect.left;
			}
		}

		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

		boolean isOnTop = (dyTop > dyBottom) ? true : false;

		if (isOnTop) {
			if (rootHeight > dyTop) {
				yPos = 15;
				LayoutParams l = scroller.getLayoutParams();
				l.height = dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom) {
				LayoutParams l = scroller.getLayoutParams();
				l.height = dyBottom;
			}
		}
		
		showArrow(((isOnTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() - xPos);
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), isOnTop);
		
		window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	/**
	 * Set animation style
	 * 
	 * @param screenWidth
	 *            Screen width
	 * @param requestedX
	 *            distance from left screen
	 * @param isOnTop
	 *            flag to indicate where the popup should be displayed. Set TRUE
	 *            if displayed on top of anchor and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean isOnTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

		switch (animStyle) {
		case ANIM_GROW_FROM_LEFT:
			window.setAnimationStyle((isOnTop) ? 
				R.style.Animations_PopUpMenu_Left : 
				R.style.Animations_PopDownMenu_Left);
			break;

		case ANIM_GROW_FROM_RIGHT:
			window.setAnimationStyle((isOnTop) ? 
				R.style.Animations_PopUpMenu_Right : 
				R.style.Animations_PopDownMenu_Right);
			break;

		case ANIM_GROW_FROM_CENTER:
			window.setAnimationStyle((isOnTop) ? 
				R.style.Animations_PopUpMenu_Center : 
				R.style.Animations_PopDownMenu_Center);
			break;

		case ANIM_AUTO:
			if (arrowPos <= screenWidth / 4) {
				window.setAnimationStyle((isOnTop) ? 
					R.style.Animations_PopUpMenu_Left : 
					R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
				window.setAnimationStyle((isOnTop) ? 
					R.style.Animations_PopUpMenu_Center : 
					R.style.Animations_PopDownMenu_Center);
			} else {
				window.setAnimationStyle((isOnTop) ? 
					R.style.Animations_PopDownMenu_Right : 
					R.style.Animations_PopDownMenu_Right);
			}

			break;
		}
	}

	/**
	 * Create action list
	 */
	private void createActionList() {
		View view;
		String title;
		Drawable icon;
		OnClickListener listener;
		int index = 1;
	
		for (ActionItem actionItem : actionList) {
			title 		= actionItem.getTitle();
			icon 		= actionItem.getIcon();
			listener	= actionItem.getListener();
	
			view 		= getActionItem(title, icon, listener);
		
			view.setFocusable(true);
			view.setClickable(true);
			 
			switch(layoutStyle) {
			case STYLE_LIST:
				mTrack.addView(view);
				break;
			case STYLE_BUTTON:
			default:
				mTrack.addView(view, index);
				index++;
				break;
			}
		}
	}
	
	/**
	 * Get action item {@link View}
	 * 
	 * @param title
	 *            action item title
	 * @param icon
	 *            {@link Drawable} action item icon
	 * @param listener
	 *            {@link View.OnClickListener} action item listener
	 * @return action item {@link View}
	 */
	private View getActionItem(String title, Drawable icon, OnClickListener listener) {
		
		LinearLayout container = (LinearLayout) inflater.inflate(itemLayoutId, null);

		ImageView img = (ImageView) container.findViewById(R.id.icon);
		TextView text = (TextView) container.findViewById(R.id.title);

		if (icon != null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}

		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}

		if (listener != null) {
			container.setOnClickListener(listener);
		}

		return container;
	}

	/**
	 * Show arrow
	 * 
	 * @param whichArrow
	 *            arrow type resource id
	 * @param requestedX
	 *            distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
		final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
		final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

		final int arrowWidth = mArrowUp.getMeasuredWidth();

		showArrow.setVisibility(View.VISIBLE);

		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

		param.leftMargin = requestedX - arrowWidth / 2;

		hideArrow.setVisibility(View.INVISIBLE);
	}
}