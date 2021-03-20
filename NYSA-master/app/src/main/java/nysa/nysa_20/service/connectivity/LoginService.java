package nysa.nysa_20.service.connectivity;

import org.json.JSONObject;

import java.util.concurrent.Callable;

import nysa.nysa_20.model.LoginFormular;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginService implements Callable {

    private static RetrofitService retrofitService;
    private static Retrofit retrofit;
    private static LoginFormular formular;

    public LoginService(LoginFormular formular){ this.formular = formular; }

    private static void initializeRetrofit() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofitService = retrofit.create(RetrofitService.class);
        }
    }


    @Override
    public Object call() throws Exception {
        initializeRetrofit();
        Call<ResponseBody> call = retrofitService.login(
                formular.getEmail(),
                formular.getPassword()
        );

        String jsonResponse = call.execute().body().string();
        JSONObject apiResponse = new JSONObject(jsonResponse);
        return apiResponse;
    }
}
