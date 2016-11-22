package in.rkvsraman.indicocr.utils;

import java.awt.Rectangle;

public class RectangleUtils {

	/**
	  * Return a largest Rectangle that will fit in a rotated image
	  * @param imgWidth Width of image
	  * @param imgHeight Height of Image
	  * @param rotAngDeg Rotation angle in degrees
	  * @param type 0 = Largest Area, 1 = Smallest Area, 2 = Widest, 3 = Tallest
	  * @return
	  */
	public static Rectangle getLargestRectangle(double imageWidth, double imageHeight, double rotAngDeg, int type) {
		  Rectangle rect = null;
		   
		 
		  double imgWidth = imageWidth;
		  double imgHeight = imageHeight;
		   
		 
		   
		  double rotateAngle = rotAngDeg;
		  double sinRotAng = Math.sin(rotateAngle);
		  double cosRotAng = Math.cos(rotateAngle);
		  double tanRotAng = Math.tan(rotateAngle);
		  // Point 1 of rotated rectangle
		  double x1 = sinRotAng * imgHeight;
		  double y1 = 0;
		  // Point 2 of rotated rectangle
		  double x2 = cosRotAng * imgWidth + x1;
		  double y2 = sinRotAng * imgWidth;
		  // Point 3 of rotated rectangle
		  double x3 = x2 - x1;
		  double y3 = y2 + cosRotAng * imgHeight;
		  // Point 4 of rotated rectangle
		  double x4 = 0;
		  double y4 = y3 - y2;
		  // MidPoint of rotated image
		  double midx = x2 / 2;
		  double midy = y3 / 2;
		   
		  // Angle for new rectangle (based on image width and height)
		  double imgAngle = Math.atan(imgHeight / imgWidth);
		  double imgRotAngle = Math.atan(imgWidth / imgHeight);
		  double tanImgAng = Math.tan(imgAngle);
		  double tanImgRotAng = Math.tan(imgRotAngle);
		  // X Point for new rectangle on bottom line
		  double ibx1 = midy / tanImgAng + midx;
		  double ibx2 = midy * tanImgAng + midx;
		   
		  // First intersecting lines
		  // y = ax + b  ,  y = cx + d  ==>  x = (d - b) / (a - c)
		  double a = y2 / x3;
		  double b = tanRotAng * -x1;
		  double c = -imgHeight / imgWidth;
		  double d = tanImgAng * ibx1;
		   
		  // Intersecting point 1
		  double ix1 = (d - b) / (a - c);
		  double iy1 = a * ix1 + b;
		   
		  // Second intersecting lines
		  c = -imgWidth / imgHeight;
		  d = tanImgRotAng * ibx2;
		   
		  // Intersecting point 2
		  double ix2 = (d - b) / (a - c);
		  double iy2 = a * ix2 + b;
		   
		  // Work out smallest rectangle
		  double radx1 = Math.abs(midx - ix1);
		  double rady1 = Math.abs(midy - iy1);
		  double radx2 = Math.abs(midx - ix2);
		  double rady2 = Math.abs(midy - iy2);
		  // Work out area of rectangles
		  double area1 = radx1 * rady1;
		  double area2 = radx2 * rady2;
		  // Rectangle (x,y,width,height)
		  Rectangle rect1 = new Rectangle((int)Math.round(midx-radx1),(int)Math.round(midy-rady1),
		    (int)Math.round(radx1*2),(int)Math.round(rady1*2));
		   
		  // Rectangle (x,y,width,height)
		  Rectangle rect2 = new Rectangle((int)Math.round(midx-radx2),(int)Math.round(midy-rady2),
		    (int)Math.round(radx2*2),(int)Math.round(rady2*2));
		   
		  switch (type) {
		   case 0: rect = (area1 > area2 ? rect1 : rect2); break;
		   case 1: rect = (area1 < area2 ? rect1 : rect2); break;
		   case 2: rect = (radx1 > radx2 ? rect1 : rect2); break;
		   case 3: rect = (rady1 > rady2 ? rect1 : rect2); break;
		  }
		   
		  return rect;
		 }
}
