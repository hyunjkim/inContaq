package nyc.c4q.jonathancolon.inContaq.sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jonathancolon on 3/28/17.
 */

public class HeadlessSmsSendService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}