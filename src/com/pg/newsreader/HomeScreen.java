package com.pg.newsreader;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

//	HTML Parser imports


public class HomeScreen extends Activity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	
	private TextToSpeech tts;
	private List<String> data= new ArrayList<String>();
	private Document doc;
	private int result=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        //	initializing data in array
        
//        data.add("Hi,here are the top news headlines from Bloomberg");
//        data.add("First Quantum increases Inmet Offer 2.9% to $5.18 billion");
//        data.add("China signals tolerance of slower growth after meeting");
//        data.add("Deutsche bank probes claims of deleted emails in a tax probe");
        
        //	End of data init
        
        //init Parser
        InitParserTask parser= new InitParserTask();
        parser.execute(new String[] {"http://mobile.bloomberg.com"} );
        //	Init text to speech.
        tts= new TextToSpeech(getApplicationContext(),(OnInitListener) this);
        

        Button load_data= (Button) findViewById(R.id.home_loadbutton);
        load_data.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				TextView botstatus= (TextView) findViewById(R.id.home_botstatus);
				if(botstatus.getText().toString().equals("Bot Status: Initializing.."))
				{
					Toast.makeText(getApplicationContext(),
		   					"Bot loading data, please wait..", Toast.LENGTH_LONG).show();
					return;
				}
				if((botstatus.getText().toString().equals("Bot Status: Idle")) & (result==1) )
				{
					Get_Data();
					botstatus.setText("Bot Status: Speaking..");
					Speak();
				}
			}
		});
        
       
        
    }
    
    public void onInit(int status)
    {
    	if(status==TextToSpeech.SUCCESS)
    	{
    		result= tts.setLanguage(Locale.US);
    		System.out.println("TTS init win");
    	}
    	else
    	{
    		System.out.println("TTS init failed.");
    	}
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_screen, menu);
        return true;
    }
    
    private void Speak()
    {
    	System.out.println("in speak() ");
    	if(!data.isEmpty())
    	{
    		System.out.println("length of data is "+Integer.toString(data.size()));
    		Iterator<String> iterator= data.iterator();
    		while(iterator.hasNext())
    		{
    			tts.speak(iterator.next().toString(),TextToSpeech.QUEUE_ADD,null);
    		}
    	}
		
    }
    

	public void onDestroy()
    {
    	if(tts!=null)
    	{
    		tts.stop();
    		tts.shutdown();
    	}
    	super.onDestroy();
    }
	
	public void onPause()
	{
		if(tts!=null)
		{
			if(tts.isSpeaking())
			{
				tts.stop();
			}
		}
		super.onPause();
	}
    
	
	private void Get_Data()
	{
		if(doc==null)
		{
			System.out.println("doc is null");
			return;
		}
        Elements spans= doc.getElementsByTag("span");
        System.out.println("length of spans is "+Integer.toString(spans.size()) );
        for(Element e: spans)
        {
//        	System.out.println("looping over spans");
        	if(e.hasClass("article-title") || (e.hasClass("top-article-headline-text")) )
        	{
        		String temp= e.text().toString();
        		data.add(temp);
        	}
        }
		
	}
	
	private class InitParserTask extends AsyncTask <String, Void, String>
	{

		@Override
		protected String doInBackground(String... params) 
		{
	        try
	        {
	        	System.out.println("initializing parser and getting page");
	        	doc=  Jsoup.connect("http://mobile.bloomberg.com").get();
//	        	new BotStatus().start();
	        	return "Win";
	        }
	        catch(Exception e)
	        {
	        	System.out.println(e.toString());
	        	
	        }
	        return "Failed";

		}
		
		protected void onPostExecute(String result)
		{
			System.out.println(result);
			if(result.equals("Win"))
			{
				TextView botstatus= (TextView) findViewById(R.id.home_botstatus);
				botstatus.setText("Bot Status: Idle");
			}
			else
			{
				TextView botstatus= (TextView) findViewById(R.id.home_botstatus);
				botstatus.setText("Bot Status: Failed to connect");
			}
		}
		
	}
	
	public class BotStatus extends Thread
	{
		TextView temp= (TextView) findViewById(R.id.home_botstatus);
		public void run()
		{
			System.out.println("in side thread");
			while(true)
			{
				
				if(tts.isSpeaking())
				{
					TextView temp= (TextView) findViewById(R.id.home_botstatus);
					if(temp.getText().toString().equals("Bot Status: Idle"))
					{
						temp.setText("Bot Status: Speaking..");
					}
				}
				else
				{
					try {
						
						if(temp.getText().toString().equals("Bot Status: Speaking.."))
						{
							temp.setText("Bot Status: Idle");
						}
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		
		TextView botstatus= (TextView) findViewById(R.id.home_botstatus);
		botstatus.setText("Bot Status: Idle");
	}
    
}
