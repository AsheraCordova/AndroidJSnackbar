package com.google.android.material.snackbar;
import r.android.content.Context;
import r.android.content.res.ColorStateList;
import r.android.text.TextUtils;
import r.android.view.View;
import r.android.view.ViewGroup;
import r.android.view.ViewParent;
import r.android.widget.Button;
import r.android.widget.FrameLayout;
import r.android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
public class Snackbar extends BaseTransientBottomBar<Snackbar> {
  private boolean hasAction;
public static class Callback extends BaseCallback<Snackbar> {
    public static final int DISMISS_EVENT_SWIPE=BaseCallback.DISMISS_EVENT_SWIPE;
    public static final int DISMISS_EVENT_ACTION=BaseCallback.DISMISS_EVENT_ACTION;
    public static final int DISMISS_EVENT_TIMEOUT=BaseCallback.DISMISS_EVENT_TIMEOUT;
    public static final int DISMISS_EVENT_MANUAL=BaseCallback.DISMISS_EVENT_MANUAL;
    public static final int DISMISS_EVENT_CONSECUTIVE=BaseCallback.DISMISS_EVENT_CONSECUTIVE;
    public void onShown(    Snackbar sb){
    }
    public void onDismissed(    Snackbar transientBottomBar,    int event){
    }
  }
  public static Snackbar make(  Context context,  View view,  String text,  int duration){
    return makeInternal(context,view,text,duration);
  }
  private static Snackbar makeInternal(  Context context,  View view,  String text,  int duration){
    final ViewGroup parent=findSuitableParent(view);
    if (parent == null) {
      throw new IllegalArgumentException("No suitable parent found from the given view. Please provide a valid view.");
    }
    if (context == null) {
      context=parent.getContext();
    }
    //final LayoutInflater inflater=LayoutInflater.from(context);
    //final SnackbarContentLayout content=(SnackbarContentLayout)inflater.inflate(hasSnackbarContentStyleAttrs(context) ? R.layout.mtrl_layout_snackbar_include : R.layout.design_layout_snackbar_include,parent,false);
    final SnackbarBaseLayout content=(SnackbarBaseLayout) parent.inflateView("@layout/my_design_layout_snackbar");if (content.getLayoutParams() instanceof androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams) {((androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams)content.getLayoutParams()).gravity = r.android.view.Gravity.BOTTOM;((androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams)content.getLayoutParams()).insetEdge = r.android.view.Gravity.BOTTOM;}final Snackbar snackbar=new Snackbar(context,parent,content);
    snackbar.setText(text);
    snackbar.setDuration(duration);
    return snackbar;
  }
  private static ViewGroup findSuitableParent(  View view){
    ViewGroup fallback=null;
    do {
      if (view instanceof CoordinatorLayout) {
        return (ViewGroup)view;
      }
 else       if (view instanceof FrameLayout) {
        if (view.getId() != -1 && com.ashera.widget.IdGenerator.getName(view.getId()).equals("@+id/content")) {
          return (ViewGroup)view;
        }
 else {
          fallback=(ViewGroup)view;
        }
      }
      if (view != null) {
        final ViewParent parent=view.getParent();
        view=parent instanceof View ? (View)parent : null;
      }
    }
 while (view != null);
    return fallback;
  }
  public Snackbar setText(  CharSequence message){
    getMessageView().setText((String) message);
    return this;
  }
  public Snackbar setAction(  String text,  final View.OnClickListener listener){
    final TextView tv=getActionView();
    if (TextUtils.isEmpty(text) || listener == null) {
      tv.setVisibility(View.GONE);
      tv.setMyAttribute("onClick", null);
      hasAction=false;
    }
 else {
      hasAction=true;
      tv.setVisibility(View.VISIBLE);
      tv.setText(text);
      tv.setMyAttribute("onClick", new View.OnClickListener() {public void onClick(View v) {
        listener.onClick(view);
        dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);}
      }
);
    }
    return this;
  }
  public Snackbar setTextColor(  ColorStateList colors){
    getMessageView().setMyAttribute("textColor", colors);
    return this;
  }
  public Snackbar setActionTextColor(  ColorStateList colors){
    getActionView().setMyAttribute("textColor", colors);
    return this;
  }
  public Snackbar setMaxInlineActionWidth(  int width){
    getContentLayout().setMaxInlineActionWidth(width);
    return this;
  }
public static class SnackbarLayout extends BaseTransientBottomBar.SnackbarBaseLayout {
    protected void onMeasure(    int widthMeasureSpec,    int heightMeasureSpec){
      super.onMeasure(widthMeasureSpec,heightMeasureSpec);
      int childCount=getChildCount();
      int availableWidth=getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
      for (int i=0; i < childCount; i++) {
        View child=getChildAt(i);
        if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) {
          child.measure(MeasureSpec.makeMeasureSpec(availableWidth,MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),MeasureSpec.EXACTLY));
        }
      }
    }
  }
  private TextView getMessageView(){
    return getContentLayout().getMessageView();
  }
  private Button getActionView(){
    return getContentLayout().getActionView();
  }
  private SnackbarContentLayout getContentLayout(){
    return (SnackbarContentLayout)view.getChildAt(0);
  }
  public Snackbar(  Context context,  ViewGroup parent,  SnackbarBaseLayout content){
    super(context,parent,content);
  }
  public static Snackbar make(  View view,  String text,  int duration){
    return makeInternal(view.getContext(),view,text,duration);
  }
  public void dismissImmediate(){
    SnackbarManager.getInstance().onDismissed(managerCallback);
  }
  public void setBackground(  r.android.graphics.drawable.Drawable background){
    this.view.getChildAt(0).setMyAttribute("background",background);
  }
}
