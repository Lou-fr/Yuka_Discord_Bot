package com.yukiDev.Yuki;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.mongodb.client.model.Sorts.descending;
import static com.yukiDev.Yuki.Bot.*;
import static com.yukiDev.Yuki.VoiceListener.baseUpdateDataBase;


public class YukaEmbedBuilder
{
    private static final Logger log = LoggerFactory.getLogger(YukaEmbedBuilder.class);

    public  static EmbedBuilder UserProfile(User user, String messageCount, double time, String guilID)
    {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(String.format("Profil de %s", user.getGlobalName()))
                .setThumbnail(user.getAvatarUrl())
                .addField("Nombre de message envoyé(s)", messageCount,false)
                .addField("Temps passer en vocal", getFormatedTime(time),false)
                .setColor(Color.black);
        if(guildOption.get(guilID).AdvanceVoiceActivity)
        {
            try {
                embed = advancedVCbuilding(user, time, guilID, embed);
            }catch (Exception ignored){embed.setColor(Color.orange);}
        }
        return  embed;
    }

    private static EmbedBuilder advancedVCbuilding(User user, double time, String guilID, EmbedBuilder embed) {
        Document resultorquerry = new Document().append("GuildId", guilID).append("NumberOfConnectedUser",1).append("User(s)", Arrays.asList(user.getId()));
        resultorquerry = voiceCollection.find(resultorquerry).first();
        double timespend = (resultorquerry != null) ? resultorquerry.getDouble("TimeSpent") : 0;
        embed.addField("Temps passé seul(e)",getFormatedTime(timespend),true);
        resultorquerry = new Document().append("GuildId", guilID).append("User(s)",new Document().append("$all",Arrays.asList(user.getId()))).append("NumberOfConnectedUser", new Document("$gt", 1L));
        timespend=0.0;
        for (Document i : voiceCollection.find(resultorquerry))
        {
            timespend+= i.getDouble("TimeSpent");
            log.debug(String.valueOf(timespend));
        }
        embed.addField("Temps passé avec d'autre membre",getFormatedTime(timespend),true);
        return  embed;
    }

    public  static  EmbedBuilder advancedVoiceActivityBuilder(String guildID,int limit,String filterMode)
    {

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.decode("#ff80ff"))
                .setTitle(String.format("Utlisateur le plus en voc triée par %s (%s)",filterMode,limit));
        Document resultorquerry = new Document().append("GuildId",guildID);
        //using defautl filter mode aka time
        for(Document i : voiceCollection.find(resultorquerry).sort(descending("TimeSpent")).limit(limit))
        {
            String FieldValue = "";
            ArrayList<String> i2 = (ArrayList<String>) i.get("User(s)");
            int index=0;
            for(String i3 : i2)
            {
                User user = getUser(i3);
                FieldValue += user.getGlobalName();
                if(index == (i2.size() -1))break;
                FieldValue += ", ";
                index++;
            }
            String FieldName = String.format("Avec %s on a ",getFormatedTime(i.getDouble("TimeSpent")));
            embed.addField(FieldName,FieldValue,false);

        }
        return embed;
    }

    //utils
    @NotNull
    private static String getFormatedTime(double timeSpendHours) {
        double timeSpendMinutes;
        double timeSpendSeconds= timeSpendHours *3600;
        String formatedTime = "";
        if(timeSpendSeconds>=3600)
        {
            timeSpendHours = timeSpendSeconds/3600;
            timeSpendMinutes = timeSpendSeconds/60;
            int timespendhours = (int) timeSpendHours %60;
            int timespendminutes = (int) timeSpendMinutes %60;
            int timespendseconds = (int) (timeSpendSeconds %60);
            formatedTime = String.format("%S heures, %s minutes et %s secondes",timespendhours,timespendminutes,timespendseconds);
        }
        else if(timeSpendSeconds>=60)
        {
            timeSpendMinutes = timeSpendSeconds/60;
            int timespendminutes = (int) timeSpendMinutes;
            int timespendseconds = (int) (timeSpendSeconds %60);
            formatedTime = String.format("%s minutes et %s secondes",timespendminutes,timespendseconds);
        }else
        {
            int timespendseconds = (int) (timeSpendSeconds %60);
            formatedTime = String.format("%s secondes",timespendseconds);
        }
        return formatedTime;
    }
}
