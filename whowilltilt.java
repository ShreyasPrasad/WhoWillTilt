package primary;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class whowilltiltMAIN {

	public static void main(String[] args) throws Exception{

		new whowilltiltMAIN("MzI5NzY1NjA4NjE2ODg2Mjcy.DDXOMQ.7BSZ0igTHJpM01GBGi1A8FbCCOE");

	}
	public whowilltiltMAIN(String token)throws Exception{
		
		
		//javacord handle result 
		
		DiscordAPI api = Javacord.getApi(token, true);
        // connect
        api.connect(new FutureCallback<DiscordAPI>() {
            
            public void onSuccess(DiscordAPI api) {
                // register listener
                api.registerListener(new MessageCreateListener() {
                   
                    public void onMessageCreate(DiscordAPI api, Message message) {
                        // check the content of the message
                        if (message.getContent().substring(0, 12).equalsIgnoreCase("!WhoWillTilt")) {
                            // reply to the message
                            try {
                                message.reply("Determining player that will tilt...");
								message.reply(determination(message.getContent().substring(13, message.getContent().length())));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                        }
                    }
                });
            }

        
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

	

	private String determination(String input)throws Exception{
		

		String output=access("https://na.api.pvp.net/api/lol/na/v1.4/summoner/by-name/"+input.toLowerCase().replaceAll("\\s+","")+"?api_key=RGAPI-84802d47-70ad-4b2a-89f9-317a33616659");

		JSONObject json1 = (JSONObject)new JSONParser().parse(output);
		String user=json1.get(input.toLowerCase().replaceAll("\\s+","")).toString();


		json1 = (JSONObject)new JSONParser().parse(user);
		int id=Integer.parseInt(json1.get("id").toString());


		output=access("https://na.api.pvp.net/observer-mode/rest/consumer/getSpectatorGameInfo/NA1/"+id+"?api_key=RGAPI-84802d47-70ad-4b2a-89f9-317a33616659");

		json1 = (JSONObject)new JSONParser().parse(output);
		JSONArray array1=(JSONArray)json1.get("participants");

		int n=array1.size();
		Long []summonerIds=new Long[10];
		String [] summonerNames=new String [10];
		Long [] championIds=new Long[10];
		for (int i=0; i<n; i++){
			final JSONObject person = (JSONObject)array1.get(i);
			summonerIds[i]=(Long)person.get("summonerId");
			summonerNames[i]=person.get("summonerName").toString();
			championIds[i]=(Long)person.get("championId");
		}

		//evaluation method

		Double [] scores= {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		for (int i=0; i<summonerIds.length; i++){
			//  scores[i]+=getRecentGamesScore(summonerIds[i]);
			scores[i]+=getChampionMastery(summonerIds[i], championIds[i]);
			if (i==5){
				TimeUnit.SECONDS.sleep(9);
			}
		}

		int min=0; 
		for (int i=0; i<summonerIds.length; i++){
			if (scores[i]<scores[min])
				min=i;
		}

		String finalplayer=summonerNames[min];
		
		return finalplayer;
	}

	private static Double getChampionMastery(Long sumid, Long champid) throws Exception{

		String output=access("https://na.api.riotgames.com/championmastery/location/NA1/player/"+sumid+"/champion/"+champid+"?api_key=RGAPI-84802d47-70ad-4b2a-89f9-317a33616659"); 
		JSONObject champmastery=(JSONObject)new JSONParser().parse(output);
		long mastery=(Long)champmastery.get("championLevel");

		long lastPlayed=(Long)(champmastery.get("lastPlayTime"));
		long timecurrent= System.currentTimeMillis();

		long days=TimeUnit.MILLISECONDS.toDays(timecurrent-lastPlayed);


		if (days>30)
			return (double)(((mastery/7)*30)+0);
		else
			return (double)(((mastery/7)*30)+((30-days)/30)*30);
	}

	public static String access(String url) throws Exception{
		String output="";
		URL oracle = new URL(url);
		URLConnection yc = oracle.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null){ 
			output=output+""+inputLine;     
		}
		in.close();

		return output;
	}

}
