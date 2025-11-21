//start - license
/*
 * Copyright (c) 2025 Ashera Cordova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
//end - license
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.snackbar;
import r.android.animation.Animator;
import r.android.animation.AnimatorListenerAdapter;
import r.android.animation.AnimatorSet;
import r.android.animation.TimeInterpolator;
import r.android.animation.ValueAnimator;
import r.android.animation.ValueAnimator.AnimatorUpdateListener;
import r.android.content.Context;
import r.android.graphics.Rect;
import r.android.os.Build;
import r.android.os.Build.VERSION;
import r.android.os.Build.VERSION_CODES;
import r.android.os.Handler;
import r.android.util.Log;
import r.android.view.Gravity;
import r.android.view.View;
import r.android.view.ViewGroup;
import r.android.view.ViewGroup.LayoutParams;
import r.android.view.ViewGroup.MarginLayoutParams;
import r.android.view.ViewParent;
import r.android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import java.lang.ref.WeakReference;
public abstract class BaseTransientBottomBar<B extends BaseTransientBottomBar<B>> {
  public static final int ANIMATION_MODE_SLIDE=0;
  public static final int ANIMATION_MODE_FADE=1;
public abstract static class BaseCallback<B> {
    public static final int DISMISS_EVENT_SWIPE=0;
    public static final int DISMISS_EVENT_ACTION=1;
    public static final int DISMISS_EVENT_TIMEOUT=2;
    public static final int DISMISS_EVENT_MANUAL=3;
    public static final int DISMISS_EVENT_CONSECUTIVE=4;
    public void onDismissed(    B transientBottomBar,    int event){
    }
    public void onShown(    B transientBottomBar){
    }
  }
  public static final int LENGTH_INDEFINITE=-2;
  public static final int LENGTH_SHORT=-1;
  public static final int LENGTH_LONG=0;
  static final int DEFAULT_SLIDE_ANIMATION_DURATION=250;
  static final int DEFAULT_ANIMATION_FADE_DURATION=180;
  private static final int DEFAULT_ANIMATION_FADE_IN_DURATION=150;
  private static final int DEFAULT_ANIMATION_FADE_OUT_DURATION=75;
  private static final float ANIMATION_SCALE_FROM_VALUE=0.8f;
  private final int animationFadeInDuration;
  private final int animationFadeOutDuration;
  private final int animationSlideDuration;
  private final TimeInterpolator animationFadeInterpolator;
  private final TimeInterpolator animationSlideInterpolator;
  private final TimeInterpolator animationScaleInterpolator;
  static final Handler handler;
  static final int MSG_SHOW=0;
  static final int MSG_DISMISS=1;
  private static final boolean USE_OFFSET_API=true || (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) && (Build.VERSION.SDK_INT <= VERSION_CODES.KITKAT);
  private static final String TAG=BaseTransientBottomBar.class.getSimpleName();
  private final ViewGroup targetParent;
  private final Context context;
  protected final SnackbarBaseLayout view;
  private final com.google.android.material.snackbar.ContentViewCallback contentViewCallback;
  private int duration;
  private boolean gestureInsetBottomIgnored;
  private Anchor anchor;
  private boolean anchorViewLayoutListenerEnabled=false;
  private final Runnable bottomMarginGestureInsetRunnable=new Runnable(){
    public void run(){
      if (view == null || context == null) {
        return;
      }
      int currentInsetBottom=getScreenHeight() - getViewAbsoluteBottom() + (int)view.getTranslationY();
      if (currentInsetBottom >= extraBottomMarginGestureInset) {
        return;
      }
      LayoutParams layoutParams=view.getLayoutParams();
      if (!(layoutParams instanceof MarginLayoutParams)) {
        Log.w(TAG,"Unable to apply gesture inset because layout params are not MarginLayoutParams");
        return;
      }
      MarginLayoutParams marginParams=(MarginLayoutParams)layoutParams;
      marginParams.bottomMargin+=extraBottomMarginGestureInset - currentInsetBottom;
      view.requestLayout();
    }
  }
;
  private int extraBottomMarginWindowInset;
  private int extraLeftMarginWindowInset;
  private int extraRightMarginWindowInset;
  private int extraBottomMarginGestureInset;
  private int extraBottomMarginAnchorView;
  private boolean pendingShowingView;
  private BaseTransientBottomBar.Behavior behavior;
  private void updateMargins(){
    LayoutParams layoutParams=view.getLayoutParams();
    if (!(layoutParams instanceof MarginLayoutParams) || view.originalMargins == null) {
      Log.w(TAG,"Unable to update margins because layout params are not MarginLayoutParams");
      return;
    }
    if (view.getParent() == null) {
      return;
    }
    int extraBottomMargin=getAnchorView() != null ? extraBottomMarginAnchorView : extraBottomMarginWindowInset;
    MarginLayoutParams marginParams=(MarginLayoutParams)layoutParams;
    marginParams.bottomMargin=view.originalMargins.bottom + extraBottomMargin;
    marginParams.leftMargin=view.originalMargins.left + extraLeftMarginWindowInset;
    marginParams.rightMargin=view.originalMargins.right + extraRightMarginWindowInset;
    marginParams.topMargin=view.originalMargins.top;
    view.requestLayout();
    if (VERSION.SDK_INT >= VERSION_CODES.Q && shouldUpdateGestureInset()) {
      view.removeCallbacks(bottomMarginGestureInsetRunnable);
      view.post(bottomMarginGestureInsetRunnable);
    }
  }
  private boolean shouldUpdateGestureInset(){
    return extraBottomMarginGestureInset > 0 && !gestureInsetBottomIgnored && isSwipeDismissable();
  }
  private boolean isSwipeDismissable(){
    LayoutParams layoutParams=view.getLayoutParams();
    return layoutParams instanceof CoordinatorLayout.LayoutParams && ((CoordinatorLayout.LayoutParams)layoutParams).getBehavior() instanceof SwipeDismissBehavior;
  }
  public B setDuration(  int duration){
    this.duration=duration;
    return (B)this;
  }
  public int getDuration(){
    return duration;
  }
  public int getAnimationMode(){
    return view.getAnimationMode();
  }
  public B setAnimationMode(  int animationMode){
    view.setAnimationMode(animationMode);
    return (B)this;
  }
  public View getAnchorView(){
    return anchor == null ? null : anchor.getAnchorView();
  }
  public void show(){
    SnackbarManager.getInstance().show(getDuration(),managerCallback);
  }
  public void dismiss(){
    dispatchDismiss(BaseCallback.DISMISS_EVENT_MANUAL);
  }
  protected void dispatchDismiss(  int event){
    SnackbarManager.getInstance().dismiss(managerCallback,event);
  }
  SnackbarManager.Callback managerCallback=new SnackbarManager.Callback(){
    public void show(){
      handler.sendMessage(handler.obtainMessage(MSG_SHOW,BaseTransientBottomBar.this));
    }
    public void dismiss(    int event){
      handler.sendMessage(handler.obtainMessage(MSG_DISMISS,event,0,BaseTransientBottomBar.this));
    }
  }
;
  protected SwipeDismissBehavior<? extends View> getNewBehavior(){
    return new Behavior();
  }
  final void showView(){
    if (this.behavior == null) {
      ViewGroup.LayoutParams lp=this.view.getLayoutParams();
      if (lp instanceof CoordinatorLayout.LayoutParams) {
        setUpBehavior((CoordinatorLayout.LayoutParams)lp);
      }
      //this.view.addToTargetParent(targetParent);
      recalculateAndUpdateMargins();
      view.setVisibility(View.INVISIBLE);
    }
    if (ViewCompat.isLaidOut(this.view)) {
      showViewImpl();
      return;
    }
    pendingShowingView=true;
  }
  private void showViewImpl(){
    if (shouldAnimate()) {
      animateViewIn();
    }
 else {
      if (view.getParent() != null) {
        view.setVisibility(View.VISIBLE);
      }
      onViewShown();
    }
  }
  private int getViewAbsoluteBottom(){
    int[] absoluteLocation=new int[2];
    view.getLocationOnScreen(absoluteLocation);
    return absoluteLocation[1] + view.getHeight();
  }
  private void setUpBehavior(  CoordinatorLayout.LayoutParams lp){
    CoordinatorLayout.LayoutParams clp=lp;
    SwipeDismissBehavior<? extends View> behavior=this.behavior == null ? getNewBehavior() : this.behavior;
    if (behavior instanceof BaseTransientBottomBar.Behavior) {
      ((Behavior)behavior).setBaseTransientBottomBar(this);
    }
    behavior.setListener(new SwipeDismissBehavior.OnDismissListener(){
      public void onDismiss(      View view){
        if (view.getParent() != null) {
          view.setVisibility(View.GONE);
        }
        dispatchDismiss(BaseCallback.DISMISS_EVENT_SWIPE);
      }
      public void onDragStateChanged(      int state){
switch (state) {
case SwipeDismissBehavior.STATE_DRAGGING:
case SwipeDismissBehavior.STATE_SETTLING:
          SnackbarManager.getInstance().pauseTimeout(managerCallback);
        break;
case SwipeDismissBehavior.STATE_IDLE:
      SnackbarManager.getInstance().restoreTimeoutIfPaused(managerCallback);
    break;
default :
}
}
}
);
clp.setBehavior(behavior);
if (getAnchorView() == null) {
clp.insetEdge=Gravity.BOTTOM;
}
}
private void recalculateAndUpdateMargins(){
int newBottomMarginAnchorView=calculateBottomMarginForAnchorView();
if (newBottomMarginAnchorView == extraBottomMarginAnchorView) {
return;
}
extraBottomMarginAnchorView=newBottomMarginAnchorView;
updateMargins();
}
private int calculateBottomMarginForAnchorView(){
if (getAnchorView() == null) {
return 0;
}
int[] anchorViewLocation=new int[2];
getAnchorView().getLocationOnScreen(anchorViewLocation);
int anchorViewAbsoluteYTop=anchorViewLocation[1];
int[] targetParentLocation=new int[2];
targetParent.getLocationOnScreen(targetParentLocation);
int targetParentAbsoluteYBottom=targetParentLocation[1] + targetParent.getHeight();
return targetParentAbsoluteYBottom - anchorViewAbsoluteYTop;
}
void animateViewIn(){
view.post(new Runnable(){
public void run(){
if (view == null) {
  return;
}
if (view.getParent() != null) {
  view.setVisibility(View.VISIBLE);
}
if (view.getAnimationMode() == ANIMATION_MODE_FADE) {
  startFadeInAnimation();
}
 else {
  startSlideInAnimation();
}
}
}
);
}
private void animateViewOut(int event){
if (view.getAnimationMode() == ANIMATION_MODE_FADE) {
startFadeOutAnimation(event);
}
 else {
startSlideOutAnimation(event);
}
}
private void startFadeInAnimation(){
ValueAnimator alphaAnimator=getAlphaAnimator(0,1);
ValueAnimator scaleAnimator=getScaleAnimator(ANIMATION_SCALE_FROM_VALUE,1);
AnimatorSet animatorSet=new AnimatorSet();
animatorSet.playTogether(alphaAnimator,scaleAnimator);
animatorSet.setDuration(animationFadeInDuration);
animatorSet.addListener(new AnimatorListenerAdapter(){
public void onAnimationEnd(Animator animator){
onViewShown();
}
}
);
animatorSet.start();
}
private void startFadeOutAnimation(final int event){
ValueAnimator animator=getAlphaAnimator(1,0);
animator.setDuration(animationFadeOutDuration);
animator.addListener(new AnimatorListenerAdapter(){
public void onAnimationEnd(Animator animator){
onViewHidden(event);
}
}
);
animator.start();
}
private ValueAnimator getAlphaAnimator(float... alphaValues){
ValueAnimator animator=ValueAnimator.ofFloat(alphaValues);
animator.setInterpolator(animationFadeInterpolator);
animator.addUpdateListener(new AnimatorUpdateListener(){
public void onAnimationUpdate(ValueAnimator valueAnimator){
view.setAlpha((Float)valueAnimator.getAnimatedValue());
}
}
);
return animator;
}
private ValueAnimator getScaleAnimator(float... scaleValues){
ValueAnimator animator=ValueAnimator.ofFloat(scaleValues);
animator.setInterpolator(animationScaleInterpolator);
animator.addUpdateListener(new AnimatorUpdateListener(){
public void onAnimationUpdate(ValueAnimator valueAnimator){
float scale=(float)valueAnimator.getAnimatedValue();
view.setScaleX(scale);
view.setScaleY(scale);
}
}
);
return animator;
}
private void startSlideInAnimation(){
final int translationYBottom=getTranslationYBottom();
if (USE_OFFSET_API) {
ViewCompat.offsetTopAndBottom(view,translationYBottom);
}
 else {
view.setTranslationY(translationYBottom);
}
ValueAnimator animator=new ValueAnimator();
animator.setIntValues(translationYBottom,0);
animator.setInterpolator(animationSlideInterpolator);
animator.setDuration(animationSlideDuration);
animator.addListener(new AnimatorListenerAdapter(){
public void onAnimationStart(Animator animator){
contentViewCallback.animateContentIn(animationSlideDuration - animationFadeInDuration,animationFadeInDuration);
}
public void onAnimationEnd(Animator animator){
onViewShown();
}
}
);
animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
private int previousAnimatedIntValue=translationYBottom;
public void onAnimationUpdate(ValueAnimator animator){
int currentAnimatedIntValue=(int)animator.getAnimatedValue();
if (USE_OFFSET_API) {
  ViewCompat.offsetTopAndBottom(view,currentAnimatedIntValue - previousAnimatedIntValue);
}
 else {
  view.setTranslationY(currentAnimatedIntValue);
}
previousAnimatedIntValue=currentAnimatedIntValue;onChildViewsChanged(view);
}
}
);
animator.start();
}
private void startSlideOutAnimation(final int event){
ValueAnimator animator=new ValueAnimator();
animator.setIntValues(0,getTranslationYBottom());
animator.setInterpolator(animationSlideInterpolator);
animator.setDuration(animationSlideDuration);
animator.addListener(new AnimatorListenerAdapter(){
public void onAnimationStart(Animator animator){
contentViewCallback.animateContentOut(0,animationFadeOutDuration);
}
public void onAnimationEnd(Animator animator){
onViewHidden(event);
}
}
);
animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
private int previousAnimatedIntValue=0;
public void onAnimationUpdate(ValueAnimator animator){
int currentAnimatedIntValue=(int)animator.getAnimatedValue();
if (USE_OFFSET_API) {
  ViewCompat.offsetTopAndBottom(view,currentAnimatedIntValue - previousAnimatedIntValue);
}
 else {
  view.setTranslationY(currentAnimatedIntValue);
}
previousAnimatedIntValue=currentAnimatedIntValue;onChildViewsChanged(view);
}
}
);
animator.start();
}
private int getTranslationYBottom(){
int translationY=view.getHeight();
LayoutParams layoutParams=view.getLayoutParams();
if (layoutParams instanceof MarginLayoutParams) {
translationY+=((MarginLayoutParams)layoutParams).bottomMargin;
}
return translationY;
}
final void hideView(int event){
if (shouldAnimate() && view.getVisibility() == View.VISIBLE) {
animateViewOut(event);
}
 else {
onViewHidden(event);
}
}
protected static class SnackbarBaseLayout extends FrameLayout {
private int animationMode;
private final int maxWidth = 0;
private final int maxInlineActionWidth = 0;
private Rect originalMargins;
private boolean addingToTargetParent;
protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
if (maxWidth > 0 && getMeasuredWidth() > maxWidth) {
widthMeasureSpec=MeasureSpec.makeMeasureSpec(maxWidth,MeasureSpec.EXACTLY);
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
}
}
int getAnimationMode(){
return animationMode;
}
void setAnimationMode(int animationMode){
this.animationMode=animationMode;
}
void addToTargetParent(ViewGroup targetParent){
addingToTargetParent=true;
targetParent.addView(this);
addingToTargetParent=false;
}
}
static class Anchor {
private final WeakReference<BaseTransientBottomBar> transientBottomBar;
private final WeakReference<View> anchorView;
private Anchor(BaseTransientBottomBar transientBottomBar,View anchorView){
this.transientBottomBar=new WeakReference<>(transientBottomBar);
this.anchorView=new WeakReference<>(anchorView);
}
View getAnchorView(){
return anchorView.get();
}
}
public static final TimeInterpolator LINEAR_INTERPOLATOR=new r.android.view.animation.LinearInterpolator();
public static final TimeInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR=new androidx.interpolator.view.animation.FastOutSlowInInterpolator();
public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR=new androidx.interpolator.view.animation.FastOutLinearInInterpolator();
public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR=new androidx.interpolator.view.animation.LinearOutSlowInInterpolator();
public static final TimeInterpolator DECELERATE_INTERPOLATOR=new r.android.view.animation.DecelerateInterpolator();
private static final TimeInterpolator DEFAULT_ANIMATION_FADE_INTERPOLATOR=LINEAR_INTERPOLATOR;
private static final TimeInterpolator DEFAULT_ANIMATION_SCALE_INTERPOLATOR=LINEAR_OUT_SLOW_IN_INTERPOLATOR;
private static final TimeInterpolator DEFAULT_ANIMATION_SLIDE_INTERPOLATOR=FAST_OUT_SLOW_IN_INTERPOLATOR;
static{
handler=new Handler(r.android.os.Looper.getMainLooper(),new Handler.Callback(){
@Override public boolean handleMessage(r.android.os.Message message){
switch (message.what) {
case MSG_SHOW:
  ((BaseTransientBottomBar)message.obj).showView();
return true;
case MSG_DISMISS:
((BaseTransientBottomBar)message.obj).hideView(message.arg1);
return true;
default :
return false;
}
}
}
);
}
class Behavior extends SwipeDismissBehavior {
Behavior(){
view.setMyAttribute("onSwiped",new com.ashera.layout.SwipeHelper.SwipeListener(){
@Override public boolean onSwiped(String direction){
if (direction.equals("right")) {
if (onDismissListener != null) {
onDismissListener.onDismiss(view);
}
}
return true;
}
}
);
}
public void setBaseTransientBottomBar(BaseTransientBottomBar<B> baseTransientBottomBar){
}
}
static class SwipeDismissBehavior<T> extends androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior<View> {
protected static final int STATE_IDLE=0;
protected static final int STATE_SETTLING=1;
protected static final int STATE_DRAGGING=2;
public interface OnDismissListener {
public void onDismiss(View view);
}
protected OnDismissListener onDismissListener;
public void setListener(OnDismissListener onDismissListener){
this.onDismissListener=onDismissListener;
}
}
protected BaseTransientBottomBar(Context context,ViewGroup parent,SnackbarBaseLayout view){
if (parent == null) {
throw new IllegalArgumentException("Transient bottom bar must have non-null parent");
}
this.view=view;
targetParent=parent;
this.context=context;
animationFadeInDuration=DEFAULT_ANIMATION_FADE_IN_DURATION;
animationFadeOutDuration=DEFAULT_ANIMATION_FADE_OUT_DURATION;
animationSlideDuration=DEFAULT_SLIDE_ANIMATION_DURATION;
animationFadeInterpolator=DEFAULT_ANIMATION_FADE_INTERPOLATOR;
animationSlideInterpolator=DEFAULT_ANIMATION_SLIDE_INTERPOLATOR;
animationScaleInterpolator=DEFAULT_ANIMATION_SCALE_INTERPOLATOR;
contentViewCallback=(SnackbarContentLayout)view.getChildAt(0);
}
public int getScreenHeight(){
return com.ashera.widget.PluginInvoker.getScreenHeight();
}
boolean shouldAnimate(){
return true;
}
private void onViewHidden(int event){
SnackbarManager.getInstance().onDismissed(managerCallback);
if (this.view != null) {
try {
View parent=(View)this.view.getRootView();
this.view.removeFromParent();
parent.requestLayout();
parent.remeasure();
}
 catch (Exception e) {
}
}
}
private void onViewShown(){
SnackbarManager.getInstance().onShown(managerCallback);
if (this.view != null) {
this.view.requestLayout();
this.view.remeasure();
}
}
private void onChildViewsChanged(View view){
View parent=(View)view.getParent();
while (parent != null) {
if (parent instanceof CoordinatorLayout) {
((CoordinatorLayout)parent).onChildViewsChanged(0);
}
parent=(View)parent.getParent();
}
}
}
