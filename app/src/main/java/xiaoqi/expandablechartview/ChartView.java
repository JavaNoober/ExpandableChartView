package xiaoqi.expandablechartview;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;



public class ChartView extends SurfaceView implements SurfaceHolder.Callback {
	private Context context;
	private SurfaceHolder holder;
	private ValueAnimator chartAnimator;
	private ValueAnimator circleAnimator;

	//中间内存信息方块的坐标
	private float centerDetailLeft;
	private float centerDetailTop;
	private float centerDetailRight;
	private float centerDetailBottom;

	//chart外接正方形坐标
	private float chartLeft;
	private float chartTop;
	private float chartRight;
	private float chartBottom;
	//起始角度
	private float startAngle = 270;
	//半径
	private float radius;
	//各区域角度
	private float area1Angle;
	private float area2Angle;
	//区域的量
	private float total;
	private float area1;
	private float area2;
	private long time;
	private int repeatCount = 2;
	//是否为第一次显示，用于防止surface闪烁
	private boolean area1IsFirstShow = true;
	private boolean area2IsFirstShow = true;

	//大扇形外接正方形
	private RectF rectF;
	//小扇形外接正方形
	private RectF rectF2;

	private Paint area1Paint;
	private Paint area2Paint;
	private Paint area3Paint;
	private Paint circlePaint;
	private Paint arcPaint;
	private Paint loadingPaint;
	private Paint textPaint;

	private static final int CIRCLE_DURATION = 1000;

	public ChartView(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		init();
	}

	private void init() {
		radius = Utility.dip2px(context, 100);
		holder = getHolder();
		holder.addCallback(this);
		setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSLUCENT);
		initPaint();
		initAnimator();
	}

	private void initAnimator() {
		PropertyValuesHolder angleValues = PropertyValuesHolder.ofFloat("angle", 0f, 360f);
		chartAnimator = ValueAnimator.ofPropertyValuesHolder(angleValues);
		chartAnimator.setDuration(2000);
		chartAnimator.setInterpolator(new LinearInterpolator());
		chartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float angle = obj2Float(animation.getAnimatedValue("angle"));
				Canvas canvas = holder.lockCanvas(null);
				if(canvas != null){
					canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
					drawDetail(canvas);
//				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
					if (!area1IsFirstShow) {
						canvas.drawArc(rectF, startAngle, area1Angle, true, area1Paint);
					}
					if (!area2IsFirstShow) {
						canvas.drawArc(rectF2, area1Angle + startAngle, area2Angle, true, area2Paint);
					}
					if (angle < area1Angle) {
						canvas.drawArc(rectF, startAngle, angle, true, area1Paint);
					} else if (angle <= area2Angle + area1Angle) {
						if (area1IsFirstShow) {
							area1IsFirstShow = false;
							canvas.drawArc(rectF, startAngle, area1Angle, true, area1Paint);
						} else {
							canvas.drawArc(rectF2, startAngle+area1Angle, angle - area1Angle, true, area2Paint);
						}
					} else {
						if (area2IsFirstShow) {
							area2IsFirstShow = false;
							canvas.drawArc(rectF2, area1Angle + startAngle, area2Angle, true, area2Paint);
						} else {
							canvas.drawArc(rectF2, startAngle + area1Angle + area2Angle, angle - area2Angle - area1Angle,
									true, area3Paint);
						}
					}
					holder.unlockCanvasAndPost(canvas);
				}

			}
		});
		circleAnimator = ValueAnimator.ofPropertyValuesHolder(angleValues);
		circleAnimator.setInterpolator(new LinearInterpolator());
		circleAnimator.setDuration(CIRCLE_DURATION);
		circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float angle = obj2Float(animation.getAnimatedValue("angle"));
				Canvas canvas = holder.lockCanvas(null);
                if(canvas != null){
                    long nowTime = System.currentTimeMillis();
                    int rate = (int) (nowTime - time) / (CIRCLE_DURATION * (repeatCount + 1) / 100);
                    if (rate <= 100) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawText("正在加载" + rate + "%", getMeasuredWidth() / 2 - radius / 2,
                                getMeasuredHeight() / 2, loadingPaint);
                    }
                    canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2 - Utility.dip2px(context, 10),
                            radius, circlePaint);
                    canvas.drawArc(rectF2, 180 + angle, 30, false, arcPaint);
                    holder.unlockCanvasAndPost(canvas);
                }
			}
		});
		circleAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				time = System.currentTimeMillis();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				chartAnimator.start();
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
	}

	private void initPaint() {
		area1Paint = new Paint();
		area1Paint.setAntiAlias(true);
		area1Paint.setStyle(Paint.Style.FILL);
		area1Paint.setTextSize((Utility.dip2px(context, 15)));
		area1Paint.setColor(context.getResources().getColor(R.color.background_blue));
		area2Paint = new Paint();
		area2Paint.setAntiAlias(true);
		area2Paint.setStyle(Paint.Style.FILL);
		area2Paint.setTextSize((Utility.dip2px(context, 15)));
		area2Paint.setColor(context.getResources().getColor(R.color.chart_blue));
		area3Paint = new Paint();
		area3Paint.setAntiAlias(true);
		area3Paint.setStyle(Paint.Style.FILL);
		area3Paint.setTextSize((Utility.dip2px(context, 15)));
		area3Paint.setColor(context.getResources().getColor(R.color.light_gary));
		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setStrokeWidth(Utility.dip2px(context, 5));
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setColor(context.getResources().getColor(R.color.background_gray));
		arcPaint = new Paint();
		arcPaint.setAntiAlias(true);
		arcPaint.setStrokeWidth(Utility.dip2px(context, 5));
		arcPaint.setStyle(Paint.Style.STROKE);
		arcPaint.setColor(context.getResources().getColor(R.color.textcolor_gray));
		loadingPaint = new Paint();
		loadingPaint.setTextSize((Utility.dip2px(context, 15)));
		loadingPaint.setColor(context.getResources().getColor(R.color.textcolor_gray));
		textPaint = new Paint();
		textPaint.setTextSize((Utility.dip2px(context, 15)));
		textPaint.setColor(context.getResources().getColor(R.color.black));
	}

	private float obj2Float(Object o) {
		return ((Number) o).floatValue();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		chartLeft = getMeasuredWidth() / 2 - radius;
		chartTop = getMeasuredHeight() / 2 - radius - Utility.dip2px(context, 10);
		chartRight = getMeasuredWidth() / 2 + radius;
		chartBottom = getMeasuredHeight() / 2 + radius - Utility.dip2px(context, 10);
		centerDetailLeft = getMeasuredWidth() / 2 - Utility.dip2px(context, 20);
		centerDetailTop = getMeasuredHeight() / 2 + radius + Utility.dip2px(context, 15);
		centerDetailRight = getMeasuredWidth() / 2;
		centerDetailBottom = getMeasuredHeight() / 2 + radius + Utility.dip2px(context, 35);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		rectF = new RectF(chartLeft - Utility.dip2px(context, 5), chartTop - Utility.dip2px(context, 5), chartRight +
				Utility.dip2px(context, 5), chartBottom + Utility.dip2px(context, 5));
		rectF2 = new RectF(chartLeft, chartTop, chartRight, chartBottom);
//		valueAnimator.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		circleAnimator.cancel();
		chartAnimator.cancel();
	}

	private void drawDetail(Canvas canvas) {
		canvas.drawRect(centerDetailLeft - Utility.dip2px(context, 150), centerDetailTop,
				centerDetailRight - Utility.dip2px(context, 150), centerDetailBottom, area1Paint);
		canvas.drawRect(centerDetailLeft, centerDetailTop, centerDetailRight, centerDetailBottom, area2Paint);
		canvas.drawRect(centerDetailLeft + Utility.dip2px(context, 150), centerDetailTop,
				centerDetailRight + Utility.dip2px(context, 150), centerDetailBottom, area3Paint);
		drawText(canvas);
	}

	private void drawText(Canvas canvas) {
		canvas.drawText("本软件", centerDetailRight - Utility.dip2px(context, 150) + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 10), area1Paint);
		canvas.drawText("200MB", centerDetailRight - Utility.dip2px(context, 150) + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 25), textPaint);
		canvas.drawText("其他", centerDetailRight + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 10), area2Paint);
		canvas.drawText("24.1GB", centerDetailRight + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 25), textPaint);
		canvas.drawText("可用", centerDetailRight + Utility.dip2px(context, 150) + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 10), area3Paint);
		canvas.drawText("30GB", centerDetailRight + Utility.dip2px(context, 150) + Utility.dip2px(context, 5),
				centerDetailTop + Utility.dip2px(context, 25), textPaint);
	}

	public void show() {
		circleAnimator.setRepeatCount(repeatCount);
		circleAnimator.start();
	}

	public void setArea1Color(int color) {
		area1Paint.setColor(color);
	}

	public void setArea2Color(int color) {
		area2Paint.setColor(color);
	}

	public void setArea3Color(int color) {
		area3Paint.setColor(color);
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public void setScale(float total, float area1, float area2){
		area1Angle = area1/total * 360;
		area2Angle = area2/total * 360;
	}

	public void setRepeatCount(int repeatCount){
		this.repeatCount = repeatCount;
	}
}
