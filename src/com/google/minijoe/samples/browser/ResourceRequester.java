// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.minijoe.samples.browser;

import com.google.minijoe.common.Util;
import com.google.minijoe.html.SystemRequestHandler;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Image;

/**
 * This is a dumb sample implementation of a resource request helper to illustrate usage of the 
 * interface. 
 * 
 * @author Stefan Haustein
 */
public class ResourceRequester implements Runnable {

//  static final String USER_AGENT = 
//	  
//    "NokiaN70-1/2.0539.1.2 Series60/2.8 Profile/MIDP-2.0 Configuration/CLDC-1.1";
////    "Mozilla/5.0 (SymbianOS/9.1; U; [en-us]) AppleWebKit/413 (KHTML, like Gecko) Safari/413";
//  //  "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-GB; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11";
//  //  "My own browser";//"NokiaN70-1/5.0609.2.0";

  static final String[] WAIT_MSGS = {
    "Growing XML tree", "Mixing Colors", "Styling Styles", "Counting Pages"
  };

  static int msg;
  static int total;
  HtmlScreen screen;
  int method;
  String url;
  int type;
  byte[] requestData;

  public ResourceRequester(HtmlScreen screen, int method, String url, int type, 
      byte[] data) {
    this.screen = screen;
    this.method = method;
    this.url = url;
    this.type = type;
    this.requestData = data;
  }

  public void run() {
    HtmlBrowser browser = screen.browser;
    
    try {
      boolean post = method == SystemRequestHandler.METHOD_POST;
      boolean page = type == SystemRequestHandler.TYPE_DOCUMENT;
      String encoding = null;

      int cut = url.indexOf('#');

      StreamConnection con = (StreamConnection) Connector.open(
          cut == -1 ? url : url.substring(0, cut), 
              post ? Connector.READ_WRITE : Connector.READ);

      screen.setStatus(type == SystemRequestHandler.TYPE_IMAGE ? "Loading Image" : 
          "Requesting");

      int contentLength = -1; 

      if (con instanceof HttpConnection) {
        HttpConnection httpCon = (HttpConnection) con;
        if (post) {
          httpCon.setRequestMethod("POST");
        }
        httpCon.setRequestProperty("User-Agent", browser.userAgent);
        httpCon.setRequestProperty("X-Screen-Width", ""+screen.getWidth());
        httpCon.setRequestProperty("Connection", "Keep-Alive");
        httpCon.setRequestProperty("Accept-Charset", "utf-8");

        String cookie = browser.getCookies(url);
        if (cookie != null) {
          httpCon.setRequestProperty("Cookie", cookie);
        }

        if (post && requestData != null) {
          OutputStream os = httpCon.openOutputStream();
          os.write(requestData);
          os.close();
        }

        contentLength = (int) httpCon.getLength();
        int headerIndex = 0;
        while (true){
          String name = httpCon.getHeaderFieldKey(headerIndex);
          if (name == null) {
            break;
          }
          name = name.toLowerCase();

          if ("content-type".equals(name)) {
            String value = httpCon.getHeaderField(headerIndex);
            if (value.indexOf("vnd.sun.j2me.app-descriptor") != -1) {
              browser.platformRequest(url);
              return;
            }
            int eq = value.indexOf ("harset="); 
            if (eq != -1) {
              encoding = value.substring(eq + 7).trim();
            }
          } else if ("set-cookie".equals(name)) {
            String cName = "";
            String cValue = "";
            String cHost = "";
            String cPath = "";
            String cExpires = null;
            String cSecure = null;
            String[] parts = Util.split(
                httpCon.getHeaderField(headerIndex), ';');
            for (int i = 0; i < parts.length; i++){
              String part = parts[i];
              cut = part.indexOf('=');
              if (cut == -1) {
                continue;
              }
              String key = part.substring(0, cut).trim();
              String value = part.substring(cut + 1).trim();
              if (i == 0) {
                cName = key;
                cValue = value;
              } else {
                key = key.toLowerCase();
                if ("host".equals(key)) {
                  cHost = value;
                } else if ("path".equals(key)) {
                  cPath = value;
                } else if ("expires".equals(key)) {
                  cExpires = value;
                } else if ("secure".equals(key)) {
                  cSecure = value;
                }
              }           
            }
            browser.setCookie(cHost, cPath, cName, cValue, cExpires, cSecure);
          }
          headerIndex++;
        }

        int responseCode = httpCon.getResponseCode();
        
        if (responseCode >= 300 && responseCode <= 310) {
          String location = httpCon.getHeaderField("Location");
          if (location != null) {
            System.out.println("Redirecting to: " + location);
        	screen.htmlWidget.setUrl(location);
            browser.requestResource(screen.htmlWidget, method, location, 
                type, requestData);
            return;
          }
        }

        if (responseCode != 200 && !page) {
          throw new IOException("HTTP Error " + responseCode);
        }
      }

      byte [] responseData;

      InputStream is = con.openInputStream();
      if (is == null) {
        responseData = new byte[0];
      } else {
        if (contentLength == -1) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte [] buf = new byte [2048];

          while (true) {
            screen.setStatus(total / 1000 + "k");
            int len = is.read(buf, 0, 2048);
            if (len <= 0) {
              break;
            }
            total += len;
            baos.write(buf, 0, len);
          }
          responseData = baos.toByteArray();
        } else {
          responseData = new byte[contentLength];
          int pos = 0;
          while (pos < contentLength) {
            screen.setStatus(total / 1000 + "k");
            int len = is.read(responseData, pos, 
                Math.min(contentLength - pos, 4096));
            if (len <= 0) {
              break;
            }
            pos += len;
            total += len;
          }
        }
      }
      con.close();

      switch (type) {
        case SystemRequestHandler.TYPE_DOCUMENT:

          String tmp = new String(responseData, 0, Math.min(responseData.length,
              4096), "US-ASCII");
          int i = tmp.indexOf("harset=");

          if (i != -1) {
            i += 7;
            StringBuffer sb = new StringBuffer();
            while (i < tmp.length()) {
              char c = tmp.charAt(i);
              if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && 
                  (c < '0' || c > '9') && c != '-') {
                break;
              }
              sb.append(c);
              i++;
            }
            encoding = sb.toString();
          }

          screen.setStatus(WAIT_MSGS[msg++ % WAIT_MSGS.length]);
          screen.htmlWidget.load(new ByteArrayInputStream(responseData), 
              encoding);
          break;

        case SystemRequestHandler.TYPE_IMAGE:
          try {
            Image image = Image.createImage(responseData, 0, responseData.length);
            screen.htmlWidget.addResource(url, image);
          } catch (IllegalArgumentException e) {
            System.err.println("Img fmt err: " + e);
          }
          break;

        case SystemRequestHandler.TYPE_STYLESHEET:
          String styleSheet = new String(responseData, "UTF-8");
          screen.htmlWidget.addResource(url, styleSheet);
          break;

        default:
          screen.htmlWidget.addResource(url, responseData);
          break;
      }
    } catch (IOException e) {
      System.err.println("Failed to request resource: " + url);
      e.printStackTrace();
    } finally {
      screen.setStatus(null);
    }
  }
}
