package com.yukiDev.Yuki;

import com.mongodb.client.model.Projections;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.yukiDev.Yuki.Bot.*;

public class GuildEventListener extends ListenerAdapter
{
    private static final Logger log = LoggerFactory.getLogger(GuildEventListener.class);

    @Override
    public  void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot()) return;
        if(event.isFromGuild())
        {
            Document query = new Document().append("UserId",event.getAuthor().getId()).append("ServerId",event.getGuild().getId());
            Bson updates = Updates.combine(Updates.inc("MessageCount",1));
            usersCollection.updateOne(query,updates);
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event)
    {
        try {
            Document query = new Document().append("UserId", event.getUser().getId()).append("ServerId", event.getGuild().getId());
            if (usersCollection.find(query).first() == null) {
                usersCollection.insertOne
                        (new Document()
                                .append("UserId", event.getUser().getId())
                                .append("ServerId", event.getGuild().getId())
                                .append("MessageCount", 0)
                                .append("BumpCount", 0)
                                .append("VoiceHoursCount", 0.0)
                        );
            }
        }catch (Exception ignored)
        {}
        try {
            TextChannel welcome = guildOption.get(event.getGuild().getId()).WelcomeChannel;
            String welcomeText = "Hey regardez qui vien de nous rejoindres %s, bienvenue a toi ! :wave:";
            MessageCreateAction t = welcome.sendMessage(String.format(welcomeText, event.getUser().getGlobalName())).addActionRow(
                    Button.secondary("welcome", "Souhaite bienvenue !").withEmoji(Emoji.fromUnicode("\uD83D\uDC4B"))
            );
            t.queue(message ->{
                Document query = new Document()
                        .append("ServerId", event.getGuild().getId())
                        .append("MessageId",message.getIdLong())
                        .append("UserWelcomed",event.getUser().getGlobalName())
                        .append("Date", LocalDateTime.now())
                        .append("UserSaidHi","");
                welcomeCollection.insertOne(query);});
        }catch (InsufficientPermissionException exception)
        {
            log.error(exception.getMessage());
        }


    }
    @Override
    public  void onButtonInteraction(ButtonInteractionEvent event)
    {
        event.deferReply().setEphemeral(true).queue();
        if(event.getButton().getId().contains("welcome"))
        {
            try {
                Bson queryFilter = Projections.fields(Projections.excludeId());
                Document query = new Document().append("ServerId", event.getGuild().getId()).append("MessageId",event.getMessage().getIdLong());
                log.debug(query.toString());
                Document result = welcomeCollection.find(query).projection(queryFilter).first();
                String[] users = result.getString("UserSaidHi").split(";");
                if(Objects.equals(result.getString("UserWelcomed"), event.getUser().getGlobalName()))
                {
                    event.getHook().sendMessage("Tu ne peux pas dire bonjours a toi même !").queue();
                    return;
                }
                if(IsUserSaidWelcome(users,event.getUser().getId()))
                {

                    event.getHook().sendMessage(String.format("Tu as déjà dit bienvenue à %s ! :angry: ",result.getString("UserWelcomed"))).setEphemeral(true).queue();
                    return;
                }
                Button button = event.getButton();
                if(button.getLabel().contains("Souhaite bienvenue !"))event.editButton(event.getButton().withLabel("1 Personne t'a dit bienvenue !")).queue();
                else {
                    String[] welcome = button.getLabel().split(" ");
                    int i = Integer.parseInt(welcome[0]);
                    i++;
                    event.editButton(event.getButton().withLabel(String.format("%s personnes t'ont dit bienvenue", i))).queue();
                }
                Bson updates = Updates.set("UserSaidHi",StringBuilder(users,event.getUser().getId()));
                welcomeCollection.updateOne(query,updates);
                event.getHook().sendMessage(String.format("Tu as dit bienvenue à %s", result.getString("UserWelcomed"))).setEphemeral(true).queue();
            }catch (Exception ignored)
            {
                event.getHook().sendMessage("Erreur 404 je ne fonctionne plus pour ta requête :(  ").setEphemeral(true).queue();
            }
            return;
        }
        event.getHook().sendMessage("Erreur 404 je ne fonctionne plus pour ta requête :(  ").setEphemeral(true).queue();
    }

    public boolean IsUserSaidWelcome(String[] users,String userID)
    {
        for(String i : users)
        {
            if(userID.equals(i))return true;
        }return  false;
    }
    public String StringBuilder(String[]users, String userID)
    {
        String ret = "";
        for(String i : users)
        {
            if(i != ";")ret+= i +";";
        }
        ret+= userID;
        return ret;
    }

}
