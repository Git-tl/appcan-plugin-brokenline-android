package com.zywx.uexbrokenline;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class MySCView extends View {

	private float cellWidth = 80;
	private float cellHeight = 75;
	private float cellHeightValue;

	private int width;
	private int height;
	private int pointRectDiameter = 20;

	private int tempXgap;
	private int tempYgap;
	private int tempMargin;
	private final static int gridColor = Color.rgb(183, 186, 177);
//	private final static int redColor = Color.rgb(204, 42, 42);
	private final static int greenLineColor = Color.argb(102, 0, 255, 0);
	private final static int greenRectColor = Color.rgb(96, 200, 51);
	private final static int blueColor = Color.rgb(00, 117, 166);

	public static final int Ygap = 70;// y轴空隙 dip
	public static final int Xgap = 70;// X轴空隙 dip
	public static final int margin = 20;
	private float density;
	private float yMin;
	private float yMax;
	private float yStep;
	private int actX;
	private List<String> XValue;
	private List<String> YValue;
	private List<String> compareYList;
	private Scroller scroller;
	private float lastX;
	private float lastY;
	private int size;
	private int todayOffset;
	private Context context;
	private int scrollX;
	private int fw;

	public MySCView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scroller = new Scroller(context);
	}

	public void setData(Context context, int frameWidth, int frameHeight,
			float ds, float yMin, float yMax, float yStep, int actX,
			float xCount, List<String> XValue, List<String> YValue,
			List<String> compareYList) {
		fw = frameWidth;
		this.context = context;
		this.density = 1;
		this.yMin = yMin;
		this.yMax = yMax;
		this.yStep = yStep;
		this.actX = actX;
		this.XValue = XValue;
		this.YValue = YValue;
		this.compareYList = compareYList;
		tempYgap = (int) (Ygap * density);
		tempXgap = (int) (Xgap * density);
		tempMargin = (int) (margin * density);
		frameHeight = (int) (frameHeight * density);
		frameWidth = (int) (frameWidth * density);

		frameHeight = frameHeight - tempMargin * 2 - tempXgap;
		frameWidth = frameWidth - tempMargin * 2;

		cellWidth = frameWidth / xCount;
		cellHeight = ((float) frameHeight / ((float) (yMax - yMin) / (float) yStep));
		cellHeightValue = cellHeight / (float) yStep;

		frameHeight = frameHeight + tempMargin * 2 + tempXgap;
		frameWidth = frameWidth + tempMargin * 2;
		todayOffset = actX;
		size = YValue.size();
		width = (int) (size * cellWidth) + tempYgap;
		height = frameHeight;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		drawGrid(canvas, (int) ((float) (yMax - yMin) / (float) yStep) + 1);
		Paint rectPaint = new Paint();
		int halfpointRectDiameter = pointRectDiameter / 2;
		List<PointF> points = new ArrayList<PointF>();
		for (int i = 0; i < size; i++) {
			float pointX = (float) (i * cellWidth) + tempMargin + tempYgap;
			float pointY = height
					- ((Float.valueOf(YValue.get(i)) - yMin) * cellHeightValue)
					- tempMargin - tempXgap;
			PointF point = new PointF(pointX, pointY);
			points.add(point);
		}
		rectPaint.setStrokeWidth(8 * density);
		rectPaint.setColor(Color.RED);
		int compareYSize = compareYList.size();
		for (int i = 0; i < compareYSize; i++) {
			String string = compareYList.get(i);
			String[] strings = string.split(",");
			float s = Integer.valueOf(strings[0]);
			float e = Integer.valueOf(strings[1]);
			float v = Float.valueOf(strings[2]);
			float lineY = height - ((v - yMin) * cellHeightValue) - tempMargin
					- tempXgap;
			canvas.drawLine(tempYgap + tempMargin + cellWidth * s, lineY,
					tempYgap + tempMargin + cellWidth * e, lineY, rectPaint);

		}
		rectPaint.setColor(greenLineColor);
		for (int i = 0; i < size - 1; i++) {
			PointF startPointF = points.get(i);
			PointF endPointF = points.get(i + 1);
			canvas.drawLine(startPointF.x, startPointF.y, endPointF.x,
					endPointF.y, rectPaint);
		}
		rectPaint.setColor(Color.BLACK);
		rectPaint.setTextAlign(Paint.Align.CENTER);
		rectPaint.setTextSize(30 * density);
		int bitmapW = 200;
		int halfW = bitmapW / 2;
		int halfH = tempXgap / 2;
		for (int i = 0; i < size; i++) {
			PointF point = points.get(i);
			if (i == actX) {
				rectPaint.setColor(Color.RED);
			}
			Bitmap bitmap2 = Bitmap.createBitmap(bitmapW, tempXgap, Config.ARGB_8888);
			Canvas canvas2 = new Canvas(bitmap2);
			canvas2.drawText(XValue.get(i), halfW, halfH, rectPaint);
			Matrix matrix = new Matrix();
			matrix.postRotate(-40, halfW, halfH);
			matrix.postTranslate(point.x - halfW, height - halfH * 2);
			canvas.drawBitmap(bitmap2, matrix, null);
			if (!bitmap2.isRecycled()) {
				bitmap2.recycle();
			}
			if (i == actX) {
				rectPaint.setColor(Color.BLACK);
			}
		}

		if (todayOffset <= size) {
			rectPaint.setColor(blueColor);
			for (int i = todayOffset; i < size; i++) {
				PointF point = points.get(i);
				canvas.drawRect(point.x - halfpointRectDiameter, point.y
						- halfpointRectDiameter, point.x
						+ halfpointRectDiameter, point.y
						+ halfpointRectDiameter, rectPaint);
			}
		}

		rectPaint.setColor(greenRectColor);
		if (todayOffset > size) {
			todayOffset = size;
		}
		for (int i = 0; i < todayOffset; i++) {
			PointF point = points.get(i);
			canvas.drawRect(point.x - halfpointRectDiameter, point.y
					- halfpointRectDiameter, point.x + halfpointRectDiameter,
					point.y + halfpointRectDiameter, rectPaint);
		}
	}

	private void drawGrid(Canvas canvas, int sizeH) {
		int sizeWidth = (int) ((float) width / (float) cellWidth);
		int sizeHeight = sizeH;
		Paint linePaint = new Paint();
		linePaint.setColor(gridColor);
		linePaint.setAntiAlias(true);
		for (int i = 0; i < sizeWidth; i++) {
			canvas.drawLine(i * cellWidth + tempMargin + tempYgap, 0, i
					* cellWidth + tempMargin + tempYgap, height - tempXgap,
					linePaint);
		}
		PathEffect effect = new DashPathEffect(new float[] { 1, 2, 4, 8 }, 1);
		linePaint.setPathEffect(effect);
		for (int i = 0; i < sizeHeight; i++) {
			float y = i * cellHeight + tempMargin;
			canvas.drawLine(0, y, width, y, linePaint);
		}

		float y = (sizeHeight - 1) * cellHeight + tempMargin;
		canvas.drawRect(0, y - 2, width, y + 2, linePaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			return true;
		case MotionEvent.ACTION_MOVE:
			float newX = event.getX();
			scrollBy((int) (lastX - newX), 0);
			lastX = newX;
			break;
		case MotionEvent.ACTION_UP:
			scrollX = getScrollX();
			if (scrollX < 0) {
				scrollX = 0;
				scrollTo(0, 0);
			}
			if (scrollX > width - fw) {
				scrollX = width - fw;
				scrollTo(width - fw, 0);
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			return;
		}
		super.computeScroll();
	}
}
