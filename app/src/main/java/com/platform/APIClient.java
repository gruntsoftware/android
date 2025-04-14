package com.platform;

import static com.brainwallet.tools.util.BRCompressor.gZipExtract;

import android.content.Context;
import android.net.Uri;
import android.os.NetworkOnMainThreadException;

import com.brainwallet.BrainwalletApp;
import com.brainwallet.presenter.activities.util.ActivityUTILS;
import com.brainwallet.tools.util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

//some part still used e.g. [sendRequest]
@Deprecated
public class APIClient {

    // proto is the transport protocol to use for talking to the API (either http or https)
    private static final String PROTO = "https";

    // convenience getter for the API endpoint
    public static String BASE_URL = PROTO + "://" + BrainwalletApp.HOST;
    //feePerKb url
    private static final String FEE_PER_KB_URL = "/v1/fee-per-kb";
    //singleton instance
    private static APIClient ourInstance;


    private static final String BUNDLES = "bundles";

    private static final String BUNDLES_FOLDER = String.format("/%s", BUNDLES);

    private static final boolean PRINT_FILES = false;

    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    private final boolean platformUpdating = false;
    private AtomicInteger itemsLeftToUpdate = new AtomicInteger(0);

    private final Context ctx;

    public static synchronized APIClient getInstance(Context context) {
        if (ourInstance == null) ourInstance = new APIClient(context);
        return ourInstance;
    }

    private APIClient(Context context) {
        ctx = context;
        itemsLeftToUpdate = new AtomicInteger(0);
    }

    // sendRequest still using, e.g. inside RemoteKVStore
    public Response sendRequest(Request locRequest, boolean needsAuth, int retryCount) {
        if (retryCount > 1)
            throw new RuntimeException("sendRequest: Warning retryCount is: " + retryCount);
        if (ActivityUTILS.isMainThread()) {
            throw new NetworkOnMainThreadException();
        }
        String lang = ctx.getResources().getConfiguration().locale.getLanguage();
        Request request = locRequest.newBuilder()
                .header("X-Litecoin-Testnet", "false")
                .header("Accept-Language", lang)
                .build();

        Response response;
        ResponseBody postReqBody;
        byte[] data = new byte[0];
        try {
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).connectTimeout(60, TimeUnit.SECONDS)/*.addInterceptor(new LoggingInterceptor())*/.build();

            // DEV Uncomment to see values
            // Timber.d("timber: sendRequest: headers for : %s \n %s", request.url(), request.headers());

            String agent = Utils.getAgentString(ctx, "OkHttp/3.4.1");
            request = request.newBuilder().header("User-agent", agent).build();
            response = client.newCall(request).execute();
            try {
                data = response.body().bytes();
            } catch (IOException e) {
                Timber.e(e);
            }

            if (response.isRedirect()) {
                String newLocation = request.url().scheme() + "://" + request.url().host() + response.header("location");
                Uri newUri = Uri.parse(newLocation);
                if (newUri == null) {
                    Timber.d("timber: sendRequest: redirect uri is null");
                } else if (!newUri.getHost().equalsIgnoreCase(BrainwalletApp.HOST) || !newUri.getScheme().equalsIgnoreCase(PROTO)) {
                    Timber.d("timber: sendRequest: WARNING: redirect is NOT safe: %s", newLocation);
                } else {
                    Timber.d("timber: redirecting: %s >>> %s", request.url(), newLocation);
                    response.close();
                    return sendRequest(new Request.Builder().url(newLocation).get().build(), needsAuth, 0);
                }
                return new Response.Builder().code(500).request(request).body(ResponseBody.create(null, new byte[0])).message("Internal Error").protocol(Protocol.HTTP_1_1).build();
            }
        } catch (IOException e) {
            Timber.e(e);
            return new Response.Builder().code(599).request(request).body(ResponseBody.create(null, new byte[0])).protocol(Protocol.HTTP_1_1).message("Network Connection Timeout").build();
        }

        if (response.header("content-encoding") != null && response.header("content-encoding").equalsIgnoreCase("gzip")) {
            Timber.d("timber: sendRequest: the content is gzip, unzipping");
            byte[] decompressed = gZipExtract(data);
            postReqBody = ResponseBody.create(null, decompressed);
            if (response.code() != 200) {
                Timber.d("timber: sendRequest: (%s)%s, code (%d), mess (%s), body (%s)", request.method(),
                        request.url(), response.code(), response.message(), new String(decompressed, StandardCharsets.UTF_8));
            }
            return response.newBuilder().body(postReqBody).build();
        } else {
            if (response.code() != 200) {
                Timber.d("timber: sendRequest: (%s)%s, code (%d), mess (%s), body (%s)", request.method(),
                        request.url(), response.code(), response.message(), new String(data, StandardCharsets.UTF_8));
            }
        }

        postReqBody = ResponseBody.create(null, data);

        return response.newBuilder().body(postReqBody).build();
    }
}
