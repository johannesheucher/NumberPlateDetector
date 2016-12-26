package com.jh.numberplatedetector;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class NumberPlateExtractor {
	
	private static final int GAUSS_KERNEL_SIZE = 7;
	
	
	public static void calculateCropOffset(Mat src, Range rowRange, Range colRange) {
		if (src.rows() > src.cols()) {
			// use center of the image (width = N * NUMBER_PLATE)
			int gapX = Math.max(src.cols() - Const.NUMBER_PLATE_MAX_WIDTH * 2, 0);
			colRange.start = gapX / 2;
			colRange.end = src.cols() - gapX / 2;
			
			// only use lower part of the source image
			rowRange.start = (int)(src.rows() * 0.4);
			rowRange.end   = (int)(src.rows() * 0.8);
		} else {
			int gapX = Math.max(src.rows() - Const.NUMBER_PLATE_MAX_WIDTH * 2, 0);
			rowRange.start = gapX / 2;
			rowRange.end = src.rows() - gapX / 2;
			
			colRange.start = (int)(src.cols() * 0.4);
			colRange.end   = (int)(src.cols() * 0.8);
		}
	}
	
	
	/**
	 * Extracts bounding region around found number plate or <strong>null</strong>
	 * @param src Must contain a gray mat
	 * @return Bounding region or <strong>null</strong>
	 */
	public static Rect extract(Mat src) {
		return extract(src, new Mat(), new Mat(), new ArrayList<MatOfPoint>());
	}
	
	
	/**
	 * Extracts bounding region around found number plate or <strong>null</strong>
	 * @param src Must contain a gray mat
	 * @param binary Output containing the thresholded binary image
	 * @param edges Output containing the edge image
	 * @param contours Output containing all found contours
	 * @return Bounding region or <strong>null</strong>
	 */
	public static Rect extract(Mat src, Mat binary, Mat edges, List<MatOfPoint> contours) {
		// blur
		Imgproc.GaussianBlur(src, binary, new Size(GAUSS_KERNEL_SIZE, GAUSS_KERNEL_SIZE), 1, 1);
		
		// binarize
		Imgproc.threshold(binary, binary, 130, 255, Imgproc.THRESH_BINARY);		// 11 matches
		//Imgproc.adaptiveThreshold(binary, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);		// 8 matches
		//Imgproc.threshold(binary, binary, 130, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);			// 10 matches
		
		// detect edges
		Imgproc.Canny(binary, edges, 50, 200, 3, true);
		
		// detect contours
	    Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
	    
		// filter for potential number plate contours
	    Rect minRect = null;
	    float minScore = Float.MAX_VALUE;
		for (int i = 0; i < contours.size(); i++) {
			Rect rect = Imgproc.boundingRect(contours.get(i));
			float score = NumberPlateExtractor.isNumberPlate(contours.get(i), rect);
			if (score <= 1 && score < minScore) {
				minScore = score;
				minRect = rect;
	    	}
		}
		
		return minRect;
	}
	
	
	public static void toGray(Mat src, Mat dst) {
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
	}
	
	
	public static void topHat(Mat src, Mat dst, Size kernelSize) {
		// morphological operation - Top Hat (difference between image and opening of the image)
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize);
		Imgproc.morphologyEx(src, dst, Imgproc.MORPH_TOPHAT, kernel);
	}
	
	
	/**
	 * Returns score for number plate match. [0..1] for a match. Float.MAX_VALUE for no match. The lower the score, the better the match.
	 * @param contour
	 * @param boundingRect
	 * @return Score
	 */
	private static float isNumberPlate(MatOfPoint contour, Rect boundingRect) {
		// rough check: size and aspect ratio
		// each error must be within [0..1] to be a valid error
		float widthError  = Math.abs(Const.NUMBER_PLATE_WIDTH  - boundingRect.width)  / Const.NUMBER_PLATE_WIDTH_HALFRANGE;
		float heightError = Math.abs(Const.NUMBER_PLATE_HEIGHT - boundingRect.height) / Const.NUMBER_PLATE_HEIGHT_HALFRANGE;
		float aspectError = Math.abs(Const.NUMBER_PLATE_ASPECT_RATIO - boundingRect.width / (float)boundingRect.height) / Const.NUMBER_PLATE_ASPECT_RATIO_EPSILON;
		
    	if (widthError <= 1 && heightError <= 1 && aspectError <= 1) {
    		// TODO: Better check could be to perform matchTemplate on the contour:
    		// 1. Build template as bounding box of contour
    		// 2. Match this template with the contour
    		// remark: Which line width is best?
    		// remark: Maybe, it is simpler to implement this on my own instead of using overheaded matchTemplate
    		
    		// more expensive check: shape by approximating contour with a polygon
    		MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
    		double perimeter = Imgproc.arcLength(contour2f, true);
    		MatOfPoint2f approx = new MatOfPoint2f();
    		Imgproc.approxPolyDP(contour2f, approx, 0.04 * perimeter, true);
    		
    		// expected number of points
    		final int MIN_NUM_POINTS = 2;
    		final int MAX_NUM_POINTS = 8;
    		final float MID_NUM_POINTS = (MAX_NUM_POINTS + MIN_NUM_POINTS) / 2f;
    		final float NUM_POINTS_HALFRANGE = (MAX_NUM_POINTS - MIN_NUM_POINTS) / 2f;
    		
    		float numPointsError = Math.abs(approx.rows() - MID_NUM_POINTS) / (float)(NUM_POINTS_HALFRANGE);
    		
    		if (numPointsError <= 1) {
    			return (widthError + heightError + aspectError + numPointsError) / 4;
    		}
    	}
		return Float.MAX_VALUE;
	}
}
