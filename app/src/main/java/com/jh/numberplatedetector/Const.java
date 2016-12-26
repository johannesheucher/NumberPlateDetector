package com.jh.numberplatedetector;

public class Const {
	public static final int IMAGE_WIDTH = 600;
	
	public static final float NUMBER_PLATE_ASPECT_RATIO = 0.24f;//4.3f;
	public static final float NUMBER_PLATE_ASPECT_RATIO_EPSILON = 0.0417f;//0.75f;
	
	// originally for an image width of 800: {180, 300} x {30, 75}
	//			  for an image width of 600: {135, 225} x {22, 56}
	public static final int NUMBER_PLATE_MIN_WIDTH = (int)(IMAGE_WIDTH * 0.0367);
	public static final int NUMBER_PLATE_MAX_WIDTH = (int)(IMAGE_WIDTH * 0.09333);
	public static final int NUMBER_PLATE_MIN_HEIGHT = (int)(NUMBER_PLATE_MIN_WIDTH / (NUMBER_PLATE_ASPECT_RATIO + NUMBER_PLATE_ASPECT_RATIO_EPSILON));
	public static final int NUMBER_PLATE_MAX_HEIGHT = (int)(NUMBER_PLATE_MAX_WIDTH / (NUMBER_PLATE_ASPECT_RATIO - NUMBER_PLATE_ASPECT_RATIO_EPSILON));
	
	public static final float NUMBER_PLATE_WIDTH  = (NUMBER_PLATE_MAX_WIDTH + NUMBER_PLATE_MIN_WIDTH) / 2f;
	public static final float NUMBER_PLATE_HEIGHT = (NUMBER_PLATE_MAX_HEIGHT + NUMBER_PLATE_MIN_HEIGHT) / 2f;
	public static final float NUMBER_PLATE_WIDTH_HALFRANGE  = (NUMBER_PLATE_MAX_WIDTH - NUMBER_PLATE_MIN_WIDTH) / 2f;
	public static final float NUMBER_PLATE_HEIGHT_HALFRANGE = (NUMBER_PLATE_MAX_HEIGHT - NUMBER_PLATE_MIN_HEIGHT) / 2f;
}