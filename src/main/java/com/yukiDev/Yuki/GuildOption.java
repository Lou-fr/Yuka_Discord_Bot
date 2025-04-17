package com.yukiDev.Yuki;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GuildOption
{
    protected boolean Welcome;
    protected TextChannel WelcomeChannel;
    protected boolean VoiceActivity;
    protected boolean AdvanceLeaderboard;
    protected boolean AdvanceVoiceActivity;
    public GuildOption(boolean welcome,TextChannel welcomeChannel,boolean voiceActivity,boolean advanceLeaderboard,boolean advanceVoiceActivity)
    {
        this.Welcome = welcome;
        if (welcome) {
            this.WelcomeChannel = welcomeChannel;
        }
        this.VoiceActivity = voiceActivity;
        this.AdvanceLeaderboard = advanceLeaderboard;
        this.AdvanceVoiceActivity = advanceVoiceActivity;
    }
}
