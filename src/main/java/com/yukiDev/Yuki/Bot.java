package com.yukiDev.Yuki;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bson.Document;
import java.util.HashMap;

public class Bot {
    public  static  MongoCollection<Document> usersCollection;
    public  static  MongoCollection<Document> welcomeCollection;
    public  static  MongoCollection<Document> voiceCollection;
    public  static HashMap<String,GuildOption> guildOption = new HashMap<>();
    static  JDA api;
    static CommandListUpdateAction commands;
    static MongoClient mongoClient;
    static MongoCollection<Document> guildsCollection;
    public static  void main(String[] args) throws Exception
    {
        String uri = "mongodb://localhost:27017";
        try {
            mongoClient = MongoClients.create(uri);
            MongoDatabase db = mongoClient.getDatabase("YukiDev");
            usersCollection = db.getCollection("Users");
            guildsCollection = db.getCollection("Guilds");
            welcomeCollection = db.getCollection("Welcome");
            voiceCollection = db.getCollection("Voice");
        } catch (Exception ignored) {}
        api = JDABuilder.createDefault(DoNotCommitClass.Token)
                .addEventListeners(new GuildEventListener())
                .addEventListeners(new CommandsList())
                .addEventListeners(new VoiceListener())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setAutoReconnect(true)
                .build()
                .awaitReady();
        commands = api.updateCommands();
        commands.addCommands(
                Commands.slash("stop","Arrête le bot, vous avez besoin d'être le owner")
                        .addOption(OptionType.BOOLEAN,"restart","Redemarre le bot apres l'avoir eteind",true).setContexts(InteractionContextType.ALL),
                Commands.slash("profil","Affiche vos stats du serveur")
                        .addOption(OptionType.USER,"membre","Affiche les stats d'une autre personne du serveur").setContexts(InteractionContextType.GUILD),
                Commands.slash("test","baka").setContexts(InteractionContextType.GUILD),
                Commands.slash("info","donne des information sur le bot").setContexts(InteractionContextType.ALL),
                Commands.slash("activitervocale","Affiche le détailles des membres en voc (mais vraiment tous !) (seulement si l'option est activer)")
                                .addSubcommands(new SubcommandData("default","toutes les valeurs par défault")).setContexts(InteractionContextType.GUILD)
                                .addSubcommands(new SubcommandData("limite","vous permet de changer la valeur max").addOptions(new OptionData(OptionType.INTEGER,"limite","permet de changer la limite max (max 25)",true).setRequiredRange(1,25))).setContexts(InteractionContextType.GUILD),
                Commands.slash("adminreg","Enregistrer toutes les personne déjà présente ! (utile pour un serveur déjà remplis de membre)").setContexts(InteractionContextType.GUILD).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("verifyreg","Verifier si vous êtes bien enregister !").setContexts(InteractionContextType.GUILD)).queue();
        for(Guild i : api.getGuilds())
        {
            guildOption.put(i.getId(), getServerOption(i));
        }
    }
    public static void Shutdown(boolean restart)
    {
        api.shutdown();
        if(restart) {
            try {
                main(new String[0]);
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        mongoClient.close();
        System.exit(0);
    }
    static  GuildOption getServerOption(Guild guild)
    {
        try {
            Document querry = new Document().append("ServerId", guild.getId());
            Document result = guildsCollection.find(querry).first();
            return  new GuildOption(result.getBoolean("Weclome"),api.getTextChannelById(result.getString("WelcomeChannel")),result.getBoolean("VoiceActivity"),result.getBoolean("AdvanceLeaderboard"),result.getBoolean("AdvanceVoiceActivity"));
        }catch (Exception ignored){}
       return null;
    }
    public static User getUser(String userId)
    {
        return api.getUserById(userId);
    }
}
