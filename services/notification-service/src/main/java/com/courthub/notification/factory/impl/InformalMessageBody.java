package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.MessageBody;


public class InformalMessageBody implements MessageBody {

    @Override
    public String format(String data) {
        return String.format(
            "Hey there! 👋\n\n" +
            "%s\n\n" +
            "See you on the court! 🎾\n" +
            "- CourtHub",
            data
        );
    }
}
