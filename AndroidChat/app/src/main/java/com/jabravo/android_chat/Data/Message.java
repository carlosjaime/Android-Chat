package com.jabravo.android_chat.Data;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jose Antonio on 26/10/2015.
 */
public class Message implements Parcelable
{
    private String text;
    private String date;
    private int id;
    private boolean read;
    private int sender;
    private int receiver;

    public Message(String text, String date, int id, boolean read, int sender, int receiver)
    {
        this.text = text;
        this.date = date;
        this.id = id;
        this.read = read;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Message(String text, int sender, int receiver)
    {
        this.text = text;
        this.date = "";
        this.id = -1;
        this.read = false;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Message()
    {
        this.text = "";
        this.date = "";
        this.id = -1;
        this.read = false;
        this.sender = -1;
        this.receiver = -1;
    }

    public Message(Parcel in)
    {
        text = in.readString();
        date = in.readString();
        id = in.readInt();
        read = in.readInt() == 1; // 1 = true, 0 = false
        sender = in.readInt();
        receiver = in.readInt();
    }

    public Message(String text, String date, int id, boolean read, int sender)
    {
        this.text = text;
        this.date = date;
        this.id = id;
        this.read = read;
        this.sender = sender;
    }


    public String getText()
    {
        return text;
    }

    public int getSender()
    {
        return sender;
    }

    public void setSender(int sender)
    {
        this.sender = sender;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public boolean isRead()
    {
        return read;
    }

    public void setRead(boolean read)
    {
        this.read = read;
    }

    public int getReceiver()
    {
        return receiver;
    }

    public void setReceiver(int receiver)
    {
        this.receiver = receiver;
    }

    // This is so we can put a Message in a bundle.
    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(text);
        dest.writeString(date);
        dest.writeInt(id);
        dest.writeInt(read ? 1 : 0);
        dest.writeInt(sender);
        dest.writeInt(receiver);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>()
    {
        public Message createFromParcel(Parcel in)
        {
            return new Message(in);
        }

        public Message[] newArray(int size)
        {
            return new Message[size];
        }
    };
}