package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.MessageBody;


public class FormalMessageBody implements MessageBody {

    @Override
    public String format(String data) {
        return String.format(
            "Dear Valued Customer,\n\n" +
            "%s\n\n" +
            "Thank you for choosing CourtHub.\n\n" +
            "Best Regards,\n" +
            "The CourtHub Team",
            data
        );
    }
}
