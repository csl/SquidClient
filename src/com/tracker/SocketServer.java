package com.tracker;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import com.google.android.maps.GeoPoint;

import android.util.Log;
import android.widget.Toast;

public class SocketServer implements Runnable
{
	private int port;
	private ServerSocket sc;
	private ControlSquidCache csc;
	
	public boolean ChildPhoneReady;

	public SocketServer(int port, ControlSquidCache mmap)throws IOException
	{
		this.port = port;
		this.sc = new ServerSocket(port);
		csc = mmap;
		ChildPhoneReady = false;
	}
	
	public void run()
	{
		Socket con = null;
		while(true)
		{
			try
			{
				con = this.sc.accept();
				DataInputStream in = new DataInputStream(con.getInputStream());
				String str = in.readUTF();
				Log.v("vDEBUG: ", "vClient " + str);
				//call back
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
       out.writeUTF("OK");
       out.flush();

       in.close();
				con.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
}
