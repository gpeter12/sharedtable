package com.sharedtable.model.network.signals;

import com.sharedtable.Utils;

import java.util.Objects;
import java.util.UUID;

public class ChatMessageSignal implements Signal{

    private UUID messageID;
    private UUID creatorID;
    private String nickname;
    private String message;

    public ChatMessageSignal(UUID cratorID, String nickname, String message) {
        this.creatorID = cratorID;
        this.nickname = nickname;
        this.message = message;
        if(message.equals(""))
            this.message = " ";
        this.messageID = UUID.randomUUID();
    }

    public ChatMessageSignal(String[] input) {
        if(input.length != 6)
            throw new IllegalArgumentException("ChatMessageSignal illegal input: "+ Utils.recombineStringArray(input));
        this.creatorID = UUID.fromString(input[2]);
        this.messageID = UUID.fromString(input[3]);
        this.nickname = input[4];
        this.message = input[5];

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIG;CHAT;").append(creatorID).append(";")
                .append(messageID).append(";")
                .append(nickname).append(";")
                .append(message);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessageSignal that = (ChatMessageSignal) o;
        return Objects.equals(messageID, that.messageID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageID);
    }

    public UUID getCreatorID() {
        return creatorID;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }


}
