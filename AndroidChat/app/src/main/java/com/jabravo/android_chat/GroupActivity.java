package com.jabravo.android_chat;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jabravo.android_chat.Data.Friend;
import com.jabravo.android_chat.Data.Message;
import com.jabravo.android_chat.Data.MessageList;
import com.jabravo.android_chat.Data.PausableThreadPool;
import com.jabravo.android_chat.Data.User;
import com.jabravo.android_chat.Services.Sender;
import com.jabravo.android_chat.Services.Service;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener
{
    private ImageButton sendButton;
    private EditText keyboard;
    private ScrollView scrollView;
    private LinearLayout messagesLayout;
    private ImageView userImage;

    private Ringtone ringtone;
    private PausableThreadPool executor;
    private Thread threadReceiver;
    private MessageList messages;
    private User user;
    private Friend friend;
    private Service service;

    private int toID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        toID = getIntent().getExtras().getInt("toID");

        user = User.getInstance();
        friend = user.getFriendsHashMap().get(String.valueOf(toID));

        service = new Service();

        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sendButton = (ImageButton) findViewById(R.id.chat_send);
        keyboard = (EditText) findViewById(R.id.chat_keyboard);
        scrollView = (ScrollView) findViewById(R.id.chat_scroll);
        messagesLayout = (LinearLayout) findViewById(R.id.chat_messages);
        userImage = (ImageView) findViewById(R.id.chat_user_image);

        sendButton.setOnClickListener(this);

        changeToolBar();

        messages = new MessageList();

        // Esto es para conseguir y hacer que suene el sonido de notificacion.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String alarms = preferences.getString("message-notification-sound", "default ringtone");

        Uri uri = Uri.parse(alarms);
        ringtone = RingtoneManager.getRingtone(this, uri);

        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

        try
        {
            queue.put(service);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        executor = new PausableThreadPool(2,2,10, TimeUnit.SECONDS,queue);
        executor.execute(service);

    }

    private void changeToolBar()
    {
        // TODO: 14/12/2015 CAMBIAR LA IMAGEN POR LA DEL USUARIO
        setTitle(friend.getNick());
        //userImage.setImageURI();
    }

    // Save the messages and the counter when the app changes orientation.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("messages", messages);
    }

    @Override
    protected void onPause()
    {
        while  (threadReceiver.isAlive())
        {
            threadReceiver.interrupt();
        }
        super.onPause();
        executor.pause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        executor.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        threadReceiver = new Thread(Receiver);
        threadReceiver.start();
        executor.resume();
    }

    // Load the messages and the counter when the app changes orientation.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        messages = savedInstanceState.getParcelable("messages");

        for (Message message : messages)
        {
            showMessage(message);
        }
    }

    public void showMessage(Message message)
    {

        TextView textView = new TextView(this);
        textView.setText(message.getText());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        if (message.getIdFriend() == user.getID())
        {
            params.gravity = Gravity.RIGHT;
            textView.setPadding(50, 10, 10, 10);
        }
        else
        {
            ringtone.play();
            params.gravity = Gravity.LEFT;
            textView.setPadding(10, 10, 50, 10);
        }

        textView.setBackgroundResource(R.drawable.message_1);
        textView.setLayoutParams(params);

        messagesLayout.addView(textView);
        keyboard.setText("");

        try
        {
            // This scrolls the ScrollView after the message has been added
            scrollView.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v)
    {
        if (keyboard.getText().toString().length() != 0)
        {
            Message message = new Message(keyboard.getText().toString(), user.getID(), toID);
            messages.add(message);

            sendMessage(message.getText());
            showMessage(message);
        }
    }

    private void sendMessage(String message)
    {
        Sender sender = new Sender();
        sender.execute(message,String.valueOf(toID),String.valueOf(user.getID()));
    }

    // **********************************************
    // Clase para recibir mensajes
    // **********************************************

    public Runnable Receiver = new Runnable() {
        @Override
        public void run() {

            while (!Thread.interrupted())
            {
                if (!service.getBuffer().isEmpty())
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            Log.i("pruebas", String.valueOf(service.getBuffer().size()));
                            Iterator<Message> it = service.getBuffer().iterator();
                            while(it.hasNext())
                            {

                                Message message = it.next();
                                Log.i("pruebas", String.valueOf(message.getIdFriend() + "-" + toID));
                                if(message.getReceiver() == user.getID() &&
                                        message.getIdFriend() == toID)
                                {
                                    messages.add(message);
                                    showMessage(message);
                                    it.remove();
                                }
                            }
                        }
                    });
                }
                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    };
}