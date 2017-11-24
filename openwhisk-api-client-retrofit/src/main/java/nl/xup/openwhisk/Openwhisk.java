package nl.xup.openwhisk;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import nl.xup.openwhisk.api.ActionsApi;
import nl.xup.openwhisk.api.ActivationsApi;
import nl.xup.openwhisk.api.NamespacesApi;
import nl.xup.openwhisk.api.PackagesApi;
import nl.xup.openwhisk.api.RulesApi;
import nl.xup.openwhisk.api.TriggersApi;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Openwhisk {

  // --------------------------------------------------------------------------
  // Constants
  // --------------------------------------------------------------------------

  private static final String OW_API_KEY = "__OW_API_KEY";
  private static final String OW_API_HOST = "__OW_API_HOST";
  private static final String OW_NAMESPACE = "__OW_NAMESPACE";

  // --------------------------------------------------------------------------
  // Object attributes
  // --------------------------------------------------------------------------

  private Retrofit retrofit;

  // --------------------------------------------------------------------------
  // Constructors
  // --------------------------------------------------------------------------

  public Openwhisk() {
    final String owHost = System.getenv().get( OW_API_HOST );
    final String[] owApiKey = System.getenv().get( OW_API_KEY ).split( ":" );
    final String authName = owApiKey[0];
    final String apiKey = owApiKey[1];
    final String owNamespace = System.getenv().get( OW_NAMESPACE );
    createDefaultAdapter( owHost, authName, apiKey, owNamespace );
  }

  public Openwhisk(final String hostUrl, final String authName, final String apiKey,
      final String namespace) {
    createDefaultAdapter( hostUrl, authName, apiKey, namespace );
  }

  // --------------------------------------------------------------------------
  // Interface
  // --------------------------------------------------------------------------

  public ActionsApi actions() {
    return retrofit.create( ActionsApi.class );
  }

  public ActivationsApi activations() {
    return retrofit.create( ActivationsApi.class );
  }

  public NamespacesApi namespaces() {
    return retrofit.create( NamespacesApi.class );
  }

  public PackagesApi packages() {
    return retrofit.create( PackagesApi.class );
  }

  public RulesApi rules() {
    return retrofit.create( RulesApi.class );
  }

  public TriggersApi triggers() {
    return retrofit.create( TriggersApi.class );
  }

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  private void createDefaultAdapter( final String hostUrl, final String authName,
      final String apiKey, final String namespace ) {
    final Gson gson = new GsonBuilder().setLenient().create();

    String baseUrl = hostUrl + "/api/v1/";

    final OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();  
    okHttpClient.addInterceptor( new Interceptor() {
      @Override
      public okhttp3.Response intercept( Chain chain ) throws IOException {
        Request originalRequest = chain.request();

        Request.Builder builder = originalRequest.newBuilder().header( "Authorization",
            Credentials.basic( authName, apiKey ) );

        Request newRequest = builder.build();
        return chain.proceed( newRequest );
      }
    } );
    
    // Add logging interceptor
    // TODO: make it optional
    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();  
    logging.setLevel(Level.BODY);
    okHttpClient.addInterceptor(logging);

    retrofit = new Retrofit.Builder().baseUrl( baseUrl )
        .client( okHttpClient.build() )
        .addConverterFactory( ScalarsConverterFactory.create() )
        .addConverterFactory( GsonCustomConverterFactory.create( gson ) ).build();
  }

}


/**
 * This wrapper is to take care of this case: when the deserialization fails due to
 * JsonParseException and the expected type is String, then just return the body string.
 */
class GsonResponseBodyConverterToString<T> implements Converter<ResponseBody, T> {
  private final Gson gson;
  private final Type type;

  GsonResponseBodyConverterToString(Gson gson, Type type) {
    this.gson = gson;
    this.type = type;
  }

  @Override
  public T convert( ResponseBody value ) throws IOException {
    String returned = value.string();
    try {
      return gson.fromJson( returned, type );
    } catch (JsonParseException e) {
      return (T) returned;
    }
  }
}


class GsonCustomConverterFactory extends Converter.Factory {
  public static GsonCustomConverterFactory create( Gson gson ) {
    return new GsonCustomConverterFactory( gson );
  }

  private final Gson gson;
  private final GsonConverterFactory gsonConverterFactory;

  private GsonCustomConverterFactory(Gson gson) {
    if (gson == null)
      throw new NullPointerException( "gson == null" );
    this.gson = gson;
    this.gsonConverterFactory = GsonConverterFactory.create( gson );
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter( Type type, Annotation[] annotations,
      Retrofit retrofit ) {
    if (type.equals( String.class ))
      return new GsonResponseBodyConverterToString<Object>( gson, type );
    else
      return gsonConverterFactory.responseBodyConverter( type, annotations, retrofit );
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter( Type type,
      Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit ) {
    return gsonConverterFactory.requestBodyConverter( type, parameterAnnotations, methodAnnotations,
        retrofit );
  }
}
