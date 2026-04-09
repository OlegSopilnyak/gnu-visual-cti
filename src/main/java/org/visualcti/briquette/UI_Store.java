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
package org.visualcti.briquette;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import org.jdom.*;
/**
Producer of briquette Operations UI
*/
public final class UI_Store
{
/**
 * to add the title to the editors's container
 * */
public static final JTextField addTitle(String theTitleText,JPanel container)
{
  JTextField title = new JTextField(theTitleText);
  title.setEditable(false); title.setBorder(null);
  title.setHorizontalAlignment(SwingConstants.CENTER);
  title.setFont(UI.titleFont);
  title.setForeground(UI.titleColor);
  container.add( title );
  return title;
}
/**
 * to add the title to the editors's container
 * */
public static final JTextField addTitle(String theTitleText,JPanel container,String place)
{
  JTextField title = new JTextField(theTitleText);
  title.setEditable(false); title.setBorder(null);
  title.setHorizontalAlignment(SwingConstants.CENTER);
  title.setFont(UI.titleFont);
  title.setForeground(UI.titleColor);
  container.add( title, place );
  return title;
}
/**
 * <store>
 * The pool of Icons
 * */
private static final HashMap theIcons = new HashMap();
/**
 * <store>
 * The store of UI classes
 * */
private static final HashMap store = new HashMap();
/**
 * <store>
 * The store of UI icons
 * */
private static final HashMap icon = new HashMap();
    /**
    <icon> class for store operation's icon
    */
    private final static class Icon
    {
        Icon(String path){this.iconPath=path;}
        Icon(ImageIcon icon){this.icon=icon;}
        ImageIcon icon=null;
        String iconPath=null;
    }
/**
<icon>
The default Icon of operation
*/
private static ImageIcon defaultIcon = makeIcon(UI_Store.class,"/icon/briquette32.gif");
/**
 * The store of the cursors by class
 * */
private static final HashMap cursors = new HashMap(15);
    /**
     * <accessor>
     * To get access to class's Cursor
     * */
    final public static Cursor getCursor(Class operClass)
    {
      return UI_Store.getCursor( operClass.getName() );
    }
    /**
    <accessor>
    To get acccess to operation's Cursor
    */
    final public static Cursor getCursor(Operation oper)
    {
      return UI_Store.getCursor( oper.getClass() );
    }
    /**
     * <accessor>
     * To get access to Cursor by class's name
     * */
    final public static Cursor getCursor(String className)
    {
      synchronized(UI_Store.cursors)
      {
          Cursor result = (Cursor)UI_Store.cursors.get(className);
          if (result == null)
          {
            ImageIcon icon = UI_Store.getIcon( className );
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            Dimension curSize =
                java.awt.Toolkit.
                getDefaultToolkit().
                getBestCursorSize(width,height);
            BufferedImage cursorImage =
                UI_Store.createTransparencyImage(curSize);
            Graphics2D canvas = cursorImage.createGraphics();
            canvas.setColor(Color.black);
            // to draw the pointer
            canvas.drawRect(0,0,1,1);
            // to draw cursor's Icon
            canvas.drawImage(icon.getImage(),1,1,UI_Store.oserver );
            // end of painting
            canvas.dispose();
            Point spot = new Point(0,0);
            result =
                java.awt.Toolkit.
                getDefaultToolkit().
                createCustomCursor(cursorImage,spot,className);
            UI_Store.cursors.put( className, result );
          }
          return result;
      }
    }
private final static ImageObserver oserver = new JButton();
private final static GraphicsConfiguration gc =
        GraphicsEnvironment.
        getLocalGraphicsEnvironment().
        getDefaultScreenDevice().
        getDefaultConfiguration();

    private static final BufferedImage createTransparencyImage(Dimension size){
      /*
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      */
      return gc.createCompatibleImage
          (
          size.width,
          size.height,
          Transparency.TRANSLUCENT
          );
    }
    /**
    <accessor>
    To get acccess to operation's Icon
    */
    final public static ImageIcon getIcon(Operation oper){
        return UI_Store.getIcon(oper.getClass());
    }
    /**
    <accessor>
    To get acccess to the Icon for name
    */
    final public static ImageIcon getIcon(Class ownerClass){
        return UI_Store.getIcon( ownerClass.getName() );
    }
    /**
    <accessor>
    To get acccess to the Icon for name
    */
    final public static ImageIcon getIcon(String className)
    {
        ImageIcon operIcon = UI_Store.defaultIcon;
        // to get Icon's instance from store
        synchronized( store )
        {
            Icon oIcon = (Icon)UI_Store.icon.get( className );
            if (oIcon != null) operIcon = oIcon.icon;
        }
        return operIcon;
    }
    /**
     * To register the Icon for class
     * */
    final public static ImageIcon register(String className,String iconPath, ImageIcon bydefault)
    {
      Icon iconData = new Icon(iconPath);
      iconData.icon = makeIcon(UI_Store.class,iconPath);
      if (iconData.icon == null) iconData.icon=bydefault;
      UI_Store.icon.put(className,iconData);
      return iconData.icon;
    }
    /**
     * <producer>
     * To get Image by url
     * */
    final public static ImageIcon makeIcon(Class owner,String URL)
    {
        ImageIcon icon = null;
        String key = owner.getClassLoader().getClass().getName()+"/"+URL;
        synchronized(theIcons)
        {
          icon = (ImageIcon)theIcons.get(key);
          if (icon != null) return icon;
          try {icon = new ImageIcon( owner.getResource(URL) );
          }catch(Exception e){}
          if (icon != null) theIcons.put(key,icon);
        }
        return icon;
    }
    /**
     * <producer>
     * To make user's interface for adjust the entity
     * @param entity editable entity
     * @return the UI
     */
    final public static EntityUI getUI(Entity entity)
    {
      Object instance = entity;
      String entityClassName = instance.getClass().getName();
      try {
          Class uiClass = Class.forName( entityClassName + "UI" );
          EntityUI ui = (EntityUI)uiClass.newInstance();
          ui.assign( entity );
          return ui;
      }catch(Exception e){
        e.printStackTrace();
      }
      return null;
    }
    /**
     * <producer>
     * To make the UI for Chain's adjusting
     * @param chain the owner of the UI
     * @return the UI
     */
    final public static UI getUI(Chain chain)
    {
      ChainUI ui = new ChainUI();
      ui.assign(chain);
      return ui;
    }
    /**
    <builder>
    To make UI for operation
    */
    final public static UI getUI(Operation oper)
    {
        OperationUI ui = null;
        String operClassName = oper.getClass().getName();
        synchronized( store )
        {
            Class uiClass = (Class)store.get( operClassName );
            if (uiClass == null) uiClass = operation_UI_class( operClassName );
            try {
                ui = (OperationUI)uiClass.newInstance();
                ui.assign( oper );
                // to get the icon from object and store it ...
                Icon oIcon = new Icon( ui.getIcon() );
                if (oIcon.icon != null) icon.put( operClassName, oIcon );
            }catch(Exception e){}
        }
        return ui;
    }
        /**
        <build>
        To make and register new Operation's UI class by default
        */
        final private static Class operation_UI_class(String operationClassName)
        {
            try {
                Class uiClass = Class.forName( operationClassName + "UI" );
                if (uiClass != null) store.put(operationClassName, uiClass);
                return uiClass;
            }catch(Exception e){}
            return baseOperationUI.class;
        }
}
