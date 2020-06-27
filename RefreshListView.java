package znjt.com.ssst1.controls;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import znjt.com.ssst1.R;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

/**
 * Created by Administrator on 2018/11/27 0027.
 */

public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private View header;
    private int headerHeight;//hedad的高度
    private int firstVisibleItem;//当前第一个item的位置
    private int scrollState;
    private boolean isRemark;//是否在最顶端
    private int startY;//开始Y值
    private int state;
    private final int RELESE=0;
    private final int REFRESHING=1;
    private final int PULL=2;
    private final int NONE=3;


    public RefreshListView(Context context) {
        super(context);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context) {
        header = LayoutInflater.from(context).inflate(R.layout.refresh_header, null);
        measureView(header);
        headerHeight = header.getMeasuredHeight();
        topPadding(-headerHeight);
        addHeaderView(header);
        setOnScrollListener(this);
    }

    /**
     * 滚动事件
     *
     * @param view             当前可见的view
     * @param firstVisibleItem 当前界面第一个可见的item的位置
     * @param visibleItemCount
     * @param totalItemCount
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    /**
     * 触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断界面是不是在最顶端
                if (firstVisibleItem == 0) {
                    isRemark = true;
                    startY = (int) ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                if (state == RELESE) {
                    state = REFRESHING;
                    //加载最新数据
                    refreshViewByState();
                    mListener.onRefresh();

                } else if (state == PULL) {
                    state = NONE;
                    isRemark = false;
                    refreshViewByState();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 判断移动过程中的操作
     */
    private void onMove(MotionEvent ev) {
        if (!isRemark) {
            return;
        }
        //当前已经移动到什么位置
        int tempY = (int) ev.getY();
        //手指移动的距离
        int space = tempY - startY;
        int topPadding = space - headerHeight;
        switch (state) {
            case NONE:
                if (space > 0) {
                    state = PULL;
                    refreshViewByState();
                }
                break;
            case PULL:
                topPadding(topPadding);
                if (space > headerHeight + 30 && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    state = RELESE;
                    refreshViewByState();
                }
                break;
            case RELESE:
                topPadding(topPadding);
                if (space < headerHeight + 30) {
                    state = PULL;
                    refreshViewByState();
                } else if (space <= 0) {
                    state = NONE;
                    isRemark = false;
                    refreshViewByState();
                }
                break;
            case REFRESHING:

                break;
        }
    }

    /**
     * 根据当前状态改变界面显示
     */
    private void refreshViewByState() {
        TextView textView = header.findViewById(R.id.txt_xia_la);
        ImageView imageView = header.findViewById(R.id.jian_tou);
        ProgressBar progressBar = header.findViewById(R.id.progress);

//        RotateAnimation animation1 = new RotateAnimation(0, 180,
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//        //设置时间间隔500ms
//        animation1.setDuration(500);
//        //当它完成时将会持续动画执行 转换
//        animation1.setFillAfter(true);
//        RotateAnimation animation2 = new RotateAnimation(180, 0,
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
//                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//        animation2.setDuration(500);
//        animation2.setFillAfter(true);
        switch (state) {
            case NONE:
                topPadding(-headerHeight);
                header.clearAnimation();
                break;
            case PULL:
                imageView.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                textView.setText("下拉可以刷新");
                imageView.clearAnimation();
                break;
            case RELESE:
                imageView.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                textView.setText("松开可以刷新");
                imageView.clearAnimation();
//                imageView.setAnimation(animation1);
                ObjectAnimator.ofFloat(imageView, "rotation",
                        0, 180F).setDuration(500).start();
                break;
            case REFRESHING:
                topPadding(headerHeight);
                imageView.setVisibility(GONE);
                progressBar.setVisibility(VISIBLE);
                textView.setText("正在刷新");
                imageView.clearAnimation();
                break;
        }
    }


    /**
     * 获取完数据
     */
    public void refreshComplete() {
        state = NONE;
        isRemark = false;
        refreshViewByState();
        TextView textView = header.findViewById(R.id.txt_last_time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String time = format.format(date);
        textView.setText(time);
    }

    /**
     * 刷新数据接口
     */
    public interface RefreshListener {
        void onRefresh();
    }

    public void setInterface(RefreshListener listener) {
        this.mListener = listener;
    }
    private RefreshListener mListener;




    /**
     * 设置header上边距
     *
     * @param topPadding 上边距
     */

    private void topPadding(int topPadding) {
        header.setPadding(header.getPaddingLeft(), topPadding,
                header.getPaddingRight(), header.getPaddingBottom());
        header.invalidate();
    }

    /**
     * 通知父布局占多大宽高
     */
    private void measureView(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, layoutParams.width);
        int height;
        int tempHeight = layoutParams.height;
        if (tempHeight > 0) {
            // 高度不是0的时候，要填充
            height = MeasureSpec.makeMeasureSpec(tempHeight, MeasureSpec.EXACTLY);
        } else {
            // 高度是0的时候不要填充
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
    }


}
