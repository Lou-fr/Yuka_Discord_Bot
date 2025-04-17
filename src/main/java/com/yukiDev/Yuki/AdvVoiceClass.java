package com.yukiDev.Yuki;

import net.dv8tion.jda.api.entities.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdvVoiceClass
{
    protected static List<Member> ConnectedUser;
    protected static LocalDateTime ConnectionStarted;
    protected static int NumberOfUser;
    public AdvVoiceClass(List<Member> connectedUser, LocalDateTime connectionStarted, int numberOfUser)
    {
        ConnectedUser = connectedUser;
        ConnectionStarted = connectionStarted;
        NumberOfUser = numberOfUser;
    }
    public  static  LocalDateTime GetStartTime()
    {
        return ConnectionStarted;
    }
    public  static  int GetNumberOfUser()
    {
        return NumberOfUser;
    }
    public static ArrayList<String> BuildUserIDList()
    {
        ArrayList<String> returned = new ArrayList<>();
        for (Member i : ConnectedUser)
        {
            returned.add(i.getId());
        }
        return returned;
    }
}
