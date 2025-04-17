package com.yukiDev.Yuki;

import com.mongodb.client.model.Projections;
import com.mongodb.client.result.InsertOneResult;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.yukiDev.Yuki.Bot.*;
import static com.yukiDev.Yuki.YukaEmbedBuilder.*;

public class CommandsList extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(CommandsList.class);

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        switch (event.getName()){
            case "verifyreg":
                VerifyReg(event);
                break;
            case "stop":
                Stop(event);
                break;
            case "adminreg":
                AdminReg(event);
                break;
            case "test":
                test(event);
                break;
            case "activitervocale":
                ActiviterVocal(event);
                break;
            case "profil":
                if(event.getGuild() != null)
                {
                    Profil(event);
                    break;
                }
            default:
                event.reply("Je n'ai pas sur répondre a ta requete :( ").setEphemeral(true).queue();

        }
    }

    private void ActiviterVocal(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int limit = (event.getOption("limite") != null) ? event.getOption("limite").getAsInt() : 10;
        String filter = (event.getOption("filtrer") != null) ? event.getOption("filtrer").getAsString() : "temps";
        event.getHook().sendMessageEmbeds(advancedVoiceActivityBuilder(event.getGuild().getId(),limit,filter).build()).queue();
    }

    private void test(SlashCommandInteractionEvent event)
    {
        event.reply("Oui oui tu veux quoi ? un macdo ? UwU ~~").setEphemeral(true).queue();
    }

    private void AdminReg(SlashCommandInteractionEvent event)
    {
        event.deferReply().setEphemeral(true).queue();
        if(!event.getMember().hasPermission(Permission.ADMINISTRATOR))
        {
            event.getHook().sendMessage("Vous ne pouvez pas faire cette commande !").queue();
            return;
        }
        List<Member> guildMembers = event.getGuild().getMembers();
        String guildId = event.getGuild().getId();
        String membreAdded = "";
        for(Member member : guildMembers )
        {
            log.debug(member.getUser().toString());
            if(!member.getUser().isBot())
            {
                log.debug(member.getUser().toString());
                Document querry =  new Document().append("UserId",member.getUser().getId()).append("ServerId",guildId);
                if(usersCollection.find(querry).first() == null)
                {
                    log.debug(member.getUser().toString());
                    usersCollection.insertOne
                            (new Document()
                                    .append("UserId", member.getUser().getId())
                                    .append("ServerId", guildId)
                                    .append("MessageCount", 0)
                                    .append("BumbCount", 0)
                                    .append("VoiceHoursCount", 0.0)
                            );
                    membreAdded+=String.format(", %s",member.getUser().getName());
                }
            }
        }
        event.getHook().sendMessage(String.format("Voicis les membres qui n'était pas enregistrer %s",membreAdded)).queue();
    }

    public void Stop(SlashCommandInteractionEvent event)
    {
        String UserId = event.getUser().getId();
        if(UserId.equals("354649747983695882") || UserId.equals("478641900887474206"))
        {
            event.reply("Je m'éteins, au revoir :wave: ").setEphemeral(true).queue();
            Shutdown(event.getOption("restart").getAsBoolean());
            return;
        }
        event.reply("Vous n'êtes pas un owner !-!").setEphemeral(true).queue();
    }
    public  void Profil(SlashCommandInteractionEvent event)
    {
        event.deferReply().queue();
        try{
            User user = (event.getOption("membre") != null) ? event.getOption("membre").getAsUser() : event.getUser();
            Document doc = getUserProfileInfo(user,event.getGuild().getId());
            event.getHook().sendMessageEmbeds(UserProfile(user, doc.getInteger("MessageCount").toString(), doc.getDouble("VoiceHoursCount"),event.getGuild().getId()).build()).queue();
        }catch (Exception ignored)
        {
            event.getHook().sendMessage("Je n'ai pas sur répondre a ta requete :( ").setEphemeral(true).queue();
        }
    }

    @Nullable
    private static Document getUserProfileInfo(User user,String ServerId) {
        Bson querryFilter = Projections.fields(Projections.excludeId());
        Document querry =  new Document().append("UserId", user.getId()).append("ServerId", ServerId);
        Document doc = usersCollection.find(querry).projection(querryFilter).first();
        return doc;
    }

    public void VerifyReg(SlashCommandInteractionEvent event)
    {
        Document querry =  new Document().append("UserId",event.getUser().getId()).append("ServerId",event.getGuild().getId());
        if(usersCollection.find(querry).first() == null) {
            InsertOneResult result = usersCollection.insertOne
                    (new Document()
                            .append("UserId", event.getUser().getId())
                            .append("ServerId", event.getGuild().getId())
                            .append("MessageCount", 0)
                            .append("BumbCount", 0)
                            .append("VoiceHoursCount", 0.0)
                    );
            event.reply("Désoler de ne pas avoir pus vous enregister quand vous avez join :/").setEphemeral(true).queue();
        }
        event.reply("Vous êtes déjà enregister !").setEphemeral(true).queue();
    }
}
