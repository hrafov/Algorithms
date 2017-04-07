import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

/**
 * 
 *
 * @author zhangyu
 * @date 2017.4.3
 */
public class KdTree
{
    private Node root;
    private int size;
    
    private static class Node
    {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node lb;        // the left/bottom subtree
        private Node rt;        // the right/top subtree
        private boolean isEvenLevel; 
        
        public Node(Point2D p, RectHV rect, boolean isEvenLevel)
        {
            this.p = p;
            this.rect = rect;
            this.isEvenLevel = isEvenLevel;
        }
     }
    
    /**
     * construct an empty set of points 
     */
    public KdTree() { }
    
    /**
     * is the set empty? 
     * 
     * @return
     */
    public boolean isEmpty()
    {
        return size == 0;
    }
    
    /**
     * number of points in the set 
     * 
     * @return
     */
    public int size()
    {
        return size;
    }
    
    /**
     * add the point to the set (if it is not already in the set)
     * 
     * @param p
     */
    public void insert(Point2D p)
    {
        if (p == null) throw new NullPointerException("Null point");
        
        root = insert(root, null, p, 0);
    }
    
    private Node insert(Node x, Node parent, Point2D p, int direction) 
    {
        if (x == null)
        {
            if (size++ == 0) return new Node(p, new RectHV(0, 0, 1, 1), true);
            
            RectHV rectOfX = parent.rect;
            
            if (direction < 0) 
            {
                if (parent.isEvenLevel)
                    rectOfX = new RectHV(parent.rect.xmin(), parent.rect.ymin(), 
                                         parent.p.x(),       parent.rect.ymax());
                else 
                    rectOfX = new RectHV(parent.rect.xmin(), parent.rect.ymin(), 
                                         parent.rect.xmax(), parent.p.y());
            }
            else if (direction > 0) 
            {
                if (parent.isEvenLevel)
                    rectOfX = new RectHV(parent.p.x(),       parent.rect.ymin(), 
                                         parent.rect.xmax(), parent.rect.ymax());
                else
                    rectOfX = new RectHV(parent.rect.xmin(), parent.p.y(), 
                                         parent.rect.xmax(), parent.rect.ymax());
            }
            return new Node(p, rectOfX, !parent.isEvenLevel);
        }
        
        int cmp = compare(p, x.p, x.isEvenLevel);

        if      (cmp < 0) x.lb = insert(x.lb, x, p, cmp);
        else if (cmp > 0) x.rt = insert(x.rt, x, p, cmp);
        return x;
}
    
    private int compare(Point2D p, Point2D q, boolean isEvenLevel) 
    {
        if (p == null || q == null) throw new NullPointerException("Null point");
        if (p.equals(q)) return 0;
        if (isEvenLevel) return p.x() < q.x() ? -1 : 1;
        else             return p.y() < q.y() ? -1 : 1;
    }
    
    /**
     * does the set contain point p? 
     * 
     * @param p
     * @return
     */
    public boolean contains(Point2D p)
    {
        if (p == null) throw new NullPointerException("Null point");
        
        return contains(root, p);
    }
    
    private boolean contains(Node x, Point2D p) 
    {
        if (x == null) return false;
        
        int cmp = compare(p, x.p, x.isEvenLevel);
        
        if      (cmp < 0) return contains(x.lb, p);
        else if (cmp > 0) return contains(x.rt, p);
        else              return true;
    }
    
    /**
     * draw all points to standard draw 
     * 
     */
    public void draw()
    {
        draw(root);
    }

    private void draw(Node x)
    {
        if (x == null) return; 
        draw(x.lb);
        draw(x.rt);
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
        x.p.draw();
        StdDraw.setPenRadius();
        if (x.isEvenLevel) 
        {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(x.p.x(), x.rect.ymin(), x.p.x(), x.rect.ymax());   
        }
        else
        {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.line(x.rect.xmin(), x.p.y(), x.rect.xmax(), x.p.y());   
        }
    } 
    
    /**
     * all points that are inside the rectangle 
     * 
     * @param rect
     * @return
     */
    public Iterable<Point2D> range(RectHV rect)
    {
        if (rect == null) throw new NullPointerException("Null rectangle");
        
        Queue<Point2D> pointQueue = new Queue<Point2D>();
        
        range(root, pointQueue, rect);
        return pointQueue;
    }
    
    private void range(Node x, Queue<Point2D> pointQueue, RectHV rect) 
    { 
        if (x == null) return; 
        if (rect.contains(x.p)) pointQueue.enqueue(x.p);
        if (x.lb != null && rect.intersects(x.lb.rect)) range(x.lb, pointQueue, rect);
        if (x.rt != null && rect.intersects(x.rt.rect)) range(x.rt, pointQueue, rect);
    } 
    
    /**
     * a nearest neighbor in the set to point p; null if the set is empty  
     * 
     * @param p
     * @return
     */
    public Point2D nearest(Point2D p)
    {
        if (p == null) throw new NullPointerException("Null point");
        if (root == null) return null;
        return nearest(root, root.p, p);
    }
    
    private Point2D nearest(Node x, Point2D nearest, Point2D p)
    {
        if (x == null) return nearest;
        
        Point2D point = nearest;
        int cmp = compare(p, x.p, x.isEvenLevel);
        
        if (p.distanceSquaredTo(x.p) < p.distanceSquaredTo(point)) point = x.p;
        if (cmp < 0)
        {
            point = nearest(x.lb, point, p);
            if (x.rt != null)
                if (point.distanceSquaredTo(p) > x.rt.rect.distanceSquaredTo(p))
                    point = nearest(x.rt, point, p);
        }
        else if (cmp > 0)
        {
            point = nearest(x.rt, point, p);
            if (x.lb != null)
                if (point.distanceSquaredTo(p) > x.lb.rect.distanceSquaredTo(p))
                    point = nearest(x.lb, point, p);
        }
        return point;
    }
    
    /**
     * unit testing of the methods (optional) 
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        String filename = args[0];
        In in = new In(filename);

        StdDraw.enableDoubleBuffering();

        // initialize the two data structures with point from standard input
        KdTree kdtree = new KdTree();
        while (!in.isEmpty()) 
        {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdtree.insert(p);
            kdtree.draw();
            StdDraw.show();
        }
        while (true)
        {
            if (StdDraw.mousePressed()) 
            {
                double x = StdDraw.mouseX();
                double y = StdDraw.mouseY();
                StdOut.printf("%8.6f %8.6f\n", x, y);
                Point2D p = new Point2D(x, y);
                p.draw();
                p.drawTo(kdtree.nearest(p));
                StdDraw.show();
            }
            StdDraw.pause(50);
        }
    }
}
