import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.FileWriter;


public class OrientedBoundingBox
{
    public static void main(String[] args) throws IOException
    {        
        new MinOrientedBoundingBoxTestPanel();
    }
}

class MinOrientedBoundingBoxTestPanel
{
    private final List<Point2D> points;

    MinOrientedBoundingBoxTestPanel()
    {
        points = new ArrayList<Point2D>();

        points.add(new Point2D.Double(95.0,62.0));
        points.add(new Point2D.Double(79.0,50.0));
        points.add(new Point2D.Double(68.0,49.0));
        points.add(new Point2D.Double(31.0,74.0));
        points.add(new Point2D.Double(17.0,91.0));
        points.add(new Point2D.Double(9.0,113.0));
        points.add(new Point2D.Double(1.0,163.0));
        points.add(new Point2D.Double(0.0,220.0));
        points.add(new Point2D.Double(4.0,226.0));
        points.add(new Point2D.Double(54.0,249.0));
        points.add(new Point2D.Double(103.0,255.0));
        points.add(new Point2D.Double(130.0,244.0));
        points.add(new Point2D.Double(168.0,231.0));
        points.add(new Point2D.Double(190.0,215.0));
        points.add(new Point2D.Double(204.0,231.0));
        points.add(new Point2D.Double(219.0,215.0));
        points.add(new Point2D.Double(228.0,187.0));
        points.add(new Point2D.Double(222.0,152.0));
        points.add(new Point2D.Double(210.0,151.0));
        points.add(new Point2D.Double(200.0,160.0));
        points.add(new Point2D.Double(194.0,156.0));
        points.add(new Point2D.Double(197.0,140.0));
        points.add(new Point2D.Double(203.0,137.0));
        points.add(new Point2D.Double(192.0,134.0));
        points.add(new Point2D.Double(189.0,115.0));
        points.add(new Point2D.Double(190.0,109.0));
        points.add(new Point2D.Double(209.0,88.0));
        points.add(new Point2D.Double(207.0,82.0));
        points.add(new Point2D.Double(197.0,71.0));
        points.add(new Point2D.Double(179.0,63.0));
        points.add(new Point2D.Double(107.0,62.0));
        points.add(new Point2D.Double(100.0,65.9));

        List<Point2D> minObbCorners = 
            MinOrientedBoundingBoxComputer.computeCorners(points);


        // try{
            // FileWriter writer = new FileWriter("output.txt"); 
            for(Point2D point: minObbCorners) {
                System.out.println(point.getX()+" "+point.getY());
                // writer.write(point.toString());
            }
            // writer.close();
            // } catch(Exception err){
                // err.printStackTrace();
        // }
    }
}


class MinOrientedBoundingBoxComputer
{
    static List<Point2D> computeCorners(List<Point2D> points)
    {
        List<Point2D> convexHullPoints = 
            computeConvexHullPoints(points);
        int alignmentPointIndex = 
            computeAlignmentPointIndex(convexHullPoints);
        Rectangle2D r = computeAlignedBounds(
            convexHullPoints, alignmentPointIndex);

        List<Point2D> alignedCorners = new ArrayList<Point2D>();
        alignedCorners.add(new Point2D.Double(r.getMinX(), r.getMinY()));
        alignedCorners.add(new Point2D.Double(r.getMaxX(), r.getMinY()));
        alignedCorners.add(new Point2D.Double(r.getMaxX(), r.getMaxY()));
        alignedCorners.add(new Point2D.Double(r.getMinX(), r.getMaxY()));

        Point2D center = convexHullPoints.get(alignmentPointIndex);
        double angleRad = computeEdgeAngleRad(
            convexHullPoints, alignmentPointIndex);

        AffineTransform at = new AffineTransform();
        at.concatenate(
            AffineTransform.getTranslateInstance(
                center.getX(), center.getY()));
        at.concatenate(
            AffineTransform.getRotateInstance(angleRad));

        List<Point2D> corners = transform(alignedCorners, at);
        return corners;
    }

    private static int computeAlignmentPointIndex(
        List<Point2D> points)
    {
        double minArea = Double.MAX_VALUE;
        int minAreaIndex = -1;
        for (int i=0; i<points.size(); i++)
        {
            Rectangle2D r = computeAlignedBounds(points, i);
            double area = r.getWidth() * r.getHeight();

            if (area < minArea)
            {
                minArea = area;
                minAreaIndex = i;
            }
        }
        return minAreaIndex;
    }

    private static double computeEdgeAngleRad(
        List<Point2D> points, int index)
    {
        int i0 = index;
        int i1 = (i0+1)%points.size();
        Point2D p0 = points.get(i0);
        Point2D p1 = points.get(i1);
        double dx = p1.getX() - p0.getX();
        double dy = p1.getY() - p0.getY();
        double angleRad = Math.atan2(dy, dx);
        return angleRad;
    }

    private static Rectangle2D computeAlignedBounds(
        List<Point2D> points, int index)
    {
        Point2D p0 = points.get(index);
        double angleRad = computeEdgeAngleRad(points, index);
        AffineTransform at = createTransform(-angleRad, p0);
        List<Point2D> transformedPoints = transform(points, at);
        Rectangle2D bounds = computeBounds(transformedPoints);
        return bounds;
    }

    private static AffineTransform createTransform(
        double angleRad, Point2D center)
    {
        AffineTransform at = new AffineTransform();
        at.concatenate(
            AffineTransform.getRotateInstance(angleRad));
        at.concatenate(
            AffineTransform.getTranslateInstance(
                -center.getX(), -center.getY()));
        return at;
    }

    private static List<Point2D> transform(
        List<Point2D> points, AffineTransform at)
    {
        List<Point2D> result = new ArrayList<Point2D>();
        for (Point2D p : points)
        {
            Point2D tp = at.transform(p, null);
            result.add(tp);
        }
        return result;
    }


    private static Rectangle2D computeBounds(
        List<Point2D> points)
    {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Point2D p : points)
        {
            double x = p.getX();
            double y = p.getY();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        return new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
    }

    static Path2D createPath(List<Point2D> points)
    {
        Path2D path = new Path2D.Double();
        for (int i=0; i<points.size(); i++)
        {
            Point2D p = points.get(i);
            double x = p.getX();
            double y = p.getY();
            if (i == 0)
            {
                path.moveTo(x, y);
            }
            else
            {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }


    static List<Point2D> computeConvexHullPoints(List<Point2D> points)
    {
        // NOTE: Converting from Point2D to Point here
        // because the FastConvexHull class expects
        // the points with integer coordinates. 
        // This should be generalized to Point2D!
        ArrayList<Point> ps = new ArrayList<Point>();
        for (Point2D p : points)
        {
            ps.add(new Point((int)p.getX(), (int)p.getY()));
        }
        List<Point> convexHull = FastConvexHull.execute(ps);
        List<Point2D> result = new ArrayList<Point2D>();
        for (Point p : convexHull)
        {
            double x = p.getX();
            double y = p.getY();
            result.add(new Point2D.Double(x,y));
        }
        return result;
    }
}


class FastConvexHull
{
    public static ArrayList<Point> execute(ArrayList<Point> points)
    {
        ArrayList<Point> xSorted = (ArrayList<Point>) points.clone();
        Collections.sort(xSorted, new XCompare());

        int n = xSorted.size();

        Point[] lUpper = new Point[n];

        lUpper[0] = xSorted.get(0);
        lUpper[1] = xSorted.get(1);

        int lUpperSize = 2;

        for (int i = 2; i < n; i++)
        {
            lUpper[lUpperSize] = xSorted.get(i);
            lUpperSize++;

            while (lUpperSize > 2 &&
                !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2],
                    lUpper[lUpperSize - 1]))
            {
                // Remove the middle point of the three last
                lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
                lUpperSize--;
            }
        }

        Point[] lLower = new Point[n];

        lLower[0] = xSorted.get(n - 1);
        lLower[1] = xSorted.get(n - 2);

        int lLowerSize = 2;

        for (int i = n - 3; i >= 0; i--)
        {
            lLower[lLowerSize] = xSorted.get(i);
            lLowerSize++;

            while (lLowerSize > 2 &&
                !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2],
                    lLower[lLowerSize - 1]))
            {
                // Remove the middle point of the three last
                lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
                lLowerSize--;
            }
        }

        ArrayList<Point> result = new ArrayList<Point>();

        for (int i = 0; i < lUpperSize; i++)
        {
            result.add(lUpper[i]);
        }

        for (int i = 1; i < lLowerSize - 1; i++)
        {
            result.add(lLower[i]);
        }

        return result;
    }

    private static boolean rightTurn(Point a, Point b, Point c)
    {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) > 0;
    }

    private static class XCompare implements Comparator<Point>
    {
        @Override
        public int compare(Point o1, Point o2)
        {
            return (new Integer(o1.x)).compareTo(new Integer(o2.x));
        }
    }
}