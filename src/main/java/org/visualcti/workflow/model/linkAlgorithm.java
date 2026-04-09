/*
##############################################################################
##
##  DO NOT REMOVE THIS LICENSE AND COPYRIGHT NOTICE FOR ANY REASON
##
##############################################################################

GNU VisualCTI - A Java multi-platform Computer Telephony Application Server
Copyright (C) 2002 by Oleg Sopilnyak.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Contact oleg.sopilnyak@gmail.com or gennady@visualcti.org for more information.

Ukraine point of contact: Oleg Sopilnyak - oleg.sopilnyak@gmail.com
Home Phone:	+380-63-8420220 (russian)

USA point of contact: Justin Kuntz - jkuntz@prominic.com
Prominic Technologies, Inc.
PO Box 3233
Champaign, IL 61826-3233
Fax number: 217-356-3356
##############################################################################

*/
package org.visualcti.workflow.model;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.lang.reflect.*;
/**
<library>
The library of algorithms for drawing the Link
*/
public class linkAlgorithm
{
    // The algorithms of the far distance
    /**
    <algorithm's ID>
    from left to right (zigzag)
    */
    public static final int L2R = 0;
    /**
    <algorithm's ID>
    from right to left (zigzag)
    */
    public static final int R2L = 1;
    /**
    <algorithm's ID>
    from top to bottom (zigzag)
    */
    public static final int T2B = 2;
    /**
    <algorithm's ID>
    from bottom to top (zigzag)
    */
    public static final int B2T = 3;
    // this algorithms will use both, near & far distances
    // left side
    /**
    <algorithm's ID>
    Upwards and to the left (corner)
    */
    public static final int U2L = 4;
    /**
    <algorithm's ID>
    To the left and upwards (corner)
    */
    public static final int L2U = 5;
    /**
    <algorithm's ID>
    Downwards and to the left (corner)
    */
    public static final int D2L = 6;
    /**
    <algorithm's ID>
    To the left and downwards (corner)
    */
    public static final int L2D = 7;
    // right side
    /**
    <algorithm's ID>
    Upwards and to the right (corner)
    */
    public static final int U2R = 8;
    /**
    <algorithm's ID>
    To the right and upwards (corner)
    */
    public static final int R2U = 9;
    /**
    <algorithm's ID>
    Downwards and to the right (corner)
    */
    public static final int D2R = 10;
    /**
    <algorithm's ID>
    To the right and downwards (corner)
    */
    public static final int R2D = 11;
    /**
    <accessor>
    to get access to algorithm's name by ID
    */
    public static final String getAlgorithmName(int ID)
    {
        try {return algName[ID];
        }catch(ArrayIndexOutOfBoundsException e){}
        return "???";
    }
    /**
    <accessor>
    to get access to algorithm's method by ID
    */
    public static final Method getAlgorithm(int ID)
    {
        try {return algMethod[ID];
        }catch(ArrayIndexOutOfBoundsException e){}
        return null;
    }

    // Bodies of algorithms
    /**
    <painter>
    drawing simple line between points
    */
    public static final void simple(Graphics g,Point from,Point to,Color color)
    {
        Color current = g.getColor();

        Graphics2D g2 = (Graphics2D)g;

        from = (Point)from.clone(); to = (Point)to.clone();
        //Polygon link = new Polygon();
        //link.addPoint(from.x,from.y);
        //link.addPoint(to.x,to.y);

//System.out.println("Draw line from "+from+" to "+to);
//System.out.println("Draw line "+link);
        g.setColor(color);
//        g.drawPolygon(link);
//        g2.draw(toGeneralPath(link));
//        link.translate(-1,-1);
        g.drawLine(from.x, from.y, to.x, to.y);
        // to draw shadow
        g.setColor(Color.lightGray);
//        g.drawPolygon(link);
//        g2.draw(toGeneralPath(link));
//        link.translate(2,2);
//        g.drawPolygon(link);
//        g2.draw(toGeneralPath(link));

        if (from.y < to.y) {
          from.translate(1,-1); to.translate(1,-1);
//System.out.println("Draw shadow1 from "+from+" to "+to);
          //g.drawLine(from.x, from.y, to.x, to.y);
          from.translate(-2,2); to.translate(-2,2);
//System.out.println("Draw shadow2 from "+from+" to "+to);
          g.drawLine(from.x, from.y, to.x, to.y);
        }else{
          from.translate(-1,-1); to.translate(-1,-1);
//System.out.println("Draw shadow1 from "+from+" to "+to);
          //g.drawLine(from.x, from.y, to.x, to.y);
          from.translate(2,2); to.translate(2,2);
//System.out.println("Draw shadow2 from "+from+" to "+to);
          g.drawLine(from.x, from.y, to.x, to.y);
        }
        /*
        from.translate(-1,-1); to.translate(-1,-1);
System.out.println("Draw line from "+from+" to "+to);
//System.out.println("Draw line "+link);
        g.setColor(color.red);
//        g.drawPolygon(link);
//        g2.draw(toGeneralPath(link));
//        link.translate(-1,-1);
        g.drawLine(from.x, from.y, to.x, to.y);
        */
//System.out.println("-------------------");
        g.setColor(current);
    }
    /**
    <painter>
    drawing zigzag from left to right
    */
    public static void L2R(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.zigzag(g,from,to,color,false);
    }
    /**
    <painter>
    drawing zigzag from right to left
    */
    public static void R2L(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.zigzag(g,from,to,color,false);
    }
    /**
    <painter>
    drawing zigzag from top to bottom
    */
    public static void T2B(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.zigzag(g,from,to,color,true);
    }
    /**
    <painter>
    drawing zigzag from bottom to top
    */
    public static void B2T(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.zigzag(g,from,to,color,true);
    }
/**
<corner-type>
Corner at the left below
*/
private static final int CornerLD = 100;
/**
<corner-type>
Corner at the left above
*/
private static final int CornerLU = 200;
/**
<corner-type>
Corner at the right below
*/
private static final int CornerRD = 300;
/**
<corner-type>
Corner at the right above
*/
private static final int CornerRU = 400;
    /**
    <painter>
    Drawing a corner upwards and to the left
    */
    public static void U2L(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerRU);
    }
    /**
    <painter>
    Drawing a corner to the left and upwards
    */
    public static void L2U(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerRD);
    }
    /**
    <painter>
    Draw a corner downwards and to the left
    */
    public static void D2L(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerRD);
    }
    /**
    <painter>
    Draw a corner to the left and downwards
    */
    public static void L2D(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerLU);
    }
    // right side
    /**
    <painter>
    Upwards and to the right (corner)
    */
    public static void U2R(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerLU);
    }
    /**
    <painter>
    To the right and upwards (corner)
    */
    public static void R2U(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerRD);
    }
    /**
    <painter>
    Downwards and to the right (corner)
    */
    public static void D2R(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerLD);
    }
    /**
    <painter>
    To the right and downwards (corner)
    */
    public static void R2D(Graphics g,Point from,Point to,Color color){
        linkAlgorithm.corner(g,from,to,color,CornerRU);
    }
    /**
    <painter>
    corner's algorithm
    */
    private static void corner
                    (
                    Graphics g,
                    Point from,
                    Point to,
                    Color color,
                    int type
                    )
    {
        boolean isVertical = true;
        Point corner = new Point();
        if (from.x > to.x)
        {
            corner(g,to,from,color,type);
            return;
        }
        switch( type )
        {
            case CornerLD:
                corner.x = from.x;
                corner.y = to.y;
                System.out.println("CornerLD....");
                break;
            case CornerLU:
                corner.x = from.x;
                corner.y = to.y;
                System.out.println("CornerLU....");
                break;
            case CornerRD:
                corner.x = to.x;
                corner.y = from.y;
                System.out.println("CornerRD....");
                break;
            case CornerRU:
                corner.x = to.x;
                corner.y = from.y;
                System.out.println("CornerRU....");
                break;
        }
        Polygon link = new Polygon();
        link.addPoint(from.x, from.y);
        link.addPoint(corner.x, corner.y);
        link.addPoint(to.x, to.y);
        drawCorner(g, link,color,from.x > corner.x);
    }
    /**
    <painter>
    zigzag's algorithm
    */
    private static void zigzag
                    (
                    Graphics g,
                    Point from,
                    Point to,
                    Color color,
                    boolean isVertical
                    )
    {
        Color current = g.getColor();
        Polygon link = makeZigZag(from,to,isVertical);
        drawZigZag(g, link, color, isVertical);
        g.setColor(current);
    }
    /**
    <producer>
    to make zigzag polygon from coordinates
    */
    private static final Polygon makeZigZag(Point from,Point to,boolean isVertical)
    {
        Polygon zigzag = new Polygon();
        Point node1,node2;int middle;

        if ( isVertical ){
            middle = ( from.y + to.y ) / 2;
            node1 = new Point( from.x, middle );
            node2 = new Point( to.x, middle );
        }else {
            middle = ( from.x + to.x ) / 2;
            node1 = new Point( middle, from.y );
            node2 = new Point( middle, to.y );
        }
        zigzag.addPoint(from.x, from.y);
        zigzag.addPoint(node1.x, node1.y);
        zigzag.addPoint(node2.x, node2.y);
        zigzag.addPoint(to.x, to.y);
        /*
        zigzag.moveTo( (float)from.getX(), (float)from.getY() );
        zigzag.lineTo( (float)node1.getX(), (float)node1.getY() );
        zigzag.lineTo( (float)node2.getX(), (float)node2.getY() );
        zigzag.lineTo( (float)to.getX(), (float)to.getY() );
        */
        return zigzag;
    }
    /**
    <producer>
    To make GeneralPath from a polygon
    */
    private static GeneralPath toGeneralPath(Polygon p)
    {
        int count = p.npoints;
        GeneralPath  path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,count);
        path.moveTo(p.xpoints[ 0 ], p.ypoints[ 0 ]);
        for(int i=1;i < count;i++) path.lineTo(p.xpoints[ i ], p.ypoints[ i ]);
        return path;
    }
// unknown direction
private static final int NoDir = -1;
// to right direction
private static final int DirR = 1;
// to down direction
private static final int DirD = 2;
// to left direction
private static final int DirL = 3;
// to up direction
private static final int DirU = 4;
    /**
    <producer>
    To make the Polygon below main Polygon
    */
    private static Polygon below(Polygon main, int delta)
    {
System.out.println("begin ************** delta="+delta);
        Polygon below = new Polygon();
        Point current = new Point(), next = new Point();
        int max = main.npoints-1;
        int direction = NoDir;
        for(int i = 0;i <= max;i++)
        {
            current.x = main.xpoints[i];
            current.y = main.ypoints[i];
            if (i == max) {
                next = null;
            } else {
                next.x = main.xpoints[i+1];
                next.y = main.ypoints[i+1];
            }
            direction = addBelow(below,current,next,delta,direction);
        }
System.out.println("end **************");
        return below;
    }
    private static int addBelow(Polygon target,Point current, Point next,int delta,int lastDirection)
    {
        int direction = NoDir,x = 0,y = 0;
        if ( next != null )
        {
                if ( current.x == next.x)
                    direction = current.y > next.y ? DirU:DirD;
                else
                if ( current.y == next.y)
                    direction = current.x > next.x ? DirL:DirR;
        }
        switch( direction )
        {
        case DirR: // to right
System.out.print("DirR");
            if (lastDirection == DirU)
                x = current.x-delta;
            else
                x = current.x+delta;
            y = current.y+delta;
            if (lastDirection == NoDir) x = current.x;
            break;
        case DirL: // to left
System.out.print("DirL");
            if (lastDirection == DirU)
                x = current.x-delta;
            else
                x = current.x+delta;
            y = current.y+delta;
            if (lastDirection == NoDir) x = current.x;
            break;
        case DirD: // to down
System.out.print("DirD");
            if (lastDirection == DirR)
                x = current.x+delta;
            else
                x = current.x-delta;
            y = current.y+delta;
            if (lastDirection == NoDir) y = current.y;
            break;
        case DirU: // to up
System.out.print("DirU");
            x = current.x+delta;
            y = current.y+delta;
            if (lastDirection == NoDir) y = current.y;
            break;
        case NoDir: // no direction
System.out.print("NoDir");
            switch(lastDirection)
            {
            case DirR: // to right
                x = current.x;
                y = current.y+delta;
                break;
            case DirL: // to left
                x = current.x;
                y = current.y+delta;
                break;
            case DirD: // to down
                x = current.x+delta;
                y = current.y;
                break;
            case DirU: // to up
                x = current.x+delta;
                y = current.y;
                break;
            }
            break;
        }
System.out.println(" added point(x:y) "+x+":"+y);
        target.addPoint(x, y);
        return direction;
    }
    private static int direction(Point curr,Point next)
    {
        if ( next == null) return NoDir;
        int direction = NoDir;
        if ( curr.x == next.x)
            direction = curr.y > next.y ? DirU:DirD;
        else
        if ( curr.y == next.y)
            direction = curr.x > next.x ? DirL:DirR;
        return direction;
    }
    /**
    <producer>
    To make below polygon, using turtle's algorithm
    */
    private static Polygon belowTurtle(Polygon link, int delta)
    {
        int direction = NoDir, max = link.npoints-1;
        Polygon result = new Polygon();
        Point prev = new Point(),curr=new Point(),next=new Point();
        curr.x = link.xpoints[0];
        curr.y = link.ypoints[0];
        try {next.x = link.xpoints[1];
             next.y = link.ypoints[1];
        }catch(ArrayIndexOutOfBoundsException e){
            next = null;
        }
        direction = belowFirst(result,prev,curr,next,delta);
        for (int i = 1;i <= max;i++)
        {
            curr.x = link.xpoints[ i ];
            curr.y = link.ypoints[ i ];
            try {next.x = link.xpoints[i+1];
                next.y = link.ypoints[i+1];
            }catch(ArrayIndexOutOfBoundsException e){
                next = null;
            }
            direction = goBelowTurtle(result,prev,curr,next,delta,direction);
        }
        return result;
    }
    private static int belowFirst
                            (
                            Polygon turtle,
                            Point prev,
                            Point curr,
                            Point next,
                            int delta
                            )
    {
        if ( next == null ) return NoDir;
        int direction = direction(curr,next);
        switch( direction ){
            case DirR:
                prev.x = curr.x;
                prev.y = curr.y-delta;
                break;
            case DirL:
                prev.x = curr.x;
                prev.y = curr.y-delta;
                break;
            case DirD:
                prev.x = curr.x - delta;
                prev.y = curr.y;
                break;
            case DirU:
                prev.x = curr.x - delta;
                prev.y = curr.y;
                break;
            default:
                prev = null;
                break;
        }
        if (prev != null) turtle.addPoint(prev.x, prev.y);
        return direction;
    }
    private static int goBelowTurtle
                            (
                            Polygon turtle,
                            Point prev,
                            Point curr,
                            Point next,
                            int delta,
                            int oldDirection
                            )
    {
        int direction = direction(curr,next);
        switch( direction ){
            case DirR:
System.out.print("Dir R ");
                if (prev.y > curr.y) prev.x = curr.x+delta;
                /*
                if (oldDirection == DirU)
                    prev.x = curr.x+delta; else prev.x = curr.x-delta;
                    */
                //prev.y = curr.y+delta;
                break;
            case DirL:
System.out.print("Dir L ");
                /*
                if (oldDirection == DirU)
                    prev.x = curr.x-delta; else prev.x = curr.x+delta;
                    */
                prev.y = curr.y+delta;
                break;
            case DirD:
System.out.print("Dir D ");
                if (oldDirection == DirL)
                    prev.y = curr.y - delta; else prev.y = curr.y + delta;

                //prev.y = curr.y - delta;
                break;
            case DirU:
System.out.print("Dir U ");
                if (oldDirection == DirL)
                    prev.y = curr.y - delta; else prev.y = curr.y + delta;
                //prev.y = curr.y + delta;
                break;
            default:
System.out.print("No Dir ");
                switch(oldDirection)
                {
                case DirR: // to right
                    prev.x = curr.x;
                    prev.y = curr.y+delta;
                    break;
                case DirL: // to left
                    prev.x = curr.x;
                    prev.y = curr.y+delta;
                    break;
                case DirD: // to down
                    prev.x = curr.x+delta;
                    prev.y = curr.y;
                    break;
                case DirU: // to up
                    prev.x = curr.x+delta;
                    prev.y = curr.y;
                    break;
                }
                break;
        }
System.out.println(" added point(x:y) "+prev.x+":"+prev.y);
        turtle.addPoint(prev.x, prev.y);
        return direction;
    }
    private static void shadow(Graphics2D g,Polygon link, int delta)
    {
        Polygon below = new Polygon(link.xpoints,link.ypoints,link.npoints);
        for(int i=0;i < below.npoints;i++)
        {
            below.xpoints[i] += delta;
            below.ypoints[i] += delta;
        }
        //belowTurtle(link,delta);
        g.setColor(Color.blue);
        AffineTransform scale = AffineTransform.getScaleInstance(0.9, 0.9);
        //scale.tr
        g.draw( toGeneralPath(below) );
    }
    /**
    <painter>
    to draw link, like zigzag
    */
    private static void drawZigZag(Graphics gr,Polygon link, Color color,boolean isVertical)
    {
        Graphics2D g = (Graphics2D)gr;
        g.setColor(color); g.draw( toGeneralPath(link) );
         //if (true) return;
        // draw a shadow of link
        int first = 0; int last = link.npoints-1;int delta = 3;
        if (true) {shadow(g,link,delta);return;}
        boolean up2down = link.ypoints[first] < link.ypoints[last];
        int delta2 = up2down ? -delta:delta;
        Polygon
        // top shadow's part
        shadow = new Polygon(link.xpoints,link.ypoints,link.npoints);
        if ( !up2down )
            shadow.translate(-delta, -delta);
         else
            shadow.translate(delta, -delta);
        if ( isVertical ){
            if ( !up2down ) {
                shadow.ypoints[ first ] += delta2;
                shadow.ypoints[ last ] += delta2;
            }else {
                shadow.ypoints[ first ] -= delta2;
                shadow.ypoints[ last ] -= delta2;
            }
        }else {
            shadow.xpoints[ first ] += delta2;
            shadow.xpoints[ last ] += delta2;
        }
//        g.setColor(color.brighter());
        g.setColor(Color.lightGray);
//        g.setColor(Color.gray);
        g.setColor(Color.blue);
        g.draw( toGeneralPath(shadow) );
        // bottom shadow's part
        shadow = new Polygon(link.xpoints,link.ypoints,link.npoints);
        if ( !up2down )
            shadow.translate(delta, delta);
         else
            shadow.translate(-delta, delta);
        if ( isVertical ) {
            if ( !up2down ) {
                shadow.ypoints[ first ] -= delta2;
                shadow.ypoints[ last ] -= delta2;
            }else{
                shadow.ypoints[ first ] += delta2;
                shadow.ypoints[ last ] += delta2;
            }
        }else {
            shadow.xpoints[ first ] -= delta2;
            shadow.xpoints[ last ] -= delta2;
        }
//        g.setColor(color.darker());
//        g.setColor(Color.gray);
        g.setColor(Color.lightGray);
//        g.setColor(Color.green);
        g.draw( toGeneralPath(shadow) );
    }
    /**
    <painter>
    to draw link, like corner
    */
    private static void drawCorner(Graphics gr,Polygon link, Color color,boolean isLeftCorner)
    {
        Graphics2D g = (Graphics2D)gr;
        g.setColor(color); g.draw( toGeneralPath(link) );
         //if (true) return;
        // draw a shadow of link
        int first = 0; int last = link.npoints-1;int delta = 3;
        if (true) {shadow(g,link,delta);return;}
        boolean up2down = link.ypoints[first] < link.ypoints[last];
        int delta2 = isLeftCorner ? delta:-delta;
        Polygon
        // top shadow's part
        shadow = new Polygon(link.xpoints,link.ypoints,link.npoints);
        if ( !up2down ){
            shadow.translate(-delta, -delta);
            shadow.ypoints[ first ] += delta2;
            shadow.xpoints[ last ] += delta2;
         }else{
            shadow.translate(delta, -delta);
            shadow.xpoints[ last ] -= delta2;
            shadow.ypoints[ first ] += delta2;
         }
            /*
        if ( isVertical ){
            shadow.ypoints[ first ] -= delta2;
            shadow.ypoints[ last ] -= delta2;
        }else {
            shadow.xpoints[ first ] += delta2;
            shadow.xpoints[ last ] += delta2;
        }
        */
//        g.setColor(color.brighter());
        g.setColor(Color.lightGray);
//        g.setColor(Color.gray);
        g.setColor(Color.blue);
        g.draw( toGeneralPath(shadow) );
        //if ( true ) return;
        // bottom shadow's part
        shadow = new Polygon(link.xpoints,link.ypoints,link.npoints);
        if ( !up2down ){
            shadow.translate(delta, delta);
            shadow.ypoints[ first ] -= delta2;
            shadow.xpoints[ last ] -= delta;
         }else{
            shadow.translate(-delta, delta);
            shadow.xpoints[ last ] += delta2;
            shadow.ypoints[ first ] -= delta2;
         }
            /*
        if ( isVertical ) {
            shadow.ypoints[ first ] += delta2;
            shadow.ypoints[ last ] += delta2;
        }else {
            shadow.xpoints[ first ] -= delta2;
            shadow.xpoints[ last ] -= delta2;
        }
        */
//        g.setColor(color.darker());
//        g.setColor(Color.gray);
        g.setColor(Color.lightGray);
//        g.setColor(Color.green);
        g.draw( toGeneralPath(shadow) );
    }
/**
the array of names
*/
private final static String algName[] = new String[]
{
    "L2R",
    "R2L",
    "T2B",
    "B2T",
    "U2L",
    "L2U",
    "D2L",
    "L2D",
    "U2R",
    "R2U",
    "D2R",
    "R2D"
};
/**
the array of methods
*/
private final static Method algMethod[]= new Method[ algName.length ];
/**
<signature>
the signature of drawing algorithm's method
*/
private static final Class[] algSignature = new Class[]{Graphics.class,Point.class,Point.class,Color.class};
/**
<painter>
reference to default draw method
*/
public final static Method simpleAlg = algorithm("simple");
    static //The initialization of the pool of algorithms
    {
        Arrays.fill(algMethod, simpleAlg);
        for(int i=0;i < algMethod.length;i++) algMethod[i] = algorithm( algName[i] );
    }
    /**
    <accessor>
    to get reference to algorithm by name
    */
    private static Method algorithm(String name){
        try {return linkAlgorithm.class.getMethod(name, algSignature);
        } catch(NoSuchMethodException e){
        } catch(SecurityException e){
        }
        return simpleAlg;
    }
}
