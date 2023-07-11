package com.google.android.material.snackbar;
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
  private final int animationFadeInDuration;
  private final int animationFadeOutDuration;
  private final int animationSlideDuration;
  static final Handler handler;
  static final int MSG_SHOW=0;
  static final int MSG_DISMISS=1;
  private static final boolean USE_OFFSET_API=(Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) && (Build.VERSION.SDK_INT <= VERSION_CODES.KITKAT);
  private static final String TAG=BaseTransientBottomBar.class.getSimpleName();
  private final ViewGroup targetParent;
  private final Context context;
  protected final SnackbarBaseLayout view;
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
animationFadeInDuration=0;
animationFadeOutDuration=0;
animationSlideDuration=0;
}
public int getScreenHeight(){
return com.ashera.widget.PluginInvoker.getScreenHeight();
}
public void animateViewIn(){
}
public void animateViewOut(int event){
}
boolean shouldAnimate(){
return false;
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
}
