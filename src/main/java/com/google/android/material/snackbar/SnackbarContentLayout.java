package com.google.android.material.snackbar;
import r.android.animation.TimeInterpolator;
import r.android.view.View;
import r.android.widget.Button;
import r.android.widget.LinearLayout;
import r.android.widget.TextView;
import androidx.core.view.ViewCompat;
public class SnackbarContentLayout extends LinearLayout implements ContentViewCallback {
  private final TimeInterpolator contentInterpolator=BaseTransientBottomBar.FAST_OUT_SLOW_IN_INTERPOLATOR;
  private int maxInlineActionWidth;
  protected void onMeasure(  int widthMeasureSpec,  int heightMeasureSpec){
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    if (getOrientation() == VERTICAL) {
      return;
    }
    final int multiLineVPadding=(int)com.ashera.widget.PluginInvoker.convertDpToPixel("24dp");
    final int singleLineVPadding=(int)com.ashera.widget.PluginInvoker.convertDpToPixel("14dp");
    //final Layout messageLayout=getMessageView().getLayout();
    final boolean isMultiLine= getNoOfLines() > 1;
    boolean remeasure=false;
    if (isMultiLine && maxInlineActionWidth > 0 && getActionView().getMeasuredWidth() > maxInlineActionWidth) {
      if (updateViewsWithinLayout(VERTICAL,multiLineVPadding,multiLineVPadding - singleLineVPadding)) {
        remeasure=true;
      }
    }
 else {
      final int messagePadding=isMultiLine ? multiLineVPadding : singleLineVPadding;
      if (updateViewsWithinLayout(HORIZONTAL,messagePadding,messagePadding)) {
        remeasure=true;
      }
    }
    if (remeasure) {
      super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }
  }
  private boolean updateViewsWithinLayout(  final int orientation,  final int messagePadTop,  final int messagePadBottom){
    boolean changed=false;
    if (orientation != getOrientation()) {
      setOrientation(orientation);
      changed=true;
    }
    if (getMessageView().getPaddingTop() != messagePadTop || getMessageView().getPaddingBottom() != messagePadBottom) {
      updateTopBottomPadding(getMessageView(),messagePadTop,messagePadBottom);
      changed=true;
    }
    return changed;
  }
  private static void updateTopBottomPadding(  View view,  int topPadding,  int bottomPadding){
    if (ViewCompat.isPaddingRelative(view)) {
      ViewCompat.setPaddingRelative(view,ViewCompat.getPaddingStart(view),topPadding,ViewCompat.getPaddingEnd(view),bottomPadding);
    }
 else {
      view.setPadding(view.getPaddingLeft(),topPadding,view.getPaddingRight(),bottomPadding);
    }
  }
  public void animateContentIn(  int delay,  int duration){
    r.android.animation.ObjectAnimator o = r.android.animation.ObjectAnimator.ofFloat(getMessageView(),"alpha", 0f, 1f);o.setDuration(duration);o.setInterpolator(contentInterpolator);o.setStartDelay(delay);o.start();
    //getMessageView().animate().alpha(1f).setDuration(duration).setInterpolator(contentInterpolator).setStartDelay(delay).start();
    if (getActionView().getVisibility() == VISIBLE) {
      r.android.animation.ObjectAnimator o1 = r.android.animation.ObjectAnimator.ofFloat(getActionView(),"alpha", 0f, 1f);o1.setDuration(duration);o.setInterpolator(contentInterpolator);o1.setStartDelay(delay);o1.start();
      //getActionView().animate().alpha(1f).setDuration(duration).setInterpolator(contentInterpolator).setStartDelay(delay).start();
    }
  }
  public void animateContentOut(  int delay,  int duration){
    r.android.animation.ObjectAnimator o = r.android.animation.ObjectAnimator.ofFloat(getMessageView(),"alpha", 1f, 0f);o.setDuration(duration);o.setInterpolator(contentInterpolator);o.setStartDelay(delay);o.start();
    //getMessageView().animate().alpha(0f).setDuration(duration).setInterpolator(contentInterpolator).setStartDelay(delay).start();
    if (getActionView().getVisibility() == VISIBLE) {
      r.android.animation.ObjectAnimator o1 = r.android.animation.ObjectAnimator.ofFloat(getActionView(),"alpha", 1f, 0f);o1.setDuration(duration);o.setInterpolator(contentInterpolator);o1.setStartDelay(delay);o1.start();
      //getActionView().animate().alpha(0f).setDuration(duration).setInterpolator(contentInterpolator).setStartDelay(delay).start();
    }
  }
  public void setMaxInlineActionWidth(  int width){
    maxInlineActionWidth=width;
  }
  public TextView getMessageView(){
    return (TextView)getChildAt(0);
  }
  public Button getActionView(){
    return (Button)getChildAt(1);
  }
  private int getNoOfLines(){
    if (getMessageView().getLineHeight() == 0) {
      return 0;
    }
    return (getMessageView().getMeasuredHeight() - getMessageView().getPaddingTop() - getMessageView().getPaddingBottom()) / getMessageView().getLineHeight();
  }
}
