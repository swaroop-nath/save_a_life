package com.george.savealife.signinflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by acer on 3/2/2018.
 */

public class SMSReceiver extends BroadcastReceiver{
    String messageBody, code,messageArray[];

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Receiver","On Receive Called");
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            for (SmsMessage message : smsMessages) {
                // Do whatever you want to do with SMS.
                messageBody=message.getMessageBody();
//                Toast.makeText(context, messageBody+"\n"+sender, Toast.LENGTH_SHORT).show();
                messageArray=messageBody.split(" ");
                if (messageArray[messageArray.length-1]=="code")
                {
                    if (messageArray[messageArray.length-2]=="verification")
                    {
                        if (messageArray[messageArray.length-3]=="your") {
                            code = messageArray[0];
                            Intent codeIntent=new Intent();
                            codeIntent.putExtra("otp",code);
                            context.sendBroadcast(codeIntent);
                        }
                    }
                }
            }
        }
    }
}
