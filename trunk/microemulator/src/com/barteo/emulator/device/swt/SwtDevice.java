/*
 *  MicroEmulator
 *  Copyright (C) 2002 Bartek Teodorczyk <barteo@it.pl>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package com.barteo.emulator.device.swt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.barteo.emulator.EmulatorContext;
import com.barteo.emulator.app.ui.swt.ImageFilter;
import com.barteo.emulator.app.ui.swt.SwtDeviceComponent;
import com.barteo.emulator.device.Device;
import com.barteo.emulator.device.DeviceDisplay;
import com.barteo.emulator.device.FontManager;
import com.barteo.emulator.device.InputMethod;


public class SwtDevice implements Device
{
  private SwtDeviceDisplay deviceDisplay;
  private FontManager fontManager = null;
  private SwtInputMethod inputMethod = null;
  private Vector buttons;
  private Vector softButtons;
  
  private Image normalImage;
  private Image overImage;
  private Image pressedImage;
  
  
  public SwtDevice()
  {
  }
  
  
  public void init(EmulatorContext context)
  {
    // Here should be device.xml but Netscape security manager doesn't accept this extension
    init(context, "/com/barteo/emulator/device/device.txt");
  }
  
  
  public void init(EmulatorContext context, String config)
  {
    deviceDisplay = new SwtDeviceDisplay(context);
    buttons = new Vector();
    softButtons = new Vector();
    
    try {
    	loadConfig(config);
    } catch (IOException ex) {
    	System.out.println("Cannot load config: " + ex);
    }
  }
  
  
	public javax.microedition.lcdui.Image createImage(int width, int height)
	{
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}
	
		return new MutableImage(width, height);
	}
	
																
	public javax.microedition.lcdui.Image createImage(String name)
  		throws IOException
	{
		return new ImmutableImage(getImage(name));
	}
  
  
	public javax.microedition.lcdui.Image createImage(javax.microedition.lcdui.Image source)
  {
    if (source.isMutable()) {
      return new ImmutableImage((MutableImage) source);
    } else {
      return source;
    }
  }
  

  public javax.microedition.lcdui.Image createImage(byte[] imageData, int imageOffset, int imageLength)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(imageData, imageOffset, imageLength);
		try {
			return new ImmutableImage(getImage(is));
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex.toString());
		}
	}
  
  
  public DeviceDisplay getDeviceDisplay()
  {
    return deviceDisplay;
  }
  
  
  public FontManager getFontManager()
  {
    if (fontManager == null) {
      fontManager = new SwtFontManager();
    }
    
    return fontManager;
  }
  
  
  public InputMethod getInputMethod()
  {
    if (inputMethod == null) {
      inputMethod = new SwtInputMethod();
    }
    
    return inputMethod;
  }
  
  
  public int getGameAction(int keyCode) 
  {
		// TODO poprawic KeyEvent	
		switch (keyCode) {
			case SWT.ARROW_UP:
				return Canvas.UP;
        
			case SWT.ARROW_DOWN:
				return Canvas.DOWN;
        
			case SWT.ARROW_LEFT:
				return Canvas.LEFT;
      	
			case SWT.ARROW_RIGHT:
				return Canvas.RIGHT;
      	
			case SWT.CR:
				return Canvas.FIRE;
      	
/*			case KeyEvent.VK_1:
			case KeyEvent.VK_A:
				return Canvas.GAME_A;
        
			case KeyEvent.VK_3:
			case KeyEvent.VK_B:
				return Canvas.GAME_B;
        
			case KeyEvent.VK_7:
			case KeyEvent.VK_C:
				return Canvas.GAME_C;
        
			case KeyEvent.VK_9:
			case KeyEvent.VK_D:
				return Canvas.GAME_D;*/
        
			default:
				return 0;
		}
  }


  public int getKeyCode(int gameAction) 
  {
		// TODO poprawic KeyEvent	
		switch (gameAction) {
			case Canvas.UP:
				return SWT.ARROW_UP;
        
			case Canvas.DOWN:
				return SWT.ARROW_DOWN;
        
			case Canvas.LEFT:
				return SWT.ARROW_LEFT;
        
			case Canvas.RIGHT:
				return SWT.ARROW_RIGHT;
        
			case Canvas.FIRE:
				return SWT.CR;
        
/*			case Canvas.GAME_A:
				return KeyEvent.VK_1;
        
			case Canvas.GAME_B:
				return KeyEvent.VK_3;
        
			case Canvas.GAME_C:
				return KeyEvent.VK_7;
        
			case Canvas.GAME_D:
				return KeyEvent.VK_9;*/

			default:
				throw new IllegalArgumentException();
		}
  }


  public Vector getSoftButtons()
  {
    return softButtons;
  }

  
  public Image getNormalImage()
  {
    return normalImage;
  }

  
  public Image getOverImage()
  {
    return overImage;
  }

  
  public Image getPressedImage()
  {
    return pressedImage;
  }

  
  public boolean hasPointerMotionEvents()
  {
    return false;
  }
  
  
  public boolean hasPointerEvents()
  {
    return false;
  }
  
  
  public boolean hasRepeatEvents()
  {
    return false;
  }

  
  public void dispose()
  {
		inputMethod.dispose();
		inputMethod = null;
  }
  
  
  public Vector getButtons()
  {
    return buttons;
  }

  
  public void loadConfig(String config)
  		throws IOException
  {
    String xml = "";
    InputStream dis = new BufferedInputStream(getClass().getResourceAsStream(config));
    while (dis.available() > 0) {
      byte[] b = new byte[dis.available()];
      if (dis.read(b) == -1) {
        break;
      }
      xml += new String(b);
    }

    XMLElement doc = new XMLElement();
    try {
      doc.parseString(xml);
    } catch (XMLParseException ex) {
    	throw new IOException(ex.toString());
    }

    for (Enumeration e = doc.enumerateChildren(); e.hasMoreElements(); ) {
      XMLElement tmp = (XMLElement) e.nextElement();
      if (tmp.getName().equals("img")) {
        try {
          if (tmp.getStringAttribute("name").equals("normal")) {
            normalImage = getSystemImage(tmp.getStringAttribute("src"));
          } else if (tmp.getStringAttribute("name").equals("over")) {
            overImage = getSystemImage(tmp.getStringAttribute("src"));
          } else if (tmp.getStringAttribute("name").equals("pressed")) {
            pressedImage = getSystemImage(tmp.getStringAttribute("src"));
          }
        } catch (IOException ex) {
          System.out.println("Cannot load " + tmp.getStringAttribute("src"));
          return;
        }
      } else if (tmp.getName().equals("display")) {
        for (Enumeration e_display = tmp.enumerateChildren(); e_display.hasMoreElements(); ) {
          XMLElement tmp_display = (XMLElement) e_display.nextElement();
          if (tmp_display.getName().equals("numcolors")) {
            deviceDisplay.numColors = Integer.parseInt(tmp_display.getContent());
          } else if (tmp_display.getName().equals("iscolor")) {
            deviceDisplay.isColor = parseBoolean(tmp_display.getContent());
          } else if (tmp_display.getName().equals("background")) {
          	int color = Integer.parseInt(tmp_display.getContent(), 16);
            deviceDisplay.backgroundColor = 
            		SwtDeviceComponent.createColor(
            				new RGB((color >> 16) % 256, (color >> 8) % 256, color % 256));
          } else if (tmp_display.getName().equals("foreground")) {
						int color = Integer.parseInt(tmp_display.getContent(), 16);
            deviceDisplay.foregroundColor = 
								SwtDeviceComponent.createColor(
										new RGB((color >> 16) % 256, (color >> 8) % 256, color % 256));
          } else if (tmp_display.getName().equals("rectangle")) {
            deviceDisplay.displayRectangle = getRectangle(tmp_display);
          } else if (tmp_display.getName().equals("paintable")) {
            deviceDisplay.displayPaintable = getRectangle(tmp_display);
          }
        }
        for (Enumeration e_display = tmp.enumerateChildren(); e_display.hasMoreElements(); ) {
          XMLElement tmp_display = (XMLElement) e_display.nextElement();          
          if (tmp_display.getName().equals("img")) {
            if (tmp_display.getStringAttribute("name").equals("up")) {
              deviceDisplay.upImage = new PositionedImage(
                  getImage(tmp_display.getStringAttribute("src")),
                  getRectangle(getElement(tmp_display, "paintable")));
            } else if (tmp_display.getStringAttribute("name").equals("down")) {
              deviceDisplay.downImage = new PositionedImage(
                  getImage(tmp_display.getStringAttribute("src")),
                  getRectangle(getElement(tmp_display, "paintable")));
            } else if (tmp_display.getStringAttribute("name").equals("mode")) {
              if (tmp_display.getStringAttribute("type").equals("123")) {
                deviceDisplay.mode123Image = new PositionedImage(
                    getImage(tmp_display.getStringAttribute("src")),
                    getRectangle(getElement(tmp_display, "paintable")));
              } else if (tmp_display.getStringAttribute("type").equals("abc")) {
                deviceDisplay.modeAbcLowerImage = new PositionedImage(
                    getImage(tmp_display.getStringAttribute("src")),
                    getRectangle(getElement(tmp_display, "paintable")));
              } else if (tmp_display.getStringAttribute("type").equals("ABC")) {
                deviceDisplay.modeAbcUpperImage = new PositionedImage(
                    getImage(tmp_display.getStringAttribute("src")),
                    getRectangle(getElement(tmp_display, "paintable")));
              }
            }
          }
        }
      } else if (tmp.getName().equals("keyboard")) {
        for (Enumeration e_keyboard = tmp.enumerateChildren(); e_keyboard.hasMoreElements(); ) {
          XMLElement tmp_keyboard = (XMLElement) e_keyboard.nextElement();
          if (tmp_keyboard.getName().equals("button")) {
            Rectangle rectangle = null;
            Vector stringArray = new Vector();
            for (Enumeration e_button = tmp_keyboard.enumerateChildren(); e_button.hasMoreElements(); ) {
              XMLElement tmp_button = (XMLElement) e_button.nextElement();
              if (tmp_button.getName().equals("chars")) {
                for (Enumeration e_chars = tmp_button.enumerateChildren(); e_chars.hasMoreElements(); ) {
                  XMLElement tmp_chars = (XMLElement) e_chars.nextElement();
                  if (tmp_chars.getName().equals("char")) {                 
                    stringArray.addElement(tmp_chars.getContent());                    
                  }
                }
              } else if (tmp_button.getName().equals("rectangle")) {
                rectangle = getRectangle(tmp_button);
              }
            }
            char[] charArray = new char[stringArray.size()];
            for (int i = 0; i < stringArray.size(); i++) {
              String str = (String) stringArray.elementAt(i);
              if (str.length() > 0) {
                charArray[i] = str.charAt(0);
              } else {
                charArray[i] = ' ';
              }
            }
            buttons.addElement(new SwtButton(tmp_keyboard.getStringAttribute("name"), 
                rectangle, tmp_keyboard.getStringAttribute("key"), charArray));
          } else if (tmp_keyboard.getName().equals("softbutton")) {
            Vector commands = new Vector();
            Rectangle rectangle = null, paintable = null;
            for (Enumeration e_button = tmp_keyboard.enumerateChildren(); e_button.hasMoreElements(); ) {
              XMLElement tmp_button = (XMLElement) e_button.nextElement();
              if (tmp_button.getName().equals("rectangle")) {
                rectangle = getRectangle(tmp_button);
              } else if (tmp_button.getName().equals("paintable")) {
                paintable = getRectangle(tmp_button);
              } else if (tmp_button.getName().equals("command")) {
                commands.addElement(tmp_button.getContent());
              }
            }
            SwtSoftButton button = new SwtSoftButton(tmp_keyboard.getStringAttribute("name"),
                rectangle, tmp_keyboard.getStringAttribute("key"), paintable, 
                tmp_keyboard.getStringAttribute("alignment"), commands);
            buttons.addElement(button);
            softButtons.addElement(button);
          }
        }
      }
    }
  }

  
  private XMLElement getElement(XMLElement source, String name)
  {
    for (Enumeration e_content = source.enumerateChildren(); e_content.hasMoreElements(); ) {
      XMLElement tmp_content = (XMLElement) e_content.nextElement();
      if (tmp_content.getName().equals(name)) {
        return tmp_content;
      }
    }

    return null;
  }
  
  
  private Rectangle getRectangle(XMLElement source)
  {
    Rectangle rect = new Rectangle(0, 0, 0, 0);
    
    for (Enumeration e_rectangle = source.enumerateChildren(); e_rectangle.hasMoreElements(); ) {
      XMLElement tmp_rectangle = (XMLElement) e_rectangle.nextElement();
      if (tmp_rectangle.getName().equals("x")) {
        rect.x = Integer.parseInt(tmp_rectangle.getContent());
      } else if (tmp_rectangle.getName().equals("y")) {
        rect.y = Integer.parseInt(tmp_rectangle.getContent());
      } else if (tmp_rectangle.getName().equals("width")) {
        rect.width = Integer.parseInt(tmp_rectangle.getContent());
      } else if (tmp_rectangle.getName().equals("height")) {
        rect.height = Integer.parseInt(tmp_rectangle.getContent());
      }
    }
    
    return rect;
  }
  
  
  private boolean parseBoolean(String value)
  {
    if (value.toLowerCase().equals(new String("true").toLowerCase())) {
      return true;
    } else {
      return false;
    }
  }

  
  private Image getSystemImage(String str)
			throws IOException
	{
    InputStream is;

    is = getClass().getResourceAsStream(str);
    if (is == null) {
      throw new IOException();
    }
    
		return SwtDeviceComponent.createImage(is);
	}
  

	private Image getImage(String str)
			throws IOException
	{
		InputStream is = deviceDisplay.getEmulatorContext().getClassLoader().getResourceAsStream(str);

		if (is == null) {
				throw new IOException(str + " could not be found.");
		}

		return getImage(is);			
	}

  
  private Image getImage(InputStream is)
  		throws IOException
  {
		ImageFilter filter = null;
    if (getDeviceDisplay().isColor()) {
			filter = new RGBImageFilter();
    } else {
      if (getDeviceDisplay().numColors() == 2) {
        filter = new BWImageFilter();
      } else {
        filter = new GrayImageFilter();
      }
    }

		return SwtDeviceComponent.createImage(is, filter);
  }
  
}
