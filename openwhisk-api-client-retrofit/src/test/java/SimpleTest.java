
import java.util.List;

import nl.xup.openwhisk.Openwhisk;
import nl.xup.openwhisk.api.model.gson.Activation;
import nl.xup.openwhisk.api.model.gson.EntityBrief;
import nl.xup.openwhisk.api.model.gson.KeyValue;
import nl.xup.openwhisk.api.model.gson.ModelPackage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimpleTest {

  // --------------------------------------------------------------------------
  // Entrypoint
  // --------------------------------------------------------------------------

  public static void main( String[] args ) {
    // Either set this config directly...
    String hostUrl = "https://openwhisk.ng.bluemix.net:443";
    String authName = "YOUR-AUTHNAME";
    String apiKey = "YOUR-APIKEY";
    String namespace = "YOUR-NAMESPACE";
    // or use environment variables
    authName = System.getenv( "AUTH_NAME" );
    apiKey = System.getenv( "API_KEY" );
    namespace = System.getenv( "NAMESPACE" );
    final Openwhisk openwhisk = new Openwhisk( hostUrl, authName, apiKey, namespace );
//    getPackages( openwhisk, namespace );
//    getPackage( openwhisk, namespace, "waitr" );
//    getActions( openwhisk, namespace );
    final KeyValue parameters = new KeyValue();
    parameters.key( "params" );
    parameters.setValue( "hallo" );
    invokeAction( openwhisk, namespace, "traeckit.tenants", "debug", parameters );
  }

  // --------------------------------------------------------------------------
  // Private methods
  // --------------------------------------------------------------------------

  private static void getPackages( final Openwhisk openwhisk, final String namespace ) {
    Call<List<EntityBrief>> response =
        openwhisk.packages().getAlPackages( namespace, null, null, null );
    response.enqueue( new Callback<List<EntityBrief>>() {

      @Override
      public void onResponse( Call<List<EntityBrief>> call, Response<List<EntityBrief>> response ) {
        System.out.println( "packages:" );
        for (EntityBrief entityBrief : response.body()) {
          System.out.println( entityBrief.getName() );
        }
      }

      @Override
      public void onFailure( Call<List<EntityBrief>> call, Throwable t ) {
        t.printStackTrace();
      }
    } );
  }

  private static void getPackage( final Openwhisk openwhisk, final String namespace,
      final String name ) {
    Call<ModelPackage> response = openwhisk.packages().getPackageByName( namespace, name );
    response.enqueue( new Callback<ModelPackage>() {

      @Override
      public void onResponse( Call<ModelPackage> call, Response<ModelPackage> response ) {
        System.out.println( "package:" + response.body().getName() );
        for (KeyValue entry : response.body().getParameters()) {
          System.out.println( entry.getKey() );
        }
      }

      @Override
      public void onFailure( Call<ModelPackage> call, Throwable t ) {
        t.printStackTrace();
      }
    } );
  }

  private static void getActions( final Openwhisk openwhisk, final String namespace ) {
    Call<List<EntityBrief>> response = openwhisk.actions().getAllActions( namespace, null, null );
    response.enqueue( new Callback<List<EntityBrief>>() {

      @Override
      public void onResponse( Call<List<EntityBrief>> call, Response<List<EntityBrief>> response ) {
        System.out.println( "actions:" );
        for (EntityBrief entityBrief : response.body()) {
          System.out.println( entityBrief.getName() );
        }
      }

      @Override
      public void onFailure( Call<List<EntityBrief>> call, Throwable t ) {
        t.printStackTrace();
      }
    } );
  }

  private static void invokeAction( final Openwhisk openwhisk, final String namespace, final String packageName, final String actionName, final KeyValue parameters ) {
    Call<Activation> response = openwhisk.actions().invokeAction( namespace, packageName, actionName, parameters, null, null, null);
    response.enqueue( new Callback<Activation>() {

      @Override
      public void onResponse( Call<Activation> call, Response<Activation> response ) {
        System.out.println( "activation:" );
        System.out.println( response.toString() );
      }

      @Override
      public void onFailure( Call<Activation> call, Throwable t ) {
        t.printStackTrace();
      }
    } );
  }
}
