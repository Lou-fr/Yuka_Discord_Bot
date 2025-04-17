package com.yukiDev.Yuki;

import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.yukiDev.Yuki.Bot.*;


public class VoiceListener extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(VoiceListener.class);
    Dictionary<String, LocalDateTime> ConnectedMember = new Hashtable<>();
    HashMap<Integer,AdvVoiceClass> AdvConnectedMember = new HashMap<>();
    @Override
    public  void onGuildVoiceUpdate(GuildVoiceUpdateEvent event)
    {
        if(event.getChannelLeft() != null)
        {
            log.debug("An user as leave {} and the user is {}", event.getChannelLeft().getName(), event.getMember().getUser().getName());
            if(guildOption.get(event.getGuild().getId()).VoiceActivity) baseVoiceActivityDisconnect(event);
            if(guildOption.get(event.getGuild().getId()).AdvanceVoiceActivity) advanceVoiceActivityDisconnect(event);
        }
        if(event.getChannelJoined()!= null)
        {
            log.debug("An user as join {} and the user is {}", event.getChannelJoined().getName(), event.getEntity().getUser().getName());
            if(guildOption.get(event.getGuild().getId()).VoiceActivity)baseVoiceActivityConnect(event);
            if(guildOption.get(event.getGuild().getId()).AdvanceVoiceActivity)advanceVoiceActivityConnect(event);
        }
    }

    private void baseVoiceActivityConnect(GuildVoiceUpdateEvent event)
    {
        ConnectedMember.put(event.getEntity().getUser().getId(), LocalDateTime.now());
    }
    private void advanceVoiceActivityConnect(GuildVoiceUpdateEvent event)
    {
        if(!AdvConnectedMember.isEmpty() && AdvConnectedMember.get(event.getChannelJoined().getMembers().size()-1) != null)
        {
            advVoiceUpdateDataBase(AdvConnectedMember.get(event.getChannelJoined().getMembers().size()-1),event.getGuild().getId());
        }
        AdvConnectedMember.put(event.getChannelJoined().getMembers().size(),new AdvVoiceClass (event.getNewValue().getMembers(),LocalDateTime.now(),event.getChannelJoined().getMembers().size()));
    }

    private void baseVoiceActivityDisconnect(GuildVoiceUpdateEvent event)
    {
        Document querry =  new Document().append("UserId", event.getMember().getUser().getId()).append("ServerId", event.getGuild().getId());
        double timeSpend = Duration.between(ConnectedMember.get(event.getMember().getUser().getId()),LocalDateTime.now()).getSeconds()/3600.0;
        baseUpdateDataBase(timeSpend, querry);
        ConnectedMember.remove(event.getMember().getUser().getId());
    }

    public static void baseUpdateDataBase(double timeSpend, Document querry) {
        Bson updates = Updates.combine(Updates.inc("VoiceHoursCount", timeSpend));
        usersCollection.updateOne(querry,updates);
    }

    private  void advanceVoiceActivityDisconnect(GuildVoiceUpdateEvent event)
    {
        if(event.getChannelLeft().getMembers().size() >= 1)
        {
            advVoiceUpdateDataBase(AdvConnectedMember.get(event.getChannelLeft().getMembers().size()), event.getGuild().getId());
            AdvConnectedMember.put(event.getChannelLeft().getMembers().size(),new AdvVoiceClass (event.getChannelLeft().getMembers(),LocalDateTime.now(),event.getChannelLeft().getMembers().size()));
        }
        else advVoiceUpdateDataBase(AdvConnectedMember.get(1), event.getGuild().getId());
    }
    private void advVoiceUpdateDataBase(AdvVoiceClass advVoiceClass, String guildID)
    {
        Document doc = new Document().append("User(s)",advVoiceClass.BuildUserIDList()).append("GuildId",guildID);
        double timeSpend = Duration.between(advVoiceClass.GetStartTime(),LocalDateTime.now()).getSeconds()/3600.0;
        doc.append("NumberOfConnectedUser",advVoiceClass.GetNumberOfUser());
        try {
            if(voiceCollection.find(doc).first() == null)
            {
                doc.append("TimeSpent",timeSpend);
                voiceCollection.insertOne(doc);
                AdvConnectedMember.remove(advVoiceClass.GetNumberOfUser());
                return;
            }
            Bson Update = Updates.combine(Updates.inc("TimeSpent",timeSpend));
            voiceCollection.updateOne(doc,Update);
            AdvConnectedMember.remove(advVoiceClass.GetNumberOfUser());
        }catch (Exception e)
        {
            log.error(e.getMessage());
        }
    }
}

