package com.zywx.uexbrokenline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class uexBrokenLine extends EUExBase {

	public static final int Ygap = 70;// y轴空隙 dip
	public static final int Xgap = 70;// X轴空隙 dip
	public static final int margin = 20;
	private Map<String, ViewGroup> views = new HashMap<String, ViewGroup>();
	private Map<String, Bitmap> yViews = new HashMap<String, Bitmap>();

	private String json;
	private float density;

	public uexBrokenLine(Context context, EBrowserView view) {
		super(context, view);
		density = mContext.getResources().getDisplayMetrics().density;
	}

	public void setData(String[] parm) {
		if (parm.length < 0) {
			return;
		}
		json = parm[0];
	}

	public void open(final String[] parm) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (parm.length < 5) {
					return;
				}
				String inX = parm[0];
				String inY = parm[1];
				String inW = parm[2];
				String inH = parm[3];
				String id = parm[4];
				int x = 0;
				int y = 0;
				int w = 0;
				int h = 0;
				try {
					x = Integer.parseInt(inX);
					y = Integer.parseInt(inY);
					w = Math.round(Float.parseFloat(inW));
					h = Math.round(Float.parseFloat(inH));
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				float YMin = 0;
				float YMax = 0;
				float YStep = 0;
				int ActX = 0;
				int xCount = 0;
				List<String> XValue = new ArrayList<String>();
				List<String> YValue = new ArrayList<String>();
				List<String> compareYList = new ArrayList<String>();

				if (json == null || json.length() == 0) {
					Toast.makeText(mContext, "请先调用setData方法", Toast.LENGTH_LONG).show();
					return;
				}
				try {
					JSONObject jsonObject = new JSONObject(json);
					JSONObject yObject = jsonObject.getJSONObject("y");
					YMin = Float.valueOf(yObject.getString("min"));
					YMax = Float.valueOf(yObject.getString("max"));
					YStep = Float.valueOf(yObject.getString("step"));
					int temp = 0;
					while (YMax > YMin + YStep * temp) {
						temp++;
					}
					YMax = YMin + YStep * temp;

					xCount = Integer.valueOf(jsonObject.getString("xCount"));
					ActX = Integer.valueOf(jsonObject.getString("actx"));
					JSONArray xArray = jsonObject.getJSONArray("x");
					int sizeX = xArray.length();
					for (int i = 0; i < sizeX; i++) {
						XValue.add(xArray.getString(i));
					}
					JSONArray yArray = jsonObject.getJSONArray("data");
					int sizeY = yArray.length();
					for (int i = 0; i < sizeY; i++) {
						YValue.add(yArray.getString(i));
					}
					JSONArray compareYArray = jsonObject
							.getJSONArray("compareY");
					int compareYSize = compareYArray.length();
					for (int i = 0; i < compareYSize; i++) {
						JSONObject object = compareYArray.getJSONObject(i);
						String s = object.getString("s");
						String e = object.getString("e");
						String v = object.getString("v");
						compareYList.add(s + "," + e + "," + v);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

				int myViewID = EUExUtil.getResLayoutID("plugin_linechart_layout");
				if (myViewID <= 0) {
					Toast.makeText(mContext, "找不到名为:plugin_linechart_layout的layout文件!", Toast.LENGTH_LONG).show();
					return;
				}
				
				ViewGroup mMyView = (ViewGroup) View.inflate(mContext,myViewID, null);
				MySCView msview = (MySCView) mMyView.findViewById(EUExUtil.getResIdID("plugin_linechart_msview"));
				ImageView YimageView = (ImageView) mMyView.findViewById(EUExUtil.getResIdID("plugin_linechart_y_axis_view"));
				
				msview.setData(mContext, w, h, density, YMin, YMax, YStep, ActX, xCount, XValue, YValue, compareYList);
				Bitmap yBitmap = creatYAxisBitmap(density, h, Ygap, YMin, YMax, YStep);
				yViews.put(id, yBitmap);
				YimageView.setBackgroundDrawable(new BitmapDrawable(yBitmap));
				
				LayoutParams lparm = new LayoutParams((int)w, (int)h);
				lparm.leftMargin = (int)x;
				lparm.topMargin = (int)y;
				views.put(id, mMyView);
				addView2CurrentWindow(mMyView, lparm);
			}
		});

	}

	private void addView2CurrentWindow(View child,
			RelativeLayout.LayoutParams parms) {
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.NO_GRAVITY;
		lp.leftMargin = l;
		lp.topMargin = t;
		adptLayoutParams(parms, lp);
		mBrwView.addViewToCurrentWindow(child, lp);
	}
	
	public void close(final String[] parm) {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (parm.length < 0) {
					return;
				}
				String id = parm[0];

				ViewGroup mMyView = views.get(id);
				if (null != mMyView) {
					views.remove(id);
					mMyView.removeAllViews();
					removeViewFromCurrentWindow(mMyView);
				}

				Bitmap yBitmap = yViews.get(id);
				myRecycle(yBitmap);
				yViews.remove(id);
			}
		});
	}

	private void myRecycle(Bitmap bitmap) {
		try {
			synchronized (bitmap) {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected boolean clean() {
		((Activity) mContext).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Set<String> s = views.keySet();
				for (String key : s) {
					ViewGroup mMyView = views.get(key);
					if (null != mMyView) {
						mMyView.removeAllViews();
						removeViewFromCurrentWindow(mMyView);
					}
				}
				views.clear();

				Set<String> yS = yViews.keySet();
				for (String key : yS) {
					Bitmap mMyView = yViews.get(key);
					myRecycle(mMyView);
				}
				yViews.clear();
			}
		});
		return true;
	}

	private Bitmap creatYAxisBitmap(float density, int h, int w, float yMin,
			float yMax, float stap) {
		float allFrameHeight = h * density;
		w = (int) (w * density);
		float frameHeight = allFrameHeight - margin * 2 * density - Xgap * density;
		float cellHeight = ((float) frameHeight / ((float) (yMax - yMin) / (float) stap));

		h = (int) ((h - Xgap) * density);
		Bitmap bitmap = Bitmap.createBitmap(w, (int) allFrameHeight, Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);

		int size = (int) ((float) (yMax - yMin) / (float) stap) + 1;

		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(35 * density);
		paint.setTextAlign(Paint.Align.CENTER);
		for (int i = 0; i < size; i++) {
			float y = h - i * cellHeight;
			String content;
			if (stap < 1) {
				content = yMin + i * stap + "";
			} else {
				content = (int) (yMin + i * stap) + "";
			}
			canvas.drawText(content, w / 2, y, paint);
		}
		return bitmap;
	}
}
