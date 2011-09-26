package com.tracker;

//import java.util.ArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List; 
import java.util.Locale; 
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent; 
//import android.graphics.drawable.Drawable;
import android.location.Address; 
import android.location.Criteria; 
import android.location.Geocoder; 
import android.location.Location; 
import android.location.LocationListener; 
import android.location.LocationManager; 
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle; 
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
//import android.util.Log;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; 
import android.widget.Button; 
import android.widget.EditText; 
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import com.google.android.maps.GeoPoint; 
//import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapController; 
import com.google.android.maps.MapView; 
//import com.google.android.maps.Overlay;
//import com.google.android.maps.OverlayItem;

public class ControlSquidCache extends Activity 
{ 
  private String TAG = "ControlSquidCache";

  private static final int MSG_OK = 1;
  private static final int MSG_TIMEOUT_CLOSE_PROGRESS = 2;

  private EditText etext;
  private Button bun;
  private String IPAddress;
  
  
  private SocketServer s_socket = null;
  private SendDataSocket sData;

  private int serve_port = 12345;
  
  public boolean Setting_Ready;

  private static final int MENU_EXIT = Menu.FIRST;
  
  @Override 
  protected void onCreate(Bundle icicle) 
  { 
    // TODO Auto-generated method stub 
    super.onCreate(icicle); 
    setContentView(R.layout.main2); 
    
    IPAddress = "192.168.123.100";
    
    //Checking Status
    if (CheckInternet(3))
    {
      //顯示輸入IP的windows
      final EditText input = new EditText(this);
      input.setText(IPAddress);
      AlertDialog.Builder alert = new AlertDialog.Builder(this);

      //openOptionsDialog(getLocalIpAddress());
      
      alert.setTitle("設定IP");
      alert.setMessage("請輸入IP");
      
      // Set an EditText view to get user input 
      alert.setView(input);
      
      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) 
      {
        try
        {
          IPAddress = input.getText().toString();  
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        //mMapController01.setCenter(getMapLocations(true).get(0).getPoint());
      }
      });

      alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) 
        {
          // Canceled.
        }
      });

      alert.show();      
    }
    else
    {
      openOptionsDialog("NO Internet");
    } 
    
    etext = (EditText) this.findViewById(R.id.url);
    
    bun = (Button) this.findViewById(R.id.send);

    bun.setOnClickListener(new Button.OnClickListener()
    {
      public void onClick(View v)
      {
        if (!etext.getText().toString().equals(""))
          SendURLData(etext.getText().toString());
        else
          openDialog("NoURL");
      }
    });
    
   }

  
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    
    menu.add(0 , MENU_EXIT, 1 ,R.string.menu_exit).setIcon(R.drawable.exit)
    .setAlphabeticShortcut('E');
  return true;  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
      { 
          case MENU_EXIT:
            openExitDialog();
    
             break ;
      }
    
      return true ;
  }
  
  
  public void SendURLData(String GPSData)
  {
    int port = 54321;

    Log.i(TAG, IPAddress + "," + port);
    sData = new SendDataSocket(this);
    sData.SetAddressPort(IPAddress , port);
    sData.SetSendData(GPSData);
    sData.SetFunction(1); 
    sData.start();
    
  }

  public int handler()
  {
    Message msg = new Message();
    msg.what = MSG_OK;
    myHandler.sendMessage(msg);
    return 1;
  }

  
  public int timeouthandler()
  {
    Log.i(TAG, "send OK");
    
    Message msg = new Message();
    msg.what = MSG_TIMEOUT_CLOSE_PROGRESS;
    myHandler.sendMessage(msg);
    return 1;
  }

  
  public String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); )
      {
          NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
            {
                InetAddress inetAddress = enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress()) {
                    return inetAddress.getHostAddress().toString();
                }
            }
      }
    }
    catch (SocketException ex) {
        Log.e("", ex.toString());
    }

    return null;
  }
  
  private boolean CheckInternet(int retry)
  {
    boolean has = false;
    for (int i=0; i<=retry; i++)
    {
      has = HaveInternet();
      if (has == true) break;       
    }
    
  return has;
  }  
  
  private boolean HaveInternet()
  {
     boolean result = false;
     
     ConnectivityManager connManager = (ConnectivityManager) 
                                getSystemService(Context.CONNECTIVITY_SERVICE); 
      
     NetworkInfo info = connManager.getActiveNetworkInfo();
     
     if (info == null || !info.isConnected())
     {
       result = false;
     }
     else 
     {
       if (!info.isAvailable())
       {
         result =false;
       }
       else
       {
         result = true;
       }
   }
  
   return result;
  }
  
  
  //show message
  public void openOptionsDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
           finish();
         }
         }
        )
    .show();
  }
  
  public Handler myHandler = new Handler(){
    public void handleMessage(Message msg) {
        switch(msg.what)
        {
          case MSG_OK:
              openDialog("squid cache refresh OK");
              break;
          case MSG_TIMEOUT_CLOSE_PROGRESS:
              openOptionsDialog("Connection Timeout");
              break;
        }
        super.handleMessage(msg);
    }
};  

  
  private void openExitDialog() {
    
    new AlertDialog.Builder(this)
      .setTitle(R.string.msg_exit)
      .setMessage(R.string.str_exit_msg)
      .setNegativeButton(R.string.str_exit_no,
          new DialogInterface.OnClickListener() {
          
            public void onClick(DialogInterface dialoginterface, int i) {
              
            }
      }
      )
   
      .setPositiveButton(R.string.str_exit_ok,
          new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialoginterface, int i) {
            
            finish();
          }
          
      }
      )
      
      .show();
  }  
  
  //show message
  public void openDialog(String info)
  {
    new AlertDialog.Builder(this)
    .setTitle("message")
    .setMessage(info)
    .setPositiveButton("OK",
        new DialogInterface.OnClickListener()
        {
         public void onClick(DialogInterface dialoginterface, int i)
         {
         }
         }
        )
    .show();
  }
}
