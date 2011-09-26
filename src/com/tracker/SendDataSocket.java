package com.tracker;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.util.Log;
 
public class SendDataSocket extends Thread 
{

	private String address;
	private int port;
	private int function;
	private String timestamp;
	private int IsOK;
	private ControlSquidCache csc;
	private int timeout;
	public String error_string;
  public String send_Data;
	String line;
	
	public SendDataSocket(ControlSquidCache map) 
  {
		IsOK = 0;
		csc = map;
  }
	
	public void SetAddressPort(String addr, int p)
	{		
		this.address = addr;
		this.port = p;
	}
	
	public int getTimeout()
	{
	  return timeout;
	}
	
  public void SetSendData(String sdata)
  {   
    this.send_Data = sdata;
  }	
  
	public String getTimeStamp()
	{
		return timestamp;		
	}
	
	public void SetFunction(int func)
	{
		function = func;		
	}

	public int getIsOK()
	{
		return IsOK;
	}
	
	@Override
	public void run() 
	{
    do{
        Socket client = new Socket();
        InetSocketAddress isa = new InetSocketAddress(address, port);
        try {
            client.connect(isa, 13265);
            
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            if (function  == 1)
            {
              out.writeUTF("URL");
           	  out.writeUTF(send_Data);
            	 // As long as we receive data, server will data back to the client.
              DataInputStream is = new DataInputStream(client.getInputStream());
              line = is.readUTF();
              while (line.equals("OK")) 
              {
                  IsOK = 2;
                  csc.handler();
                	break;
              }
              	
              is.close();
            }
            out.close();
            client.close();
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
        
        timeout++;
        if (timeout > 10)
        {
          csc.timeouthandler();
          break;
        }
     } while (IsOK != 2);
	}
}