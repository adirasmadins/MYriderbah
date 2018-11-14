package driver.dev.asliborneo.app.myridebah.Service;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import driver.dev.asliborneo.app.myridebah.Model.Token;


public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedtoken= FirebaseInstanceId.getInstance().getToken();
        Updateservertoken(refreshedtoken);
    }

    private void Updateservertoken(String refreshedtoken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(refreshedtoken);
        if(FirebaseAuth.getInstance().getCurrentUser() !=null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}
